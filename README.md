# Paper

Paper es un lector de PDF centrado en la lectura activa: abrir un documento, orientarse en él, marcar lo importante y conservar notas sin convertir el archivo original en un experimento irreversible.

El repositorio contiene dos clientes:

- **Android (`app`)**: la aplicación táctil para tablet y lápiz.
- **Windows (`desktop`)**: un lector de escritorio nativo, pensado para trabajar con teclado, ratón y paneles laterales.

La versión de Windows se ha reconstruido alrededor de un único motor PDFBox. La vista, la navegación y las herramientas trabajan sobre el mismo estado del documento, en lugar de mantener un visor separado de la lógica de Paper.

## Para empezar en Windows

### Usar el instalador

Si solo quieres usar Paper, descarga `PaperDesktop-1.0.0.exe` desde la página de [Releases](https://github.com/PabloValencia14/paper/releases). Si has clonado el código y lo has compilado, el instalador generado para esta versión se encuentra en:

```text
desktop/build/compose/binaries/main/exe/PaperDesktop-1.0.0.exe
```

Ejecuta el instalador y abre **Paper** desde el menú Inicio. También se puede abrir un PDF arrastrándolo a la ventana o mediante **Archivo → Abrir PDF**.

### Flujo de lectura

1. Abre un PDF.
2. Usa la barra superior para elegir **mano**, **selección de texto**, **resaltado** o **lápiz**.
3. Cambia entre **página**, **desplazamiento continuo** y **pliego** desde la sección de vista.
4. Abre el panel izquierdo para páginas, índice y marcas.
5. Abre el panel derecho para notas, información del documento o el asistente de IA.
6. Guarda la sesión con `Ctrl+S` o deja que Paper la guarde al cerrar.

Paper guarda el trabajo de lectura en un archivo paralelo junto al PDF:

```text
mi-documento.pdf.paper.json
```

Ese archivo contiene posición, zoom, modo de lectura, rotación, anotaciones y notas. El PDF original no se modifica. Para conservar una sesión hay que copiar el PDF y su archivo `.paper.json`.

### Atajos

| Atajo | Acción |
|---|---|
| `Ctrl+O` | Abrir PDF |
| `Ctrl+S` | Guardar sesión |
| `Ctrl+P` | Imprimir |
| `Ctrl+F` | Buscar en el documento |
| `Ctrl+K` | Abrir/cerrar el asistente de IA |
| `Ctrl+Z` / `Ctrl+Y` | Deshacer / rehacer |
| `Ctrl+C` | Copiar la selección |
| `Ctrl+W` | Cerrar el documento activo |
| `Esc` | Cerrar búsqueda, diálogo o panel abierto |

## Asistente de IA

Paper no incluye un modelo remoto. El cliente de Windows se conecta al proxy local configurado en:

```text
http://100.94.0.92:8082/v1/chat/completions
```

Si el proxy no está encendido o la máquina no puede alcanzar esa dirección, el asistente mostrará un error de conexión. Cuando funciona, Paper envía al proxy la pregunta y el contexto de texto necesario para responder; no se envía automáticamente el PDF completo a un servicio externo.

## Compilar y probar

Requisitos para compilar el cliente de Windows:

- Windows 10/11 de 64 bits.
- JDK 17.
- Gradle Wrapper incluido en el repositorio (no hace falta instalar Gradle aparte).

Desde la raíz del proyecto:

```powershell
.\gradlew.bat :desktop:test
.\gradlew.bat :desktop:run
.\gradlew.bat :desktop:run --args 'C:\ruta\al\documento.pdf'
.\gradlew.bat :desktop:packageExe
```

El último comando genera el instalador en `desktop/build/compose/binaries/main/exe/`.

Para mantener o compilar la aplicación Android se utiliza el módulo `app` desde Android Studio. La instalación en una tablet requiere que el dispositivo esté autorizado en ADB; el cliente Windows no depende de ADB.

## Organización del código

```text
app/       Aplicación Android para tablet y lápiz
desktop/   Cliente Windows Compose Desktop
  ai/      Cliente del proxy local de IA
  model/   Herramientas, modos y modelos de documento
  pdf/     Motor PDFBox y caché de bitmaps
  state/   Estado de pestañas, sesiones y workspace
  ui/      Ventana, barra de herramientas, docks y lienzo PDF
```

En Windows, la carga y extracción pesada del PDF se ejecutan fuera del hilo de interfaz. La caché de páginas está limitada para evitar que un documento grande consuma toda la memoria disponible.

## Estado del port de Windows

La versión actual cubre lectura, navegación, búsqueda, selección, resaltado, escritura a mano, notas, índice, miniaturas, modos de página, deshacer/rehacer, impresión y persistencia de sesión.

Las funciones de firma digital, marcas de agua y edición destructiva del PDF no se anuncian como disponibles porque no tenían una implementación fiable en el port original. Se mantienen fuera de la interfaz hasta que puedan implementarse y verificarse correctamente.

## Historial de esta versión

- `98125e5`: instantánea recuperable del port inicial de Windows.
- `25b0e02`: reconstrucción del lector, interfaz y persistencia de sesión.

El proyecto está en desarrollo. Las pruebas automatizadas actuales cubren la pila de anotaciones y la restauración de sesiones; antes de distribuir una versión estable conviene añadir pruebas de interacción de la interfaz y una validación completa del proxy de IA.
