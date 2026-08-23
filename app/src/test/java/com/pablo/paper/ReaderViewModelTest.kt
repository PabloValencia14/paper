package com.pablo.paper

import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.pablo.paper.data.repository.AnnotationRepository
import com.pablo.paper.data.repository.DocumentRepository
import com.pablo.paper.data.repository.PreferencesRepository
import com.pablo.paper.domain.model.ColorPalette
import com.pablo.paper.domain.model.Document
import com.pablo.paper.domain.model.InkTool
import com.pablo.paper.domain.model.ReaderAction
import com.pablo.paper.domain.model.ReaderMode
import com.pablo.paper.domain.model.ViewMode
import com.pablo.paper.pdf.PdfEngine
import com.pablo.paper.ui.reader.ReaderViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val documentRepository: DocumentRepository = mockk(relaxed = true)
    private val annotationRepository: AnnotationRepository = mockk(relaxed = true)
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val pdfEngine: PdfEngine = mockk(relaxed = true)

    private val sampleDoc = Document(
        id = "doc123",
        uri = "content://com.android.providers.media.documents/document/123",
        name = "DeepLearningPaper.pdf",
        pageCount = 43,
        currentPage = 24,
        progress = 24f / 43f
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Uri::class)
        val mockUri: Uri = mockk(relaxed = true)
        every { Uri.parse(any()) } returns mockUri

        every { preferencesRepository.selectedInkToolFlow } returns flowOf(InkTool.PEN)
        every { preferencesRepository.selectedColorFlow } returns flowOf(ColorPalette.BLACK)
        every { preferencesRepository.selectedHighlighterColorFlow } returns flowOf(ColorPalette.YELLOW)
        every { preferencesRepository.recentColorsFlow } returns flowOf(ColorPalette.getInitialRecentColors())
        every { preferencesRepository.viewModeFlow } returns flowOf(ViewMode.FULL_PAGE)
        coEvery { documentRepository.getDocumentById("doc123") } returns sampleDoc
        coEvery { annotationRepository.getAnnotationsForPage(any(), any()) } returns emptyList()
        coEvery { pdfEngine.open(any()) } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun enterInkMode_and_exitInkMode_updatesStateProperly() = runTest(testDispatcher) {
        val viewModel = ReaderViewModel(
            documentId = "doc123",
            documentRepository = documentRepository,
            annotationRepository = annotationRepository,
            preferencesRepository = preferencesRepository,
            context = context,
            pdfEngine = pdfEngine
        )
        advanceUntilIdle()

        assertThat(viewModel.state.value.mode).isEqualTo(ReaderMode.READING)

        // Enter ink mode
        viewModel.onAction(ReaderAction.EnterInkMode)
        assertThat(viewModel.state.value.mode).isEqualTo(ReaderMode.INK)

        // Select Highlighter tool
        viewModel.onAction(ReaderAction.SelectInkTool(InkTool.HIGHLIGHTER))
        assertThat(viewModel.state.value.activeInkTool).isEqualTo(InkTool.HIGHLIGHTER)

        // Exit ink mode
        viewModel.onAction(ReaderAction.ExitInkMode)
        assertThat(viewModel.state.value.mode).isEqualTo(ReaderMode.READING)
    }

    @Test
    fun togglePageNavigator_updatesVisibility() = runTest(testDispatcher) {
        val viewModel = ReaderViewModel(
            documentId = "doc123",
            documentRepository = documentRepository,
            annotationRepository = annotationRepository,
            preferencesRepository = preferencesRepository,
            context = context,
            pdfEngine = pdfEngine
        )
        advanceUntilIdle()

        assertThat(viewModel.state.value.showPageNavigator).isFalse()

        viewModel.onAction(ReaderAction.TogglePageNavigator)
        assertThat(viewModel.state.value.showPageNavigator).isTrue()

        viewModel.onAction(ReaderAction.TogglePageNavigator)
        assertThat(viewModel.state.value.showPageNavigator).isFalse()
    }
}
