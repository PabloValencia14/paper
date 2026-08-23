package com.pablo.paper.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pablo.paper.domain.model.ColorPalette
import com.pablo.paper.domain.model.InkTool
import com.pablo.paper.domain.model.ViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "paper_preferences")

class PreferencesRepository(private val context: Context) {

    private val gson = Gson()

    private object Keys {
        val INK_TOOL = stringPreferencesKey("ink_tool")
        val SELECTED_COLOR = longPreferencesKey("selected_color")
        val SELECTED_HIGHLIGHTER_COLOR = longPreferencesKey("selected_highlighter_color")
        val PEN_WIDTH = floatPreferencesKey("pen_width")
        val HIGHLIGHTER_WIDTH = floatPreferencesKey("highlighter_width")
        val RECENT_COLORS_JSON = stringPreferencesKey("recent_colors_json")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key")
        val SELECTED_AI_MODEL = stringPreferencesKey("selected_ai_model")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val STYLUS_PRIMARY_ACTION = stringPreferencesKey("stylus_primary_action")
        val STYLUS_SECONDARY_ACTION = stringPreferencesKey("stylus_secondary_action")
        val PAPER_COLOR = stringPreferencesKey("paper_color")
        val PAPER_TEXTURE = stringPreferencesKey("paper_texture")
        val PAPER_TEXTURE_POINTS = floatPreferencesKey("paper_texture_points")
        val IS_SEAMLESS_CANVAS = androidx.datastore.preferences.core.booleanPreferencesKey("is_seamless_canvas")
    }

    val paperColorFlow: Flow<com.pablo.paper.domain.model.PaperColor> = context.dataStore.data.map { prefs ->
        prefs[Keys.PAPER_COLOR]?.let {
            try { enumValueOf<com.pablo.paper.domain.model.PaperColor>(it) } catch (e: Exception) { com.pablo.paper.domain.model.PaperColor.WHITE }
        } ?: com.pablo.paper.domain.model.PaperColor.WHITE
    }

    val paperTextureFlow: Flow<com.pablo.paper.domain.model.PaperTexture> = context.dataStore.data.map { prefs ->
        prefs[Keys.PAPER_TEXTURE]?.let {
            try { enumValueOf<com.pablo.paper.domain.model.PaperTexture>(it) } catch (e: Exception) { com.pablo.paper.domain.model.PaperTexture.SMOOTH }
        } ?: com.pablo.paper.domain.model.PaperTexture.SMOOTH
    }

