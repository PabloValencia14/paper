/*
    SPDX-FileCopyrightText: 2026 Pablo Valencia

    SPDX-License-Identifier: GPL-2.0-or-later
*/

#include "paperworkspace.h"

#include "core/document.h"
#include "pageview.h"

#include <QDateTime>
#include <QDir>
#include <QFile>
#include <QFileInfo>
#include <QFont>
#include <QHBoxLayout>
#include <QIcon>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonValue>
#include <QLabel>
#include <QLineEdit>
#include <QNetworkAccessManager>
#include <QNetworkReply>
#include <QNetworkRequest>
#include <QPlainTextEdit>
#include <QPushButton>
#include <QSaveFile>
#include <QSettings>
#include <QTextCursor>
#include <QTimer>
#include <QVBoxLayout>

#include <KLocalizedString>

#include <initializer_list>

namespace
{
constexpr auto defaultEndpoint = "http://100.94.0.92:8082/v1/chat/completions";
constexpr int maximumContextCharacters = 12000;
constexpr int maximumConversationMessages = 12;

QString contentFromMessage(const QJsonObject &message)
{
    const QJsonValue content = message.value(QStringLiteral("content"));
    if (content.isString()) {
        return content.toString();
    }
    if (!content.isArray()) {
        return {};
    }

    QString result;
    for (const QJsonValue &part : content.toArray()) {
        if (part.isObject()) {
            result += part.toObject().value(QStringLiteral("text")).toString();
        }
    }
    return result;
}

QString firstString(const QJsonObject &object, std::initializer_list<QString> names)
{
    for (const QString &name : names) {
        const QJsonValue value = object.value(name);
        if (value.isString() && !value.toString().trimmed().isEmpty()) {
            return value.toString();
        }
    }
    return {};
}
}

