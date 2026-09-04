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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val filteredList = uiState.filteredEntries
    val listState = rememberLazyListState()

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
            // Top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Main Database",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    color = Color.White
                )
                Text(
                    text = "${uiState.entries.size}",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Search
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

            Spacer(Modifier = Modifier.height(12.dp))

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
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stable key is critical for large lists
                        items(
                            items = filteredList,
                            key = { entry -> entry.name + "|" + entry.id }
                        ) { entry ->
                            DatabaseCard(
                                entry = entry,
                                onClick = { entryToEdit = entry },
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
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

            // Tags under FABs
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
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

        // Toast
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

    // Dialogs
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
