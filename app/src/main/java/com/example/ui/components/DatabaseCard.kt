package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DatabaseEntry
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AccentTealContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LexendFontFamily
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DatabaseCard(
    entry: DatabaseEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { isExpanded = !isExpanded }
            .testTag("database_card_${entry.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Card Header: Title & ID + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title: Free Google Font Lexend, very thick and bold, capitalized
                        Text(
                            text = entry.displayName,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 21.sp,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("card_title_${entry.name}")
                        )

                        if (entry.id.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.testTag("card_id_${entry.id}")
                            ) {
                                Text(
                                    text = "#${entry.id}",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_button_${entry.name}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit ${entry.displayName}",
                            tint = AccentTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_button_${entry.name}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete ${entry.displayName}",
                            tint = DangerRed.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Description if present
            if (entry.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.description,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    modifier = Modifier.testTag("card_description_${entry.name}")
                )
            }

            // Stats section
            if (entry.stats.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                StatsView(stats = entry.stats)
            }

            // Tags section
            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entry.tags.forEach { tag ->
                        TagPill(
                            tag = tag,
                            onClick = { onTagClick(tag) }
                        )
                    }
                }
            }

            // Expanded view: Extra Fields
            AnimatedVisibility(visible = isExpanded && entry.extraFields.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Attributes",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    entry.extraFields.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key,
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = value,
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatsView(stats: String) {
    val statPairs = stats.split(",", ";").map { it.trim() }.filter { it.isNotBlank() }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            statPairs.forEach { pair ->
                val parts = pair.split(":", limit = 2)
                if (parts.size == 2) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = parts[0].trim(),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = parts[1].trim(),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AccentTeal
                        )
                    }
                } else {
                    Text(
                        text = pair,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TagPill(
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val bgColor = if (isSelected) {
        AccentTeal
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("tag_pill_$tag")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tag,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.75f),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = tag,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = textColor
            )
        }
    }
}