PaperWorkspace::PaperWorkspace(QWidget *parent)
    : QWidget(parent)
    , m_network(new QNetworkAccessManager(this))
{
    auto *rootLayout = new QVBoxLayout(this);
    rootLayout->setContentsMargins(12, 12, 12, 12);
    rootLayout->setSpacing(8);

    auto *title = new QLabel(i18n("Paper workspace"), this);
    QFont titleFont = title->font();
    titleFont.setBold(true);
    titleFont.setPointSize(titleFont.pointSize() + 1);
    title->setFont(titleFont);
    rootLayout->addWidget(title);

    auto *subtitle = new QLabel(i18n("Notas y asistencia local sin modificar el PDF"), this);
    subtitle->setWordWrap(true);
    subtitle->setStyleSheet(QStringLiteral("color: palette(mid);"));
    rootLayout->addWidget(subtitle);

    m_documentLabel = new QLabel(i18n("Ningún documento abierto"), this);
    m_documentLabel->setWordWrap(true);
    rootLayout->addWidget(m_documentLabel);

    auto *notesLabel = new QLabel(i18n("Notas Markdown"), this);
    rootLayout->addWidget(notesLabel);

    m_notesEdit = new QPlainTextEdit(this);
    m_notesEdit->setPlaceholderText(i18n("Escribe aquí tus notas del documento…"));
    m_notesEdit->setMinimumHeight(150);
    m_notesEdit->setTabChangesFocus(false);
    rootLayout->addWidget(m_notesEdit);

    auto *notesActions = new QHBoxLayout;
    m_sessionStatus = new QLabel(i18n("Las notas se guardan junto al documento."), this);
    m_sessionStatus->setWordWrap(true);
    notesActions->addWidget(m_sessionStatus, 1);
    m_saveButton = new QPushButton(i18n("Guardar sesión"), this);
    m_saveButton->setIcon(QIcon::fromTheme(QStringLiteral("document-save")));
    notesActions->addWidget(m_saveButton);
    rootLayout->addLayout(notesActions);

    auto *aiLabel = new QLabel(i18n("Asistente IA local"), this);
    rootLayout->addWidget(aiLabel);

    auto *endpointLayout = new QHBoxLayout;
    endpointLayout->addWidget(new QLabel(i18n("Servidor:"), this));
    m_endpointEdit = new QLineEdit(this);
    QSettings settings;
    m_endpointEdit->setText(settings.value(QStringLiteral("paper/aiEndpoint"), QString::fromLatin1(defaultEndpoint)).toString());
    m_endpointEdit->setPlaceholderText(QString::fromLatin1(defaultEndpoint));
    endpointLayout->addWidget(m_endpointEdit, 1);
    endpointLayout->addWidget(new QLabel(i18n("Modelo:"), this));
    m_modelEdit = new QLineEdit(this);
    m_modelEdit->setText(settings.value(QStringLiteral("paper/aiModel"), QStringLiteral("auto")).toString());
    m_modelEdit->setMaximumWidth(100);
    endpointLayout->addWidget(m_modelEdit);
    rootLayout->addLayout(endpointLayout);

    auto *contextHint = new QLabel(i18n("Solo se envían a la IA la selección y la página activa, no el PDF completo."), this);
    contextHint->setWordWrap(true);
    contextHint->setStyleSheet(QStringLiteral("color: palette(mid);"));
    rootLayout->addWidget(contextHint);

    m_promptEdit = new QPlainTextEdit(this);
    m_promptEdit->setPlaceholderText(i18n("Pregunta sobre la selección o la página activa…"));
    m_promptEdit->setMinimumHeight(70);
    rootLayout->addWidget(m_promptEdit);

    auto *askRow = new QHBoxLayout;
    m_aiStatus = new QLabel(i18n("IA local lista"), this);
    m_aiStatus->setWordWrap(true);
    askRow->addWidget(m_aiStatus, 1);
    m_askButton = new QPushButton(i18n("Preguntar"), this);
    m_askButton->setIcon(QIcon::fromTheme(QStringLiteral("dialog-ok")));
    askRow->addWidget(m_askButton);
    rootLayout->addLayout(askRow);

    m_responseEdit = new QPlainTextEdit(this);
    m_responseEdit->setReadOnly(true);
    m_responseEdit->setPlaceholderText(i18n("Las respuestas aparecerán aquí."));
    m_responseEdit->setMinimumHeight(220);
    rootLayout->addWidget(m_responseEdit);

    rootLayout->addStretch(1);

    connect(m_notesEdit, &QPlainTextEdit::textChanged, this, [this] {
        if (!m_sessionLoaded) {
            return;
        }
        m_sessionDirty = true;
        m_sessionStatus->setText(i18n("Cambios pendientes de guardar."));
    });
    connect(m_saveButton, &QPushButton::clicked, this, &PaperWorkspace::saveNotes);
    connect(m_askButton, &QPushButton::clicked, this, &PaperWorkspace::sendPrompt);
    connect(m_promptEdit, &QPlainTextEdit::textChanged, this, [this] { m_askButton->setEnabled(m_pendingReply == nullptr && !m_promptEdit->toPlainText().trimmed().isEmpty() && !m_documentPath.isEmpty()); });

    clearDocument();
}

PaperWorkspace::~PaperWorkspace()
{
    if (m_pendingReply) {
        m_pendingReply->abort();
    }
}

void PaperWorkspace::setDocument(Okular::Document *document, PageView *pageView)
{
    m_document = document;
    m_pageView = pageView;
}

void PaperWorkspace::setDocumentUrl(const QUrl &url)
{
    const QString newPath = url.isLocalFile() ? QFileInfo(url.toLocalFile()).absoluteFilePath() : QString();
    if (newPath == m_documentPath && url == m_documentUrl) {
        return;
    }

    if (m_pendingReply) {
        m_pendingReply->abort();
        m_pendingReply = nullptr;
    }

    m_documentUrl = url;
    m_documentPath = newPath;
    m_sessionLoaded = false;
    m_sessionDirty = false;
    m_sidecarData = {};
    m_conversation = {};
    m_responseEdit->clear();
    m_notesEdit->clear();

    if (m_documentPath.isEmpty()) {
        m_documentLabel->setText(i18n("Ningún documento local abierto"));
        m_sessionStatus->setText(i18n("Las sesiones solo se guardan para documentos locales."));
        m_saveButton->setEnabled(false);
    } else {
        m_documentLabel->setText(i18n("Documento: %1", QFileInfo(m_documentPath).fileName()));
        m_saveButton->setEnabled(true);
        loadSession();
    }
    m_askButton->setEnabled(false);
}

