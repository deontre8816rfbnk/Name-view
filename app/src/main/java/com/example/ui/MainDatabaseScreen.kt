package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DatabaseEntry
import com.example.ui.components.AddEditEntryDialog
import com.example.ui.components.DatabaseCard
import com.example.ui.components.TagPill
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LexendFontFamily
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
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
    var entryToDelete by remember { mutableStateOf<DatabaseEntry?>(null) }

    val filteredList = uiState.filteredEntries

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Section: Title & Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // Main Database Title with Lexend bold font
                    Text(
                        text = "Main Database",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("main_database_title")
                    )
                    Text(
                        text = "${uiState.entries.size} items in database",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh from .md file",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { isAddingNew = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Slate900,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("add_name_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Search Text Field (Right under the main title)
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "Search names, ID, tags, stats...",
                        fontFamily = LexendFontFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Slate900,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .testTag("search_text_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area: Cards List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentTeal)
                }
            } else if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank() || uiState.selectedTag != "all")
                                "No matching entries found"
                            else
                                "Database table is empty",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank() || uiState.selectedTag != "all")
                                "Try adjusting your search query or tag filter."
                            else
                                "Add your first entry or check your linked .md file table.",
                            fontFamily = LexendFontFamily,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (uiState.searchQuery.isNotBlank() || uiState.selectedTag != "all") {
                                    onSearchChange("")
                                    onTagSelect("all")
                                } else {
                                    isAddingNew = true
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                        ) {
                            Text(
                                text = if (uiState.searchQuery.isNotBlank() || uiState.selectedTag != "all")
                                    "Clear Filters"
                                else
                                    "+ Add First Name",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("database_cards_list"),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 100.dp // Leave space for bottom tags bar
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredList, key = { "${it.name}_${it.id}" }) { entry ->
                        DatabaseCard(
                            entry = entry,
                            onEdit = { entryToEdit = entry },
                            onDelete = { entryToDelete = entry },
                            onTagClick = { clickedTag -> onTagSelect(clickedTag) }
                        )
                    }
                }
            }
        }

        // Bottom Tags Bar: positioned at bottom left corner, starting from "all" and "A-Z sorting"
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(elevation = 12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 10.dp)
            ) {
                // Horizontally scrollable row starting from "all", "A-Z sorting" to dynamic tags
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bottom_tags_row"),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tag 1: "all"
                    item {
                        val isAllSelected = uiState.selectedTag.equals("all", ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAllSelected) Slate900 else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onTagSelect("all") }
                                .testTag("tag_pill_all")
                        ) {
                            Text(
                                text = "all",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }

                    // Tag 2: "A-Z sorting"
                    item {
                        val sortLabel = when (uiState.sortOrder) {
                            SortOrder.ORIGINAL -> "A-Z sorting"
                            SortOrder.A_TO_Z -> "A-Z sorting (A-Z)"
                            SortOrder.Z_TO_A -> "A-Z sorting (Z-A)"
                        }
                        val isSortActive = uiState.sortOrder != SortOrder.ORIGINAL

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSortActive) AccentTeal else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onToggleSort() }
                                .testTag("tag_pill_az_sorting")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = when (uiState.sortOrder) {
                                        SortOrder.ORIGINAL -> Icons.Default.SortByAlpha
                                        SortOrder.A_TO_Z -> Icons.Default.ArrowUpward
                                        SortOrder.Z_TO_A -> Icons.Default.ArrowDownward
                                    },
                                    contentDescription = "Toggle Sort",
                                    tint = if (isSortActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = sortLabel,
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSortActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Dynamically created tags
                    items(uiState.allTags) { tag ->
                        val isTagSelected = uiState.selectedTag.equals(tag, ignoreCase = true)
                        TagPill(
                            tag = tag,
                            isSelected = isTagSelected,
                            onClick = {
                                if (isTagSelected) onTagSelect("all") else onTagSelect(tag)
                            }
                        )
                    }
                }
            }
        }

        // Save Notification Banner (Indicates saved status)
        AnimatedVisibility(
            visible = uiState.saveNotification != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp)
        ) {
            uiState.saveNotification?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Slate900,
                    shadowElevation = 8.dp,
                    modifier = Modifier.testTag("save_status_indicator")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = msg,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Add New Entry Dialog
    if (isAddingNew) {
        AddEditEntryDialog(
            initialEntry = null,
            existingColumns = uiState.columns,
            onDismiss = { isAddingNew = false },
            onSave = { newEntry ->
                onAddEntry(newEntry)
                isAddingNew = false
            }
        )
    }

    // Edit Entry Dialog
    entryToEdit?.let { entry ->
        AddEditEntryDialog(
            initialEntry = entry,
            existingColumns = uiState.columns,
            onDismiss = { entryToEdit = null },
            onSave = { updatedEntry ->
                onUpdateEntry(entry, updatedEntry)
                entryToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = {
                Text(
                    text = "Delete Entry",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${entry.displayName}\" from the database and remove it from the .md file?",
                    fontFamily = LexendFontFamily,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEntry(entry)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Delete", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel", fontFamily = LexendFontFamily)
                }
            }
        )
    }
}
