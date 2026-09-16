package com.example.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DatabaseEntry
import com.example.model.EntityType
import com.example.ui.theme.LexendFontFamily

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun DatabaseCard(
    entry: DatabaseEntry,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onTagClick: (String) -> Unit,
    nationPlayerCount: Int = -1,
    modifier: Modifier = Modifier
) {
    // Square card for Nation (and Club with logo)
    if (entry.entityType == EntityType.Nation) {
        NationSquareCard(
            entry = entry,
            isSelected = isSelected,
            playerCount = nationPlayerCount,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = modifier
        )
        return
    }

    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1E1E1E) else Color(0xFF141414)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.displayName,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (entry.entityType != EntityType.Player) {
                            Text(
                                entry.entityType.name,
                                fontFamily = LexendFontFamily,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.45f)
                            )
                        }
                        if (entry.entityType == EntityType.Player && entry.overall > 0f) {
                            Text(
                                "OVR ${entry.overall.toInt()}",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                        if (entry.position.isNotBlank() && entry.entityType == EntityType.Player) {
                            Text(
                                entry.position,
                                fontFamily = LexendFontFamily,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    if (entry.id.isNotBlank()) {
                        Text(
                            text = "ID: ${entry.id}",
                            fontFamily = LexendFontFamily,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    }
                    if (entry.description.isNotBlank()) {
                        Text(
                            text = entry.description,
                            fontFamily = LexendFontFamily,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (entry.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            entry.tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.combinedClickable(
                                        onClick = { onTagClick(tag) }
                                    )
                                ) {
                                    Text(
                                        text = tag,
                                        fontFamily = LexendFontFamily,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NationSquareCard(
    entry: DatabaseEntry,
    isSelected: Boolean,
    playerCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val flagPath = entry.extraFields["FlagPath"] ?: entry.extraFields["flagPath"] ?: ""
    val bitmap = remember(flagPath) {
        if (flagPath.isBlank()) null
        else try {
            context.contentResolver.openInputStream(Uri.parse(flagPath))?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1E1E1E) else Color(0xFF141414)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = entry.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        Icons.Default.Public,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier
                            .padding(18.dp)
                            .size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = entry.displayName,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (playerCount >= 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$playerCount players",
                    fontFamily = LexendFontFamily,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
