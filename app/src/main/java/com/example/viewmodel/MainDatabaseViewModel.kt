package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MarkdownTableRepository
import com.example.data.ParseResult
import com.example.model.DatabaseEntry
import com.example.model.EntityType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

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
    val isSaving: Boolean = false,
    /** Seed for random feed of 46 cards. Changes when "All" is pressed. */
    val feedSeed: Long = System.currentTimeMillis()
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

    val nations: List<String>
        get() = entries
            .filter { it.entityType == EntityType.Nation }
            .map { it.displayName }
            .distinct()
            .sortedWith(String.CASE_INSENSITIVE_ORDER)

    val clubs: List<String>
        get() = entries
            .filter { it.entityType == EntityType.Club }
            .map { it.displayName }
            .distinct()
            .sortedWith(String.CASE_INSENSITIVE_ORDER)

    /**
     * Filtered + ranked list used for the feed / search results.
     * - Tag filter
     * - Search (starts-with for single letter, combinations, overall ranking)
     * - "Highest overall" → top 10
     * - Default feed: up to 46 random cards when no search/tag
     */
    val filteredEntries: List<DatabaseEntry>
        get() {
            var list = entries

            // Tag filter
            if (!selectedTag.equals("all", ignoreCase = true)) {
                list = list.filter { entry ->
                    entry.tags.any { it.equals(selectedTag, ignoreCase = true) }
                }
            }

            val rawQuery = searchQuery.trim()
            var rankingStat: String? = null
            var highestOverallOnly = false

            if (rawQuery.isNotBlank()) {
                val parts = rawQuery.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                for (part in parts) {
                    val q = part.lowercase(Locale.getDefault())

                    when {
                        q == "highest" || q == "highest overall" || q == "highest overall stat" -> {
                            highestOverallOnly = true
                            rankingStat = "overall"
                        }
                        q in STAT_KEYS || q == "overall" -> {
                            rankingStat = if (q == "overall") "overall" else q
                        }
                        q in POSITIONS -> {
                            list = list.filter { entry ->
                                entry.position.equals(part, ignoreCase = true) ||
                                    entry.secondaryPositions.any { it.equals(part, ignoreCase = true) }
                            }
                        }
                        // Single letter → starts with only
                        q.length == 1 && q[0].isLetter() -> {
                            list = list.filter { entry ->
                                entry.name.lowercase(Locale.getDefault()).startsWith(q)
                            }
                        }
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

                // Rank by overall (or chosen stat) when relevant
                if (rankingStat != null || (!highestOverallOnly && rankingStat == null && parts.any { it.lowercase() in POSITIONS || it.lowercase() in STAT_KEYS })) {
                    val key = rankingStat ?: "overall"
                    list = list.sortedByDescending { extractOverallOrStat(it, key) }
                }

                if (highestOverallOnly) {
                    list = list.sortedByDescending { extractOverallOrStat(it, "overall") }.take(10)
                }
            } else if (selectedTag.equals("all", ignoreCase = true) && sortOrder == SortOrder.ORIGINAL) {
                // Feed mode: random 46
                list = list.shuffled(Random(feedSeed)).take(46)
            }

            // A-Z / Z-A only when not in ranked / feed-random mode
            if (rawQuery.isBlank() && !selectedTag.equals("all", ignoreCase = true).not()) {
                // already handled feed
            }
            if (rawQuery.isBlank() && sortOrder != SortOrder.ORIGINAL) {
                list = when (sortOrder) {
                    SortOrder.A_TO_Z -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.displayName })
                    SortOrder.Z_TO_A -> list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.displayName })
                    else -> list
                }
            }

            return list
        }

    companion object {
        private val STAT_KEYS = setOf(
            "speed", "defense", "attack", "strength", "resistance", "flexibility", "iq", "overall",
            "finishing", "acceleration", "stamina", "dribbling", "heading", "tackling", "aggression"
        )
        private val POSITIONS = setOf(
            "gk", "cb", "lb", "rb", "dmf", "cmf", "amf",
            "lmf", "rmf", "lwf", "rwf", "ss", "cf"
        )

        fun extractOverallOrStat(entry: DatabaseEntry, key: String): Float {
            if (key.equals("overall", ignoreCase = true)) {
                val o = entry.overall
                if (o > 0f) return o
                // fallback: average of any numeric stats in the stats string
                val values = entry.stats.split(",", ";")
                    .mapNotNull {
                        it.substringAfter(":", "").trim().toFloatOrNull()
                    }
                return if (values.isNotEmpty()) values.average().toFloat() else 0f
            }
            return entry.extractStat(key)
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

    /**
     * Selecting "all" clears search and refreshes the random feed of 46.
     */
    fun setSelectedTag(tag: String) {
        if (tag.equals("all", ignoreCase = true)) {
            _uiState.update {
                it.copy(
                    selectedTag = "all",
                    searchQuery = "",
                    feedSeed = System.currentTimeMillis()
                )
            }
        } else {
            _uiState.update { it.copy(selectedTag = tag) }
        }
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
        saveAndCommit(updatedList, "Added \"${newEntry.displayName}\"")
    }

    fun updateEntry(oldEntry: DatabaseEntry, updatedEntry: DatabaseEntry) {
        val current = _uiState.value
        val index = current.entries.indexOfFirst {
            it.name == oldEntry.name && it.id == oldEntry.id
        }
        val updatedList = if (index != -1) {
            current.entries.toMutableList().apply { set(index, updatedEntry) }
        } else {
            current.entries.map {
                if (it.name.equals(oldEntry.name, ignoreCase = true)) updatedEntry else it
            }
        }
        saveAndCommit(updatedList, "Updated \"${updatedEntry.displayName}\"")
    }

    fun deleteEntry(entryToDelete: DatabaseEntry) {
        val current = _uiState.value
        val updatedList = current.entries.filterNot {
            it.name == entryToDelete.name && it.id == entryToDelete.id
        }
        saveAndCommit(updatedList, "Deleted \"${entryToDelete.displayName}\"")
    }

    /** Batch update (multi-select tag assignment). One save for all. */
    fun updateMultipleEntries(updates: List<Pair<DatabaseEntry, DatabaseEntry>>) {
        if (updates.isEmpty()) return
        val current = _uiState.value
        val updatedList = current.entries.toMutableList()
        updates.forEach { (old, new) ->
            val index = updatedList.indexOfFirst { it.name == old.name && it.id == old.id }
            if (index != -1) {
                updatedList[index] = new
            } else {
                val idx2 = updatedList.indexOfFirst { it.name == old.name }
                if (idx2 != -1) updatedList[idx2] = new
            }
        }
        saveAndCommit(updatedList, "Updated ${updates.size} entries")
    }

    /** Batch delete (multi-select). One save for all. */
    fun deleteMultipleEntries(toDelete: List<DatabaseEntry>) {
        if (toDelete.isEmpty()) return
        val keys = toDelete.map { it.name to it.id }.toSet()
        val current = _uiState.value
        val updatedList = current.entries.filterNot { (it.name to it.id) in keys }
        saveAndCommit(updatedList, "Deleted ${toDelete.size} entries")
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
