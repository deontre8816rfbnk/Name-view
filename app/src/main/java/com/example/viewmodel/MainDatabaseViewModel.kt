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
                    if (tag.isNotBlank()) {
                        tagSet.add(tag.trim())
                    }
                }
            }
            return tagSet.sortedWith(String.CASE_INSENSITIVE_ORDER)
        }

    val filteredEntries: List<DatabaseEntry>
        get() {
            var list = entries

            // Filter by search query
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase(Locale.getDefault())
                list = list.filter { entry ->
                    entry.name.lowercase(Locale.getDefault()).contains(q) ||
                    entry.id.lowercase(Locale.getDefault()).contains(q) ||
                    entry.description.lowercase(Locale.getDefault()).contains(q) ||
                    entry.stats.lowercase(Locale.getDefault()).contains(q) ||
                    entry.tags.any { it.lowercase(Locale.getDefault()).contains(q) } ||
                    entry.extraFields.values.any { it.lowercase(Locale.getDefault()).contains(q) }
                }
            }

            // Filter by tag
            if (!selectedTag.equals("all", ignoreCase = true)) {
                list = list.filter { entry ->
                    entry.tags.any { it.equals(selectedTag, ignoreCase = true) }
                }
            }

            // Sort
            list = when (sortOrder) {
                SortOrder.ORIGINAL -> list
                SortOrder.A_TO_Z -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.displayName })
                SortOrder.Z_TO_A -> list.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.displayName })
            }

            return list
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
                // Reload from file to ensure columns and structure are synced
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

            // Auto dismiss notification after 3 seconds
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