QString PaperWorkspace::sidecarPath() const
{
    if (m_documentPath.isEmpty()) {
        return {};
    }

    const QFileInfo documentInfo(m_documentPath);
    return documentInfo.absolutePath() + QDir::separator() + documentInfo.completeBaseName() + QStringLiteral(".paper.json");
}

void PaperWorkspace::loadSession()
{
    if (m_documentPath.isEmpty()) {
        return;
    }

    const QString path = sidecarPath();
    QFile file(path);
    if (!file.exists()) {
        m_sidecarData = {};
        m_sessionLoaded = true;
        m_sessionDirty = false;
        m_sessionStatus->setText(i18n("No hay sesión previa; se creará al guardar."));
        return;
    }
    if (!file.open(QIODevice::ReadOnly)) {
        m_sessionLoaded = true;
        setStatus(i18n("No se pudo leer %1: %2", QFileInfo(path).fileName(), file.errorString()), true);
        return;
    }

    QJsonParseError parseError;
    const QJsonDocument document = QJsonDocument::fromJson(file.readAll(), &parseError);
    if (parseError.error != QJsonParseError::NoError || !document.isObject()) {
        m_sidecarData = {};
        m_sessionLoaded = true;
        setStatus(i18n("La sesión no es válida: %1", parseError.errorString()), true);
        return;
    }

    const QJsonObject root = document.object();
    m_sidecarData = root;
    m_notesEdit->blockSignals(true);
    m_notesEdit->setPlainText(root.value(QStringLiteral("notes")).toString());
    m_notesEdit->blockSignals(false);

    if (m_document && m_document->pages() > 0) {
        const QString viewportString = root.value(QStringLiteral("viewport")).toString();
        Okular::DocumentViewport viewport(viewportString);
        if (!viewport.isValid()) {
            const int page = root.value(QStringLiteral("currentPage")).toInt(-1);
            if (page >= 0 && page < static_cast<int>(m_document->pages())) {
                viewport = Okular::DocumentViewport(page);
            }
        }
        if (viewport.isValid()) {
            m_document->setViewport(viewport, nullptr, false, false);
        }
    }

    m_sessionLoaded = true;
    m_sessionDirty = false;
    m_saveButton->setEnabled(true);
    m_sessionStatus->setText(i18n("Sesión restaurada desde %1.", QFileInfo(path).fileName()));
}

bool PaperWorkspace::saveSession()
{
    if (m_documentPath.isEmpty()) {
        return true;
    }

    QJsonObject root = m_sidecarData;
    root.insert(QStringLiteral("format"), QStringLiteral("paper"));
    root.insert(QStringLiteral("version"), 1);
    root.insert(QStringLiteral("currentPage"), m_document ? static_cast<int>(m_document->currentPage()) : 0);
    root.insert(QStringLiteral("viewport"), m_document ? m_document->viewport().toString() : QString());
    root.insert(QStringLiteral("notes"), m_notesEdit->toPlainText());
    root.insert(QStringLiteral("updatedAt"), QDateTime::currentDateTimeUtc().toString(Qt::ISODate));

    QSaveFile file(sidecarPath());
    if (!file.open(QIODevice::WriteOnly)) {
        setStatus(i18n("No se pudo guardar la sesión: %1", file.errorString()), true);
        return false;
    }
    const QByteArray serialized = QJsonDocument(root).toJson(QJsonDocument::Indented);
    if (file.write(serialized) != serialized.size()) {
        setStatus(i18n("No se pudo escribir la sesión: %1", file.errorString()), true);
        return false;
    }
    if (!file.commit()) {
        setStatus(i18n("No se pudo completar el guardado de la sesión: %1", file.errorString()), true);
        return false;
    }

    m_sidecarData = root;
    m_sessionLoaded = true;
    m_sessionDirty = false;
    m_sessionStatus->setText(i18n("Sesión guardada en %1.", QFileInfo(sidecarPath()).fileName()));
    return true;
}

