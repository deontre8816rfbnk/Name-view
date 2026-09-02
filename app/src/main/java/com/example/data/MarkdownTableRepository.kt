package com.example.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.example.model.DatabaseEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarkdownTableRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("main_database_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "MarkdownRepo"
        private const val KEY_LINKED_URI = "key_linked_md_uri"
    }

    fun getLinkedUri(): Uri? {
        val uriString = prefs.getString(KEY_LINKED_URI, null) ?: return null
        return try {
            Uri.parse(uriString)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing saved URI", e)
            null
        }
    }

    fun saveLinkedUri(uri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            Log.w(TAG, "Could not take persistable URI permission: ${e.message}")
        }
        prefs.edit().putString(KEY_LINKED_URI, uri.toString()).apply()
    }

    fun clearLinkedUri() {
        prefs.edit().remove(KEY_LINKED_URI).apply()
    }

    suspend fun loadEntries(uri: Uri): ParseResult = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                it.readText()
            } ?: return@withContext ParseResult.Error("Could not open file stream")

            val parsed = parseMarkdown(content)
            ParseResult.Success(
                entries = parsed.entries,
                columns = parsed.columns,
                preamble = parsed.preamble,
                postamble = parsed.postamble,
                rawContent = content
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load entries from URI: $uri", e)
            ParseResult.Error(e.message ?: "Failed to read markdown file")
        }
    }

    suspend fun saveEntries(
        uri: Uri,
        entries: List<DatabaseEntry>,
        currentColumns: List<String> = emptyList(),
        preamble: String = "",
        postamble: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Determine all required columns
            val finalColumns = determineColumns(currentColumns, entries)
            val markdown = serializeMarkdown(finalColumns, entries, preamble, postamble)

            context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use {
                it.write(markdown)
                it.flush()
            } ?: return@withContext false

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save entries to URI: $uri", e)
            false
        }
    }

    private fun determineColumns(existing: List<String>, entries: List<DatabaseEntry>): List<String> {
        val columnList = mutableListOf<String>()

        // Ensure "Names" is always the first column
        val hasExistingNameCol = existing.firstOrNull { it.equals("names", ignoreCase = true) || it.equals("name", ignoreCase = true) }
        val nameColName = hasExistingNameCol ?: "Names"
        columnList.add(nameColName)

        // Add other existing columns
        for (col in existing) {
            if (!col.equals(nameColName, ignoreCase = true) && !columnList.any { it.equals(col, ignoreCase = true) }) {
                columnList.add(col)
            }
        }

        // Check if any entry uses standard attributes not yet in columns
        val hasId = entries.any { it.id.isNotBlank() }
        val hasDesc = entries.any { it.description.isNotBlank() }
        val hasStats = entries.any { it.stats.isNotBlank() }
        val hasTags = entries.any { it.tags.isNotEmpty() }

        if (hasId && !columnList.any { it.equals("id", ignoreCase = true) }) {
            columnList.add("ID")
        }
        if (hasDesc && !columnList.any { it.equals("description", ignoreCase = true) || it.equals("desc", ignoreCase = true) }) {
            columnList.add("Description")
        }
        if (hasStats && !columnList.any { it.equals("stats", ignoreCase = true) || it.equals("stat", ignoreCase = true) }) {
            columnList.add("Stats")
        }
        if (hasTags && !columnList.any { it.equals("tags", ignoreCase = true) || it.equals("tag", ignoreCase = true) }) {
            columnList.add("Tags")
        }

        // Check for any extra custom fields
        for (entry in entries) {
            for (key in entry.extraFields.keys) {
                if (key.isNotBlank() && !columnList.any { it.equals(key, ignoreCase = true) }) {
                    columnList.add(key)
                }
            }
        }

        return columnList
    }

    fun parseMarkdown(content: String): ParsedMarkdown {
        val lines = content.lines()
        val preambleLines = mutableListOf<String>()
        val tableLines = mutableListOf<String>()
        val postambleLines = mutableListOf<String>()

        var tableStarted = false
        var tableEnded = false

        for (i in lines.indices) {
            val line = lines[i]
            val isTableCandidate = isTableLine(line)

            if (!tableStarted) {
                if (isTableCandidate && i + 1 < lines.size && isSeparatorLine(lines[i + 1])) {
                    tableStarted = true
                    tableLines.add(line)
                } else {
                    preambleLines.add(line)
                }
            } else if (!tableEnded) {
                if (isTableCandidate) {
                    tableLines.add(line)
                } else {
                    tableEnded = true
                    postambleLines.add(line)
                }
            } else {
                postambleLines.add(line)
            }
        }

        if (tableLines.isEmpty()) {
            // Check if there are loose rows or just simple list
            return parseFallback(lines)
        }

        // Header line
        val headerLine = tableLines[0]
        val rawColumns = extractCells(headerLine)
        val columns = if (rawColumns.isEmpty()) listOf("Names") else rawColumns

        // Skip separator line (tableLines[1])
        val entries = mutableListOf<DatabaseEntry>()
        for (j in 2 until tableLines.size) {
            val rowLine = tableLines[j]
            if (isSeparatorLine(rowLine)) continue
            val cells = extractCells(rowLine)
            if (cells.isEmpty()) continue

            var name = ""
            var id = ""
            var description = ""
            var stats = ""
            var tags = emptyList<String>()
            val extraFields = mutableMapOf<String, String>()

            for (c in columns.indices) {
                val colName = columns[c]
                val cellValue = cells.getOrElse(c) { "" }.trim()
                when (colName.lowercase().trim()) {
                    "names", "name" -> name = cellValue
                    "id" -> id = cellValue
                    "description", "desc" -> description = cellValue
                    "stats", "stat" -> stats = cellValue
                    "tags", "tag" -> {
                        tags = if (cellValue.isNotBlank()) {
                            cellValue.split(",", ";").map { it.trim() }.filter { it.isNotBlank() }
                        } else emptyList()
                    }
                    else -> {
                        if (cellValue.isNotBlank()) {
                            extraFields[colName] = cellValue
                        }
                    }
                }
            }

            // Fallback: if name is empty but first cell exists
            if (name.isBlank() && cells.isNotEmpty()) {
                name = cells[0].trim()
            }

            if (name.isNotBlank() || id.isNotBlank() || description.isNotBlank()) {
                entries.add(
                    DatabaseEntry(
                        name = name,
                        id = id,
                        description = description,
                        stats = stats,
                        tags = tags,
                        extraFields = extraFields
                    )
                )
            }
        }

        return ParsedMarkdown(
            entries = entries,
            columns = columns,
            preamble = preambleLines.joinToString("\n").trimEnd(),
            postamble = postambleLines.joinToString("\n").trimStart()
        )
    }

    private fun parseFallback(lines: List<String>): ParsedMarkdown {
        val entries = mutableListOf<DatabaseEntry>()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || trimmed.isBlank()) continue
            val clean = trimmed.removePrefix("-").removePrefix("*").trim()
            if (clean.isNotBlank()) {
                entries.add(DatabaseEntry(name = clean))
            }
        }
        return ParsedMarkdown(
            entries = entries,
            columns = listOf("Names"),
            preamble = "",
            postamble = ""
        )
    }

    private fun isTableLine(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.startsWith("|") || trimmed.contains("|")
    }

    private fun isSeparatorLine(line: String): Boolean {
        val trimmed = line.trim()
        if (!trimmed.contains("-")) return false
        val cells = extractCells(trimmed)
        return cells.isNotEmpty() && cells.all { cell ->
            cell.replace(":", "").replace("-", "").trim().isEmpty()
        }
    }

    private fun extractCells(line: String): List<String> {
        val trimmed = line.trim()
        val stripped = trimmed.removePrefix("|").removeSuffix("|")
        return stripped.split("|").map { it.trim() }
    }

    fun serializeMarkdown(
        columns: List<String>,
        entries: List<DatabaseEntry>,
        preamble: String = "",
        postamble: String = ""
    ): String {
        val sb = StringBuilder()
        if (preamble.isNotBlank()) {
            sb.append(preamble.trim())
            sb.append("\n\n")
        }

        // Header
        sb.append("| ")
        sb.append(columns.joinToString(" | "))
        sb.append(" |\n")

        // Separator
        sb.append("| ")
        sb.append(columns.map { "---" }.joinToString(" | "))
        sb.append(" |\n")

        // Rows
        for (entry in entries) {
            sb.append("| ")
            val rowCells = columns.map { colName ->
                val rawVal = entry.getFieldValue(colName)
                // escape pipe characters in cell content
                rawVal.replace("|", "\\|").replace("\n", " ")
            }
            sb.append(rowCells.joinToString(" | "))
            sb.append(" |\n")
        }

        if (postamble.isNotBlank()) {
            sb.append("\n")
            sb.append(postamble.trim())
            sb.append("\n")
        }

        return sb.toString()
    }
}

data class ParsedMarkdown(
    val entries: List<DatabaseEntry>,
    val columns: List<String>,
    val preamble: String,
    val postamble: String
)

sealed class ParseResult {
    data class Success(
        val entries: List<DatabaseEntry>,
        val columns: List<String>,
        val preamble: String,
        val postamble: String,
        val rawContent: String
    ) : ParseResult()

    data class Error(val message: String) : ParseResult()
}
