# Paper — Android Tablet PDF Reader & Stylus Annotation Engine

**Paper** is a fast, tablet-first PDF reader and handwriting/annotation app built with **Kotlin** and **Jetpack Compose**. It is engineered for academic researchers, engineers, and students who read papers, annotate textbooks, and write notes with stylus on Android tablets.

---

## 🌟 Key Features

### 1. 📚 Library & Document Management
- **Storage Access Framework (SAF):** Pick and open PDFs using standard `ACTION_OPEN_DOCUMENT` with `takePersistableUriPermission` so documents stay accessible across app restarts.
- **Smart Progress Tracking:** Displays `Page X of Y · Z%` progress along with an interactive progress bar.
- **Thumbnail Caching:** Generates and caches high-DPI thumbnails of Page 1.
- **State Restoration:** Automatically restores the last read page, zoom, and annotations on document re-open.

### 2. ⚡ Immediate Navigation
- **Zero-Latency Edge Navigation:** Tapping the left 18% turns to the previous page; tapping the right 18% turns to the next page.
- **Top Toolbar Controls:** Direct `‹` and `›` arrow buttons and `Page X / Y ⌄` dropdown indicator.
- **Floating Page Navigator Panel:**
  - Direct numeric page entry with `Go` action and `1 <= page <= pageCount` validation.
  - `-1` and `+1` increment/decrement buttons.
  - Interactive slider for rapid scrubbing across 500+ pages without UI stutter.

### 3. ✍️ Low-Latency Stylus & Ink Annotation Engine
- **Non-destructive Canvas Overlay:** Annotations are stored independently in **PDF page coordinate space** (0.0 to 1.0) rather than rasterized destructively into the PDF bitmap. Annotations scale perfectly with zoom, device rotation, and screen resolution.
- **Stylus Palm Rejection:** Rejects touch input when a stylus pen or eraser (`MotionEvent.TOOL_TYPE_STYLUS`) is active.
- **Pressure Sensitivity:** Dynamic stroke width adjustment based on hardware pressure.
- **Curve Smoothing:** Quadratic Bézier curve interpolation for smooth, natural handwriting.
- **Tools Included:**
  - **Pen:** Freehand handwriting with configurable stroke width (2–4 dp base).
  - **Highlighter:** Translucent overlay with blend mode (`alpha ~ 0.35`).
  - **Text Marker / Underline:** Direct markup tools.
  - **Object Eraser:** Spatial segment proximity testing that removes intersecting strokes as unified objects.
  - **Floating Color Popover:** 8 primary observed colors (`#000000`, `#2F6BFF`, `#E53935`, `#2E8B45`, `#F4C430`, `#F57C00`, `#EC407A`, `#7E3FF2`) and a dynamically updated recent colors row.
  - **Undo / Redo:** Complete historical state tracking for stroke additions, batch deletions, and eraser modifications.
  - **✓ Done Button:** Persists annotations to Room DB and returns to distraction-free reading mode.

### 4. 🚀 High-Performance PDF Engine
- **Android `PdfRenderer` Integration:** Thread-safe native rendering engine with parcel file descriptor handling.
- **LRU Bitmap Cache:** Memory-conscious bitmap cache (64MB–256MB dynamically bounded to 20% JVM heap).
- **Background Prefetching:** Pre-renders `currentPage ± 1` on `Dispatchers.IO` for `< 50ms` cached page turns.
- **Pinch-to-Zoom & Pan:** 1.0x to 5.0x zoom range in Full Page mode.

---

## 🏗️ Architecture & Package Structure