void PaperWorkspace::clearDocument()
{
    if (m_pendingReply) {
        m_pendingReply->abort();
        m_pendingReply = nullptr;
    }
    m_documentUrl = QUrl();
    m_documentPath.clear();
    m_sessionLoaded = false;
    m_sessionDirty = false;
    m_sidecarData = {};
    m_conversation = {};
    m_notesEdit->clear();
    m_responseEdit->clear();
    m_documentLabel->setText(i18n("Ningún documento abierto"));
    m_sessionStatus->setText(i18n("Las notas se guardan junto al documento."));
    m_aiStatus->setText(i18n("IA local lista"));
    m_aiStatus->setStyleSheet(QString());
    m_saveButton->setEnabled(false);
    m_askButton->setEnabled(false);
}

QString PaperWorkspace::documentContext() const
{
    if (!m_document || !m_pageView || !m_document->pages()) {
        return i18n("No hay ningún documento PDF abierto.");
    }

    const int pageNumber = static_cast<int>(m_document->currentPage());
    const QString selected = m_pageView->selectedText().trimmed();
    const QString pageText = m_pageView->currentPageText().trimmed();
    QString context;
    context += i18n("Documento: %1\n", QFileInfo(m_documentPath).fileName());
    context += i18n("Página activa: %1 de %2\n", pageNumber + 1, m_document->pages());
    if (!selected.isEmpty()) {
        context += i18n("Texto seleccionado:\n%1\n", selected.left(8000));
    }
    if (!pageText.isEmpty()) {
        context += i18n("Texto de la página activa:\n%1\n", pageText.left(8000));
    }
    const QString notes = m_notesEdit->toPlainText().trimmed();
    if (!notes.isEmpty()) {
        context += i18n("Notas del usuario:\n%1\n", notes.left(3000));
    }
    return context.left(maximumContextCharacters);
}

void PaperWorkspace::sendPrompt()
{
    if (m_pendingReply) {
        return;
    }

    const QString prompt = m_promptEdit->toPlainText().trimmed();
    if (prompt.isEmpty() || m_documentPath.isEmpty()) {
        return;
    }

    const QUrl endpoint(m_endpointEdit->text().trimmed());
    if (!endpoint.isValid() || (endpoint.scheme() != QLatin1String("http") && endpoint.scheme() != QLatin1String("https"))) {
        setStatus(i18n("El endpoint de IA no es una URL HTTP válida."), true);
        return;
    }

    QSettings settings;
    settings.setValue(QStringLiteral("paper/aiEndpoint"), endpoint.toString());
    settings.setValue(QStringLiteral("paper/aiModel"), m_modelEdit->text().trimmed());

    QJsonObject systemMessage;
    systemMessage.insert(QStringLiteral("role"), QStringLiteral("system"));
    systemMessage.insert(
        QStringLiteral("content"),
        i18n("Eres el asistente de Paper, un lector local de PDFs. Responde con rigor, separa hechos del documento de inferencias y no inventes referencias ni contenido ausente. Usa el idioma de la consulta.\n\n%1", documentContext()));

    QJsonArray messages;
    messages.append(systemMessage);
    for (const QJsonValue &message : m_conversation) {
        messages.append(message);
    }
    QJsonObject userMessage;
    userMessage.insert(QStringLiteral("role"), QStringLiteral("user"));
    userMessage.insert(QStringLiteral("content"), prompt);
    messages.append(userMessage);

    QJsonObject payload;
    payload.insert(QStringLiteral("model"), m_modelEdit->text().trimmed().isEmpty() ? QStringLiteral("auto") : m_modelEdit->text().trimmed());
    payload.insert(QStringLiteral("messages"), messages);

    m_conversation.append(userMessage);
    while (m_conversation.size() > maximumConversationMessages) {
        m_conversation.removeFirst();
    }
    appendConversationMessage(i18n("Tú"), prompt);
    m_promptEdit->clear();
    m_askButton->setEnabled(false);
    m_aiStatus->setText(i18n("Consultando IA local…"));

    QNetworkRequest request(endpoint);
    request.setHeader(QNetworkRequest::ContentTypeHeader, QStringLiteral("application/json; charset=utf-8"));
    request.setRawHeader("Accept", "application/json");
    m_pendingReply = m_network->post(request, QJsonDocument(payload).toJson(QJsonDocument::Compact));
    connect(m_pendingReply, &QNetworkReply::finished, this, [this, reply = m_pendingReply] { handleReply(reply); });
    QTimer::singleShot(90000, m_pendingReply, [reply = m_pendingReply] {
        if (reply && reply->isRunning()) {
            reply->abort();
        }
    });
}

