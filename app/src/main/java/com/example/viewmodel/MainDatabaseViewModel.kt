package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MarkdownTableRepository
import com.example.data.ParseResult
import com.example.model.DatabaseEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

enum class SortOrder {
    ORIGINAL,
    A_TO_Z,
    Z_TO_A
}

data class DatabaseUiState(
    val isLinked: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val entries: List<DatabaseEntry> = emptyList(),
    val columns: List<String> = emptyList(),
    val preamble: String = "",
    val postamble: String = "",
    val searchQuery: String = "",
    val selectedTag: String = "all",
    val sortOrder: SortOrder = SortOrder.ORIGINAL,
    val saveNotification: String? = null,
    val isSaving: Boolean = false
) {
    val allTags: List<String>
        get() {
            val tagSet = mutableSetOf<String>()
            for (entry in entries) {
                for (tag in entry.tags) {
                    if (tag.isNotBlank()) tagSet.add(tag.trim())
                }
            }
            return tagSet.sortedWith(String.CASE_INSENSITIVE_ORDER)
        }

    /**
     * Advanced search supporting:
     * - plain text (name, id, description, tags, extraFields)
     * - stat names → rank by that stat descending (e.g. "strength")
     * - "overall" → rank by overall
     * - position / size values
     * - comma combinations: "CMF, LG, strength"
     */
    val filteredEntries: List<DatabaseEntry>
        get() {
            var list = entries

            // Tag filter first
            if (!selectedTag.equals("all", ignoreCase = true)) {
                list = list.filter { entry ->
                    entry.tags.any { it.equals(selectedTag, ignoreCase = true) }
                }
            }

            val rawQuery = searchQuery.trim()
            if (rawQuery.isNotBlank()) {
                // Split by comma for combinations
                val parts = rawQuery.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                var rankingStat: String? = null

                for (part in parts) {
                    val q = part.lowercase(Locale.getDefault())

                    when {
                        // Rank by a specific stat
                        q in STAT_KEYS -> {
                            rankingStat = q
                            // Keep all for now; we will sort later
                        }
                        q == "overall" -> {
                            rankingStat = "overall"
                        }
                        // Position filter
                        q in POSITIONS -> {
                            list = list.filter { entry ->
                                entry.extraFields["Position"]?.equals(part, ignoreCase = true) == true ||
                                entry.extraFields["POSITION"]?.equals(part, ignoreCase = true) == true ||
                                entry.stats.lowercase().contains("position:$q")
                            }
                        }
                        // Size filter
                        q in SIZES -> {
                            list = list.filter { entry ->
                                entry.extraFields["Size"]?.equals(part, ignoreCase = true) == true ||
                                entry.extraFields["SIZE"]?.equals(part, ignoreCase = true) == true
                            }
                        }
                        // General text search
                        else -> {
                            list = list.filter { entry ->
                                entry.name.lowercase(Locale.getDefault()).contains(q) ||
                                entry.id.lowercase(Locale.getDefault()).contains(q) ||
                                entry.description.lowercase(Locale.getDefault()).contains(q) ||
                                entry.stats.lowercase(Locale.getDefault()).contains(q) ||
                                entry.tags.any { it.lowercase(Locale.getDefault()).contains(q) } ||
                                entry.extraFields.values.any { it.lowercase(Locale.getDefault()).contains(q) }
                            }
                        }
                    }
                }

                // If a ranking stat was requested, sort by that stat descending
                if (rankingStat != null) {
                    list = list.sortedByDescending { entry ->
                        extractStat(entry, rankingStat!!)
                    }
                }
            }

            // Final A-Z / Z-A sort (only if no ranking was applied)
            if (searchQuery.isBlank() || !containsStatKeyword(searchQuery)) {
                list = when (sortOrder) {
                    SortOrder.ORIGINAL -> list
                    SortOrder.A_TO_Z -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.displayName })
                    SortOrder.Z_TO_A -> list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.displayName })
                }
            }

            return list
        }

    companion object {
        private val STAT_KEYS = setOf(
            "speed", "defense", "attack", "strength",
            "resistance", "flexibility", "iq", "overall"
        )
        private val POSITIONS = setOf(
            "gk", "cb", "lb", "rb", "dmf", "cmf", "amf",
            "lmf", "rmf", "lwf", "rwf", "ss", "cf"
        )
        private val SIZES = setOf("sm", "md", "lg", "xl")

        private fun extractStat(entry: DatabaseEntry, key: String): Float {
            val upper = key.uppercase()
            // Try stats string first
            val fromStats = entry.stats.split(",", ";")
                .map { it.trim() }
                .firstOrNull { it.uppercase().startsWith("$upper:") }
                ?.substringAfter(":")
                ?.trim()
                ?.toFloatOrNull()
            if (fromStats != null) return fromStats

            // Fallback overall calculation if needed
            if (key == "overall") {
                val values = listOf("SPEED", "DEFENSE", "ATTACK", "STRENGTH", "RESISTANCE", "FLEXIBILITY", "IQ")
                    .map { extractStat(entry, it.lowercase()) }
                return values.average().toFloat()
            }
            return 0f
        }

        private fun containsStatKeyword(query: String): Boolean {
            val lower = query.lowercase()
            return STAT_KEYS.any { lower.contains(it) }
        }
    }
}

class MainDatabaseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MarkdownTableRepository(application)

    private val _uiState = MutableStateFlow(DatabaseUiState())
    val uiState: StateFlow<DatabaseUiState> = _uiState.asStateFlow()

    init {
        checkExistingLink()
    }

    private fun checkExistingLink() {
        val linkedUri = repository.getLinkedUri()
        if (linkedUri != null) {
            loadFromUri(linkedUri)
        } else {
            _uiState.update { it.copy(isLinked = false, isLoading = false) }
        }
    }

    fun linkFile(uri: Uri) {
        repository.saveLinkedUri(uri)
        loadFromUri(uri)
    }

    fun unlinkFile() {
        repository.clearLinkedUri()
        _uiState.update { DatabaseUiState(isLinked = false) }
    }

    fun loadFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isLinked = true) }
            when (val result = repository.loadEntries(uri)) {
                is ParseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLinked = true,
                            entries = result.entries,
                            columns = result.columns,
                            preamble = result.preamble,
                            postamble = result.postamble,
                            errorMessage = null
                        )
                    }
                }
                is ParseResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLinked = true,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedTag(tag: String) {
        _uiState.update { it.copy(selectedTag = tag) }
    }

    fun toggleSortOrder() {
        _uiState.update {
            val nextSort = when (it.sortOrder) {
                SortOrder.ORIGINAL -> SortOrder.A_TO_Z
                SortOrder.A_TO_Z -> SortOrder.Z_TO_A
                SortOrder.Z_TO_A -> SortOrder.ORIGINAL
            }
            it.copy(sortOrder = nextSort)
        }
    }

    fun addEntry(newEntry: DatabaseEntry) {
        val current = _uiState.value
        val updatedList = current.entries + newEntry
        saveAndCommit(updatedList, "Added \"${newEntry.displayName}\" and saved to .md file")
    }

    fun updateEntry(oldEntry: DatabaseEntry, updatedEntry: DatabaseEntry) {
        val current = _uiState.value
        val index = current.entries.indexOf(oldEntry)
        val updatedList = if (index != -1) {
            current.entries.toMutableList().apply { set(index, updatedEntry) }
        } else {
            current.entries.map { if (it.name.equals(oldEntry.name, ignoreCase = true)) updatedEntry else it }
        }
        saveAndCommit(updatedList, "Updated \"${updatedEntry.displayName}\" and saved to .md file")
    }

    fun deleteEntry(entryToDelete: DatabaseEntry) {
        val current = _uiState.value
        val updatedList = current.entries.filterNot {
            it == entryToDelete || (it.name == entryToDelete.name && it.id == entryToDelete.id)
        }
        saveAndCommit(updatedList, "Deleted \"${entryToDelete.displayName}\" and updated .md file")
    }


    fun updateMultipleEntries(updates: List<Pair<DatabaseEntry, DatabaseEntry>>) {
        if (updates.isEmpty()) return
        val current = _uiState.value
        val updatedList = current.entries.toMutableList()
        updates.forEach { (old, new) ->
            val index = updatedList.indexOfFirst { it.name == old.name && it.id == old.id }
            if (index != -1) {
                updatedList[index] = new
            }
        }
        saveAndCommit(updatedList, "Updated ${updates.size} entries")
    }

    private fun saveAndCommit(newList: List<DatabaseEntry>, successMessage: String) {
        val uri = repository.getLinkedUri() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val current = _uiState.value
            val success = repository.saveEntries(
                uri = uri,
                entries = newList,
                currentColumns = current.columns,
                preamble = current.preamble,
                postamble = current.postamble
            )

            if (success) {
                when (val result = repository.loadEntries(uri)) {
                    is ParseResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                entries = result.entries,
                                columns = result.columns,
                                preamble = result.preamble,
                                postamble = result.postamble,
                                saveNotification = successMessage
                            )
                        }
                    }
                    is ParseResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                entries = newList,
                                saveNotification = successMessage
                            )
                        }
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveNotification = "Error saving changes to file"
                    )
                }
            }

            delay(3000)
            _uiState.update { it.copy(saveNotification = null) }
        }
    }

    fun refresh() {
        val uri = repository.getLinkedUri()
        if (uri != null) {
            loadFromUri(uri)
        }
    }
}