    val paperTexturePointsFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.PAPER_TEXTURE_POINTS] ?: 24f
    }

    val isSeamlessCanvasFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_SEAMLESS_CANVAS] ?: true
    }

    val themeModeFlow: Flow<com.pablo.paper.domain.model.AppThemeMode> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE]?.let {
            try { enumValueOf<com.pablo.paper.domain.model.AppThemeMode>(it) } catch (e: Exception) { com.pablo.paper.domain.model.AppThemeMode.SYSTEM }
        } ?: com.pablo.paper.domain.model.AppThemeMode.SYSTEM
    }

    val stylusPrimaryButtonActionFlow: Flow<com.pablo.paper.domain.model.StylusButtonAction> = context.dataStore.data.map { prefs ->
        prefs[Keys.STYLUS_PRIMARY_ACTION]?.let {
            try { enumValueOf<com.pablo.paper.domain.model.StylusButtonAction>(it) } catch (e: Exception) { com.pablo.paper.domain.model.StylusButtonAction.TEMPORARY_ERASER }
        } ?: com.pablo.paper.domain.model.StylusButtonAction.TEMPORARY_ERASER
    }

    val stylusSecondaryButtonActionFlow: Flow<com.pablo.paper.domain.model.StylusButtonAction> = context.dataStore.data.map { prefs ->
        prefs[Keys.STYLUS_SECONDARY_ACTION]?.let {
            try { enumValueOf<com.pablo.paper.domain.model.StylusButtonAction>(it) } catch (e: Exception) { com.pablo.paper.domain.model.StylusButtonAction.SWITCH_TO_HIGHLIGHTER }
        } ?: com.pablo.paper.domain.model.StylusButtonAction.SWITCH_TO_HIGHLIGHTER
    }

    val openRouterApiKeyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.OPENROUTER_API_KEY] ?: ""
    }

    val selectedAiModelFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_AI_MODEL] ?: com.pablo.paper.ai.OpenRouterModels.DEFAULT_MODEL
    }

    val aiProviderFlow: Flow<com.pablo.paper.ai.AiProvider> = context.dataStore.data.map { prefs ->
        prefs[Keys.AI_PROVIDER]?.let {
            try { enumValueOf<com.pablo.paper.ai.AiProvider>(it) } catch (e: Exception) { com.pablo.paper.ai.AiProvider.GOOGLE_GEMINI }
        } ?: com.pablo.paper.ai.AiProvider.GOOGLE_GEMINI
    }

    suspend fun saveAiProvider(provider: com.pablo.paper.ai.AiProvider) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AI_PROVIDER] = provider.name
        }
    }

    val selectedInkToolFlow: Flow<InkTool> = context.dataStore.data.map { prefs ->
        prefs[Keys.INK_TOOL]?.let {
            try { enumValueOf<InkTool>(it) } catch (e: Exception) { InkTool.PEN }
        } ?: InkTool.PEN
    }

    val selectedColorFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_COLOR] ?: ColorPalette.BLACK
    }

    val selectedHighlighterColorFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_HIGHLIGHTER_COLOR] ?: ColorPalette.YELLOW
    }

    val penWidthFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.PEN_WIDTH] ?: 2.5f
    }

    val highlighterWidthFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.HIGHLIGHTER_WIDTH] ?: 14f
    }

    val recentColorsFlow: Flow<List<Long>> = context.dataStore.data.map { prefs ->
        val json = prefs[Keys.RECENT_COLORS_JSON]
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<Long>>() {}.type
            gson.fromJson<List<Long>>(json, type)
        } else {
            ColorPalette.getInitialRecentColors()
        }
    }

    val viewModeFlow: Flow<ViewMode> = context.dataStore.data.map { prefs ->
        prefs[Keys.VIEW_MODE]?.let {
            try { enumValueOf<ViewMode>(it) } catch (e: Exception) { ViewMode.FULL_PAGE }
        } ?: ViewMode.FULL_PAGE
    }

    suspend fun saveSelectedInkTool(tool: InkTool) {
        context.dataStore.edit { prefs ->
            prefs[Keys.INK_TOOL] = tool.name
        }
    }

    suspend fun saveSelectedColor(color: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_COLOR] = color
        }
        recordRecentColor(color)
    }

    suspend fun saveSelectedHighlighterColor(color: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_HIGHLIGHTER_COLOR] = color
        }
        recordRecentColor(color)
    }

    suspend fun savePenWidth(width: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PEN_WIDTH] = width
        }
    }

    suspend fun saveHighlighterWidth(width: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HIGHLIGHTER_WIDTH] = width
        }
    }

    suspend fun saveViewMode(viewMode: ViewMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VIEW_MODE] = viewMode.name
        }
    }

    fun getDocumentNotesFlow(documentId: String): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[stringPreferencesKey("doc_notes_$documentId")] ?: ""
    }

    suspend fun saveOpenRouterApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OPENROUTER_API_KEY] = apiKey
        }
    }

    suspend fun saveSelectedAiModel(modelId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_AI_MODEL] = modelId
        }
    }

    suspend fun saveThemeMode(mode: com.pablo.paper.domain.model.AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun savePaperColor(paperColor: com.pablo.paper.domain.model.PaperColor) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PAPER_COLOR] = paperColor.name
        }
    }

    suspend fun savePaperTexture(paperTexture: com.pablo.paper.domain.model.PaperTexture) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PAPER_TEXTURE] = paperTexture.name
        }
    }

    suspend fun savePaperTexturePoints(points: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PAPER_TEXTURE_POINTS] = points
        }
    }

    suspend fun saveIsSeamlessCanvas(isSeamless: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_SEAMLESS_CANVAS] = isSeamless
        }
    }

    suspend fun saveStylusPrimaryAction(action: com.pablo.paper.domain.model.StylusButtonAction) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STYLUS_PRIMARY_ACTION] = action.name
        }
    }

    suspend fun saveStylusSecondaryAction(action: com.pablo.paper.domain.model.StylusButtonAction) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STYLUS_SECONDARY_ACTION] = action.name
        }
    }

    suspend fun saveDocumentNotes(documentId: String, notes: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("doc_notes_$documentId")] = notes
        }
    }

    fun getDocumentOutlineFlow(documentId: String): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[stringPreferencesKey("doc_outline_$documentId")] ?: ""
    }

    suspend fun saveDocumentOutline(documentId: String, outlineJson: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("doc_outline_$documentId")] = outlineJson
        }
    }

    private suspend fun recordRecentColor(color: Long) {
        context.dataStore.edit { prefs ->
            val json = prefs[Keys.RECENT_COLORS_JSON]
            val currentList: MutableList<Long> = if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<Long>>() {}.type
                gson.fromJson<List<Long>>(json, type).toMutableList()
            } else {
                ColorPalette.getInitialRecentColors().toMutableList()
            }

            currentList.remove(color)
            currentList.add(0, color)
            val trimmed = currentList.take(6)
            prefs[Keys.RECENT_COLORS_JSON] = gson.toJson(trimmed)
        }
    }
}
