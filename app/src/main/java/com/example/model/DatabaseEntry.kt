package com.example.model

import java.util.Locale

data class DatabaseEntry(
    val name: String,
    val id: String = "",
    val description: String = "",
    val stats: String = "",
    val tags: List<String> = emptyList(),
    val extraFields: Map<String, String> = emptyMap()
) {
    val displayName: String
        get() {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return "Untitled"
            return trimmed.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }

    val tagsString: String
        get() = tags.joinToString(", ")

    fun getFieldValue(columnName: String): String {
        return when (columnName.lowercase().trim()) {
            "name", "names" -> name
            "id" -> id
            "description", "desc" -> description
            "stats", "stat" -> stats
            "tags", "tag" -> tagsString
            else -> extraFields[columnName] ?: ""
        }
    }
}
