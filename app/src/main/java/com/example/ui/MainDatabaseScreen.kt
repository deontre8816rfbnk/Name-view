package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DatabaseEntry
import com.example.ui.components.AddEditEntryDialog
import com.example.ui.components.DatabaseCard
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.LexendFontFamily
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.DatabaseUiState
import com.example.viewmodel.SortOrder
import kotlinx.coroutines.launch

@Composable
fun MainDatabaseScreen(
    uiState: DatabaseUiState,
    onSearchChange: (String) -> Unit,
    onTagSelect: (String) -> Unit,
    onToggleSort: () -> Unit,
    onAddEntry: (DatabaseEntry) -> Unit,
    onUpdateEntry: (DatabaseEntry, DatabaseEntry) -> Unit,
    onDeleteEntry: (DatabaseEntry) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var entryToEdit by remember { mutableStateOf<DatabaseEntry?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    var selectionMode by remember { mutableStateOf(false) }
    var selectedKeys by remember { mutableStateOf(setOf<String>()) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val filteredList = uiState.filteredEntries
    val gridState = rememberLazyGridState()

    fun entryKey(e: DatabaseEntry) = e.name + "|" + e.id

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectionMode) "${selectedKeys.size} selected" else "Main Database",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.White
                )
                if (!selectionMode) {
                    Text(
                        text = "${uiState.entries.size}",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "Cancel",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.clickable {
                            selectionMode = false
                            selectedKeys = emptySet()
                        }
                    )
                }
            }

            // Search
            if (!selectionMode) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text("Search names, tags...", fontFamily = LexendFontFamily, fontSize = 14.sp, color = Color.White.copy(alpha = 0.4f))
                    },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.6f)) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, null, tint = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentTeal)
                    }
                }
                filteredList.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().weight(1f).padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Storage, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (uiState.searchQuery.isNotBlank() || uiState.selectedTag != "all") "No matching names" else "No names yet",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 180.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = filteredList,
                            key = { entryKey(it) },
                            contentType = { "name_card" }          // helps performance with large lists
                        ) { entry ->
                            val key = entryKey(entry)
                            DatabaseCard(
                                entry = entry,
                                isSelected = selectedKeys.contains(key),
                                onClick = {
                                    if (selectionMode) {
                                        selectedKeys = if (selectedKeys.contains(key)) {
                                            selectedKeys - key
                                        } else {
                                            selectedKeys + key
                                        }
                                        if (selectedKeys.isEmpty()) selectionMode = false
                                    } else {
                                        entryToEdit = entry
                                    }
                                },
                                onLongClick = {
                                    selectionMode = true
                                    selectedKeys = selectedKeys + key
                                },
                                onTagClick = { onTagSelect(it) }
                            )
                        }
                    }
                }
            }
        }

        // Bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            if (selectionMode && selectedKeys.isNotEmpty()) {
                Surface(color = Color(0xFF1A1A1A), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                val names = filteredList
                                    .filter { selectedKeys.contains(entryKey(it)) }
                                    .joinToString("\n") { "• ${it.displayName}" }
                                clipboardManager.setText(AnnotatedString(names))
                            }) {
                                Icon(Icons.Default.ContentCopy, null, tint = Color.White)
                            }
                            Text("Copy", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), fontFamily = LexendFontFamily)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                filteredList
                                    .filter { selectedKeys.contains(entryKey(it)) }
                                    .forEach { onDeleteEntry(it) }
                                selectedKeys = emptySet()
                                selectionMode = false
                            }) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444))
                            }
                            Text("Remove", fontSize = 11.sp, color = Color(0xFFEF4444), fontFamily = LexendFontFamily)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Label, null, tint = Color.White)
                            }
                            Text("Tag", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), fontFamily = LexendFontFamily)
                        }
                    }
                }
            }

            if (!selectionMode) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FloatingActionButton(
                        onClick = {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        },
                        containerColor = Color(0xFF1C1C1C),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    FloatingActionButton(
                        onClick = { isAddingNew = true },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        elevation = FloatingActionButtonDefaults.elevation(6.dp),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(28.dp))
                    }
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.10f),
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable {
                            scope.launch {
                                if (gridState.canScrollForward) {
                                    gridState.scrollToItem(filteredList.lastIndex.coerceAtLeast(0))
                                } else {
                                    gridState.scrollToItem(0)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (gridState.canScrollForward) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Jump",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(10.dp).size(18.dp)
                        )
                    }
                }

                item {
                    val isSortActive = uiState.sortOrder != SortOrder.ORIGINAL
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSortActive) AccentTeal else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { onToggleSort() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                when (uiState.sortOrder) {
                                    SortOrder.ORIGINAL -> Icons.Default.SortByAlpha
                                    SortOrder.A_TO_Z -> Icons.Default.ArrowUpward
                                    SortOrder.Z_TO_A -> Icons.Default.ArrowDownward
                                },
                                null,
                                tint = if (isSortActive) Color.White else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                when (uiState.sortOrder) {
                                    SortOrder.ORIGINAL -> "A-Z"
                                    SortOrder.A_TO_Z -> "A→Z"
                                    SortOrder.Z_TO_A -> "Z→A"
                                },
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSortActive) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                items(uiState.allTags, key = { it }) { tag ->
                    val isSelected = uiState.selectedTag.equals(tag, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable {
                            if (isSelected) onTagSelect("all") else onTagSelect(tag)
                        }
                    ) {
                        Text(
                            tag,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }

                item {
                    val isAllSelected = uiState.selectedTag.equals("all", ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isAllSelected) Color.White else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { onTagSelect("all") }
                    ) {
                        Text(
                            "All",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isAllSelected) Color.Black else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.saveNotification != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 8.dp)
        ) {
            uiState.saveNotification?.let { msg ->
                Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF1C1C1C), shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                        Text(msg, fontFamily = LexendFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }
    }

    if (isAddingNew) {
        AddEditEntryDialog(
            initialEntry = null,
            existingColumns = uiState.columns,
            onDismiss = { isAddingNew = false },
            onSave = {
                onAddEntry(it)
                isAddingNew = false
            }
        )
    }

    entryToEdit?.let { entry ->
        AddEditEntryDialog(
            initialEntry = entry,
            existingColumns = uiState.columns,
            onDismiss = { entryToEdit = null },
            onSave = {
                onUpdateEntry(entry, it)
                entryToEdit = null
            },
            onDelete = {
                onDeleteEntry(entry)
                entryToEdit = null
            }
        )
    }
}
