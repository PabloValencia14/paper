package com.pablo.paper.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pablo.paper.domain.model.AnnotationType
import com.pablo.paper.domain.model.InkPoint
import com.pablo.paper.domain.model.InkTool

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromAnnotationType(type: AnnotationType): String = type.name

    @TypeConverter
    fun toAnnotationType(value: String): AnnotationType = enumValueOf(value)

    @TypeConverter
    fun fromInkTool(tool: InkTool): String = tool.name

    @TypeConverter
    fun toInkTool(value: String): InkTool = enumValueOf(value)

    @TypeConverter
    fun fromInkPointsList(points: List<InkPoint>?): String? {
        if (points == null) return null
        return gson.toJson(points)
    }

    @TypeConverter
    fun toInkPointsList(json: String?): List<InkPoint>? {
        if (json.isNullOrEmpty()) return null
        val type = object : TypeToken<List<InkPoint>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromHighlightRects(rects: List<FloatArray>?): String? {
        if (rects == null) return null
        return gson.toJson(rects)
    }

    @TypeConverter
    fun toHighlightRects(json: String?): List<FloatArray>? {
        if (json.isNullOrEmpty()) return null
        val type = object : TypeToken<List<FloatArray>>() {}.type
        return gson.fromJson(json, type)
    }
}
