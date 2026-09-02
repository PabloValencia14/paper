# Integración de Paper en Okular

Esta rama añade una capa de trabajo de Paper sobre el lector de Okular sin
alterar el motor de documentos ni modificar el PDF original.

## Qué aporta

- **Sesión por documento**: al cerrar un PDF se guarda su página y viewport en
  `<nombre>.paper.json`, junto a las notas Markdown.
- **Compatibilidad con Paper**: al guardar se conservan también los campos que
  Paper ya tuviera en ese sidecar (por ejemplo, anotaciones, zoom o modo de
  lectura), aunque Okular no los edite todavía.
- **Asistente IA local**: el panel `Paper workspace` se puede abrir desde
  `Tools` o con `Ctrl+Alt+P`. El endpoint predeterminado es
  `http://100.94.0.92:8082/v1/chat/completions` y el modelo predeterminado es
  `auto`.
- **Contexto acotado**: solo se envían la selección, el texto de la página
  activa y hasta 3.000 caracteres de notas. El PDF completo no se sube al
  proxy.

## Seguridad y datos

Las notas y el estado de lectura se escriben mediante `QSaveFile`, de forma
atómica, y no se incrustan en el documento. El asistente solo hace una
petición cuando se pulsa `Preguntar`; no hay procesos en segundo plano ni
claves incrustadas en el código. El endpoint se puede cambiar en el propio
panel y se guarda como preferencia local de Okular.

## Compilación

La integración forma parte del objetivo `okularpart` y añade el componente
`Qt6::Network`. Requiere el entorno de desarrollo habitual de KDE/Qt 6
(ECM, KF6, CMake y Ninja); la configuración de CMake debe ejecutarse desde
KDE Craft o una instalación equivalente.

La app Android de Paper no se modifica con esta rama. Las funciones específicas
de lápiz de Android (rechazo de palma, presión, borrado de objetos y
reconocimiento de formas) siguen perteneciendo a esa app; portarlas a las
anotaciones persistentes de Okular requiere una fase independiente.