void PaperWorkspace::handleReply(QNetworkReply *reply)
{
    if (!reply || reply != m_pendingReply) {
        if (reply) {
            reply->deleteLater();
        }
        return;
    }

    const QByteArray responseData = reply->readAll();
    const auto networkError = reply->error();
    const int statusCode = reply->attribute(QNetworkRequest::HttpStatusCodeAttribute).toInt();
    if (networkError != QNetworkReply::NoError) {
        const QString detail = networkError == QNetworkReply::OperationCanceledError ? i18n("tiempo de espera agotado") : reply->errorString();
        setStatus(i18n("No responde la IA local en %1 (%2). Comprueba que el servicio esté iniciado y que el puerto 8082 sea accesible.", m_endpointEdit->text().trimmed(), detail), true);
        appendConversationMessage(i18n("Sistema"), i18n("Error de IA: %1", detail));
    } else if (statusCode >= 300) {
        const QString detail = QString::fromUtf8(responseData).trimmed().left(500);
        setStatus(i18n("La IA local respondió HTTP %1.", statusCode), true);
        appendConversationMessage(i18n("Sistema"), i18n("Error de IA HTTP %1: %2", statusCode, detail));
    } else {
        QJsonParseError parseError;
        const QJsonDocument document = QJsonDocument::fromJson(responseData, &parseError);
        QString answer;
        if (parseError.error == QJsonParseError::NoError && document.isObject()) {
            const QJsonObject root = document.object();
            const QJsonArray choices = root.value(QStringLiteral("choices")).toArray();
            if (!choices.isEmpty() && choices.first().isObject()) {
                const QJsonObject message = choices.first().toObject().value(QStringLiteral("message")).toObject();
                answer = contentFromMessage(message).trimmed();
                if (answer.isEmpty()) {
                    answer = firstString(message, {QStringLiteral("reasoning"), QStringLiteral("reasoning_content"), QStringLiteral("thought")}).trimmed();
                }
                if (!answer.isEmpty()) {
                    QJsonObject assistantMessage;
                    assistantMessage.insert(QStringLiteral("role"), QStringLiteral("assistant"));
                    assistantMessage.insert(QStringLiteral("content"), answer);
                    m_conversation.append(assistantMessage);
                    while (m_conversation.size() > maximumConversationMessages) {
                        m_conversation.removeFirst();
                    }
                }
            }
        }
        if (answer.isEmpty()) {
            setStatus(parseError.error == QJsonParseError::NoError ? i18n("La IA devolvió una respuesta sin contenido.") : i18n("La respuesta de la IA no es JSON válido: %1", parseError.errorString()), true);
            appendConversationMessage(i18n("Sistema"), i18n("La IA devolvió una respuesta sin contenido."));
        } else {
            setStatus(i18n("Respuesta recibida."));
            appendConversationMessage(i18n("IA local"), answer);
        }
    }

    if (m_pendingReply == reply) {
        m_pendingReply = nullptr;
    }
    reply->deleteLater();
    m_askButton->setEnabled(!m_promptEdit->toPlainText().trimmed().isEmpty() && !m_documentPath.isEmpty());
}

void PaperWorkspace::appendConversationMessage(const QString &role, const QString &content)
{
    if (!m_responseEdit->toPlainText().isEmpty()) {
        m_responseEdit->appendPlainText(QString());
    }
    m_responseEdit->appendPlainText(role + QLatin1String(":") + QLatin1Char('\n') + content);
    constexpr int maximumResponseCharacters = 24000;
    const QString output = m_responseEdit->toPlainText();
    if (output.size() > maximumResponseCharacters) {
        m_responseEdit->setPlainText(output.right(maximumResponseCharacters));
        m_responseEdit->moveCursor(QTextCursor::End);
    }
}

void PaperWorkspace::saveNotes()
{
    saveSession();
}

void PaperWorkspace::setStatus(const QString &message, bool error)
{
    m_aiStatus->setText(message);
    m_aiStatus->setStyleSheet(error ? QStringLiteral("font-weight: bold;") : QString());
}
