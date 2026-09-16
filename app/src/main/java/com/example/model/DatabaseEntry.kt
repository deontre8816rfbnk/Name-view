package com.example.model

import java.util.Locale

/**
 * Unified row in the single .md table.
 * Type = Player | Manager | Club | Nation
 * Type-specific data lives in extraFields + stats string.
 */
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

    /** Entity type stored in extraFields["Type"]. Defaults to Player for legacy rows. */
    val entityType: EntityType
        get() {
            val raw = extraFields["Type"] ?: extraFields["type"] ?: "Player"
            return EntityType.from(raw)
        }

    val position: String
        get() = extraFields["Position"] ?: extraFields["POSITION"] ?: ""

    val nationality: String
        get() = extraFields["Nationality"] ?: extraFields["nationality"] ?: ""

    val club: String
        get() = extraFields["Club"] ?: extraFields["club"] ?: ""

    val date: String
        get() = extraFields["Date"] ?: extraFields["date"] ?: ""

    val playstyle: String
        get() = extraFields["Playstyle"] ?: extraFields["playstyle"] ?: ""

    val secondaryPositions: List<String>
        get() {
            val raw = extraFields["SecondaryPositions"] ?: extraFields["secondaryPositions"] ?: ""
            if (raw.isBlank()) return emptyList()
            return raw.split(",", ";").map { it.trim() }.filter { it.isNotBlank() }
        }

    val skills: List<String>
        get() {
            val raw = extraFields["Skills"] ?: extraFields["skills"] ?: ""
            if (raw.isBlank()) return emptyList()
            return raw.split(",", ";").map { it.trim() }.filter { it.isNotBlank() }
        }

    val overall: Float
        get() {
            val fromExtra = extraFields["Overall"]?.toFloatOrNull()
            if (fromExtra != null) return fromExtra
            return extractStat("OVERALL")
        }

    fun extractStat(key: String): Float {
        val upper = key.uppercase()
        val fromStats = stats.split(",", ";")
            .map { it.trim() }
            .firstOrNull { it.uppercase().startsWith("$upper:") }
            ?.substringAfter(":")
            ?.trim()
            ?.toFloatOrNull()
        if (fromStats != null) return fromStats
        return extraFields[key]?.toFloatOrNull()
            ?: extraFields[key.uppercase()]?.toFloatOrNull()
            ?: 0f
    }

    fun getFieldValue(columnName: String): String {
        return when (columnName.lowercase().trim()) {
            "name", "names" -> name
            "id" -> id
            "description", "desc" -> description
            "stats", "stat" -> stats
            "tags", "tag" -> tagsString
            else -> extraFields[columnName]
                ?: extraFields.entries.firstOrNull { it.key.equals(columnName, ignoreCase = true) }?.value
                ?: ""
        }
    }

    fun withExtra(key: String, value: String): DatabaseEntry {
        if (value.isBlank()) {
            return copy(extraFields = extraFields - key)
        }
        return copy(extraFields = extraFields + (key to value))
    }
}

enum class EntityType {
    Player, Manager, Club, Nation;

    companion object {
        fun from(raw: String): EntityType {
            return when (raw.trim().lowercase()) {
                "manager", "coach" -> Manager
                "club" -> Club
                "nation" -> Nation
                else -> Player
            }
        }
    }
}
