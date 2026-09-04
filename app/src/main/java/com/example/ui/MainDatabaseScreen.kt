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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.LexendFontFamily
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
            // Top: Title + Refresh + Add
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Main Database",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("main_database_title")
                    )
                    Text(
                        text = "${uiState.entries.size} items in database",
                        fontFamily = LexendFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onRefresh, modifier = Modifier.testTag("refresh_button")) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { isAddingNew = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("add_name_top_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        "Search names, ID, tags, stats...",
                        fontFamily = LexendFontFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Slate900,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .testTag("search_text_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cards list
            if (uiState.isLoading) {
                Box(Modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentTeal)
                }
            } else if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(64.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Storage, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank() || uiState.selectedTag != "all")
                                "No matching entries found" else "Database table is empty",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank() || uiState.selectedTag != "all")
                                "Try adjusting your search or tag filter."
                            else
                                "Add your first entry or check your linked .md file.",
                            fontFamily = LexendFontFamily,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("database_cards_list"),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredList, key = { "${it.name}_${it.id}" }) { entry ->
                        DatabaseCard(
                            entry = entry,
                            onEdit = { entryToEdit = entry },
                            onDelete = { entryToDelete = entry },
                            onTagClick = { onTagSelect(it) }
                        )
                    }
                }
            }
        }

        // ========== BOTTOM AREA ==========
        // Tags row sits RIGHT ABOVE the FABs
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Tags bar – A-Z on the RIGHT, All + tags scrollable
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .testTag("bottom_tags_row"),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true          // makes items start from the right
                ) {
                    // A-Z sorting – always the right-most item because of reverseLayout
                    item {
                        val sortLabel = when (uiState.sortOrder) {
                            SortOrder.ORIGINAL -> "A-Z"
                            SortOrder.A_TO_Z -> "A→Z"
                            SortOrder.Z_TO_A -> "Z→A"
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
                                    contentDescription = null,
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

                    // Dynamic tags
                    items(uiState.allTags) { tag ->
                        val isSelected = uiState.selectedTag.equals(tag, ignoreCase = true)
                        TagPill(
                            tag = tag,
                            isSelected = isSelected,
                            onClick = { if (isSelected) onTagSelect("all") else onTagSelect(tag) }
                        )
                    }

                    // "All" tag
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
                                text = "All",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // FABs row (Search + Add) – sits at the very bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { /* focus is handled by the search field above */ },
                    containerColor = Color.White,
                    contentColor = Slate900,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("search_fab")
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                FloatingActionButton(
                    onClick = { isAddingNew = true },
                    containerColor = Slate900,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("add_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add name", modifier = Modifier.size(28.dp))
                }
            }
        }

        // Save notification
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
                    shadowElevation = 8.dp
                ) {
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
            onSave = { onAddEntry(it); isAddingNew = false }
        )
    }

    entryToEdit?.let { entry ->
        AddEditEntryDialog(
            initialEntry = entry,
            existingColumns = uiState.columns,
            onDismiss = { entryToEdit = null },
            onSave = { onUpdateEntry(entry, it); entryToEdit = null }
        )
    }

    entryToDelete?.let { entry ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete ${entry.displayName}?", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove the entry from the linked .md file.", fontFamily = LexendFontFamily) },
            confirmButton = {
                Button(
                    onClick = { onDeleteEntry(entry); entryToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) { Text("Delete", fontFamily = LexendFontFamily) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel", fontFamily = LexendFontFamily)
                }
            }
        )
    }
}
