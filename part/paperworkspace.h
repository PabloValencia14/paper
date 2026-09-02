/*
    SPDX-FileCopyrightText: 2026 Pablo Valencia

    SPDX-License-Identifier: GPL-2.0-or-later
*/

#ifndef OKULAR_PAPERWORKSPACE_H
#define OKULAR_PAPERWORKSPACE_H

#include <QJsonArray>
#include <QJsonObject>
#include <QUrl>
#include <QWidget>

class QLabel;
class QLineEdit;
class QNetworkAccessManager;
class QNetworkReply;
class QPlainTextEdit;
class QPushButton;

namespace Okular
{
class Document;
}

class PageView;

/**
 * Paper's document-side workspace for notes and a local AI assistant.
 *
 * The workspace deliberately stores its state in a sidecar next to the
 * document. It never modifies the PDF and only sends the current page or text
 * selection to the configured local endpoint when the user asks a question.
 */
class PaperWorkspace : public QWidget
{
    Q_OBJECT

public:
    explicit PaperWorkspace(QWidget *parent = nullptr);
    ~PaperWorkspace() override;

    void setDocument(Okular::Document *document, PageView *pageView);
    void setDocumentUrl(const QUrl &url);
    void loadSession();
    bool saveSession();
    void clearDocument();

private Q_SLOTS:
    void sendPrompt();
    void saveNotes();

private:
    QString sidecarPath() const;
    QString documentContext() const;
    void handleReply(QNetworkReply *reply);
    void setStatus(const QString &message, bool error = false);
    void appendConversationMessage(const QString &role, const QString &content);

    Okular::Document *m_document = nullptr;
    PageView *m_pageView = nullptr;
    QNetworkAccessManager *m_network = nullptr;
    QNetworkReply *m_pendingReply = nullptr;

    QUrl m_documentUrl;
    QString m_documentPath;
    bool m_sessionLoaded = false;
    bool m_sessionDirty = false;
    QJsonObject m_sidecarData;

    QLabel *m_documentLabel = nullptr;
    QLabel *m_sessionStatus = nullptr;
    QPlainTextEdit *m_notesEdit = nullptr;
    QPushButton *m_saveButton = nullptr;

    QLineEdit *m_endpointEdit = nullptr;
    QLineEdit *m_modelEdit = nullptr;
    QPlainTextEdit *m_promptEdit = nullptr;
    QPlainTextEdit *m_responseEdit = nullptr;
    QPushButton *m_askButton = nullptr;
    QLabel *m_aiStatus = nullptr;

    QJsonArray m_conversation;
};

#endif