```text
com.pablo.paper
├── MainActivity.kt               # Entry Activity with PDF VIEW intent filter
├── PaperApp.kt                   # NavHost and top-level navigation routes
├── PaperApplication.kt           # Application class & DI singleton providers
│
├── domain.model
│   ├── Annotation.kt             # Domain annotation entity
│   ├── InkPoint.kt               # PDF-normalized point (x, y, pressure, time)
│   ├── InkStroke.kt              # Stroke properties (points, color, width, opacity)
│   ├── InkTool.kt                # Tool enums (PEN, HIGHLIGHTER, TEXT_HIGHLIGHT, etc.)
│   ├── ColorPalette.kt           # Standard & recent palette tokens
│   ├── Document.kt               # Document domain representation & progress
│   ├── ReaderMode.kt             # READING, INK, MARKDOWN, ASSISTANT
│   ├── ReaderState.kt            # Immutable state & MVI actions
│   └── InkState.kt               # Real-time drawing state & actions
│
├── data
│   ├── db
│   │   ├── PaperDatabase.kt      # Room database builder
│   │   ├── DocumentDao.kt        # Document query & progress DAO
│   │   ├── AnnotationDao.kt      # Page annotations query & persistence DAO
│   │   ├── DocumentEntity.kt     # Room Document table
│   │   ├── AnnotationEntity.kt   # Room Annotation table
│   │   └── Converters.kt         # Type converters for JSON point lists
│   └── repository
│       ├── DocumentRepository.kt # SAF permissions, metadata & thumbnail generation
│       ├── AnnotationRepository.kt# Annotation CRUD and batch persistence
│       └── PreferencesRepository.kt# DataStore preferences for tools & colors
│
├── pdf
│   ├── PdfEngine.kt              # Engine abstraction interface
│   ├── NativePdfEngine.kt        # Android PdfRenderer implementation
│   ├── PdfBitmapCache.kt         # Thread-safe LRU bitmap cache
│   └── CoordinateTransformer.kt  # Viewport <-> PDF coordinate mappings
│
├── ink
│   ├── InkController.kt          # Drawing state, palm rejection & touch coordinator
│   ├── StrokeSmoother.kt         # Bézier curve smoothing
│   ├── EraserEngine.kt           # Object erasing & segment intersection math
│   └── UndoRedoManager.kt        # Undo/Redo stack manager
│
└── ui
    ├── theme                     # Colors, typography, shapes (iPadOS 26 tablet style)
    ├── library
    │   ├── LibraryScreen.kt      # Adaptive tablet grid view
    │   ├── LibraryViewModel.kt   # Document import & selection state
    │   ├── LibraryHeader.kt      # "Paper", count, "Open documents" button
    │   ├── DocumentCard.kt       # Card with thumbnail, title, progress bar
    │   └── EmptyLibraryView.kt   # Empty slate onboarding view
    └── reader
        ├── ReaderScreen.kt       # 6-layer UI orchestrator
        ├── ReaderViewModel.kt    # Navigation & reader state reducer
        ├── ReaderToolbar.kt      # Top toolbar with buttons & indicators
        ├── PdfViewport.kt        # High-DPI canvas viewer with zoom/pan
        ├── EdgeNavigationOverlay.kt# 18% left/right touch zones
        ├── PageNavigator.kt      # Bottom floating scrubber & input panel
        ├── ViewModeDropdown.kt   # View mode popup (Full page, Fit width)
        ├── SidePanels.kt         # Markdown & Assistant side panels
        └── ink
            ├── InkToolbar.kt     # Annotation toolbar with Done button
            ├── InkCanvas.kt      # Direct hardware-accelerated drawing overlay
            └── ColorPickerPopover.kt# Tool preview, 8-color palette & recent colors
```

---

## 🧪 Unit Tests

The test suite covers:
- **`CoordinateTransformerTest`**: Bounds calculation, viewport aspect ratios, screen-to-PDF roundtrips, and left/right 18% edge tap detection.
- **`UndoRedoManagerTest`**: Stroke addition, batch erasure, stack inversion, and redo invalidation.
- **`EraserEngineTest`**: Point-to-segment geometric distance calculations and stroke proximity hit-testing.
- **`DocumentProgressTest`**: Progress percentage and clamping calculations.
- **`ReaderViewModelTest`**: State transitions, mode switching, tool selection, and page navigator visibility.

---

## 🚀 Building & Running

1. Open the directory `C:\Users\pablo\.gemini\antigravity\scratch\paper` in **Android Studio Hedgehog / Jellyfish / Ladybug**.
2. Sync Gradle with project files.
3. Run on an Android tablet emulator (e.g. Pixel Tablet API 34) or physical tablet.
