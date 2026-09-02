package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.model.DatabaseEntry
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LexendFontFamily
import com.example.ui.theme.Slate900

@Composable
fun AddEditEntryDialog(
    initialEntry: DatabaseEntry? = null,
    existingColumns: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (DatabaseEntry) -> Unit
) {
    val isEdit = initialEntry != null

    var name by remember { mutableStateOf(initialEntry?.name ?: "") }
    var id by remember { mutableStateOf(initialEntry?.id ?: "") }
    var description by remember { mutableStateOf(initialEntry?.description ?: "") }
    var stats by remember { mutableStateOf(initialEntry?.stats ?: "") }
    var tagsInput by remember { mutableStateOf(initialEntry?.tagsString ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }

    // Dynamic custom attributes
    val customFields = remember {
        mutableStateMapOf<String, String>().apply {
            initialEntry?.extraFields?.forEach { (k, v) -> put(k, v) }
        }
    }

    var newCustomKey by remember { mutableStateOf("") }
    var newCustomVal by remember { mutableStateOf("") }
    var showAddCustomField by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp)
            .testTag("add_edit_entry_dialog"),
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) "Edit Database Entry" else "Add New Entry",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name Field (Required)
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Name *", fontFamily = LexendFontFamily) },
                    placeholder = { Text("e.g. Aetherius", fontFamily = LexendFontFamily) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = DangerRed) } },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_entry_name")
                )

                // ID Field
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("ID (Optional)", fontFamily = LexendFontFamily) },
                    placeholder = { Text("e.g. 001 or G-OV16", fontFamily = LexendFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_entry_id")
                )

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", fontFamily = LexendFontFamily) },
                    placeholder = { Text("Overview or bio details...", fontFamily = LexendFontFamily) },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_entry_description")
                )

                // Stats Field
                OutlinedTextField(
                    value = stats,
                    onValueChange = { stats = it },
                    label = { Text("Stats (e.g. ATK: 85, DEF: 60)", fontFamily = LexendFontFamily) },
                    placeholder = { Text("HP: 100, STR: 50, SPD: 70", fontFamily = LexendFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_entry_stats")
                )

                // Tags Field
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags (comma separated)", fontFamily = LexendFontFamily) },
                    placeholder = { Text("warrior, boss, fire, ancient", fontFamily = LexendFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_entry_tags")
                )

                // Custom Columns / Fields Section
                if (customFields.isNotEmpty()) {
                    Text(
                        text = "Additional Column Fields",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    customFields.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = value,
                                onValueChange = { customFields[key] = it },
                                label = { Text(key, fontFamily = LexendFontFamily) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { customFields.remove(key) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove $key",
                                    tint = DangerRed
                                )
                            }
                        }
                    }
                }

                // Add Custom Column Form
                if (showAddCustomField) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "New Column Attribute",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newCustomKey,
                                onValueChange = { newCustomKey = it },
                                placeholder = { Text("Column Name", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newCustomVal,
                                onValueChange = { newCustomVal = it },
                                placeholder = { Text("Value", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                showAddCustomField = false
                                newCustomKey = ""
                                newCustomVal = ""
                            }) {
                                Text("Cancel", fontFamily = LexendFontFamily)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (newCustomKey.isNotBlank()) {
                                        customFields[newCustomKey.trim()] = newCustomVal.trim()
                                        newCustomKey = ""
                                        newCustomVal = ""
                                        showAddCustomField = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                            ) {
                                Text("Add Field", fontFamily = LexendFontFamily)
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showAddCustomField = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Custom Column / Field",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Name cannot be empty"
                        return@Button
                    }
                    val parsedTags = tagsInput.split(",", ";")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    val newEntry = DatabaseEntry(
                        name = name.trim(),
                        id = id.trim(),
                        description = description.trim(),
                        stats = stats.trim(),
                        tags = parsedTags,
                        extraFields = customFields.toMap()
                    )
                    onSave(newEntry)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Slate900,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("save_entry_button")
            ) {
                Text(
                    text = if (isEdit) "Save Changes" else "Add Entry",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = LexendFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
