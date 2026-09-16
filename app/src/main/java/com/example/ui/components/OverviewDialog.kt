package com.example.ui.components

import androidx.compose.ui.draw.clip

import androidx.compose.runtime.remember

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.graphics.asImageBitmap

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.Image

import android.net.Uri

import android.graphics.BitmapFactory

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DatabaseEntry
import com.example.model.EntityType
import com.example.ui.theme.LexendFontFamily

/**
 * Read-only overview. Tap outside to close.
 * Press-and-hold inside the card to open edit.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverviewDialog(
    entry: DatabaseEntry,
    onDismiss: () -> Unit,
    onRequestEdit: () -> Unit,
    nationPlayerCount: Int = -1,
    nationBreakdown: Map<String, Int> = emptyMap()
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() })
                },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF141414),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.82f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { onRequestEdit() },
                            onTap = { /* consume so outside tap doesn't fire */ }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text(
                        entry.displayName,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        entry.entityType.name,
                        fontFamily = LexendFontFamily,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    // Flag / Logo
                    val imgPath = entry.extraFields["FlagPath"]
                        ?: entry.extraFields["LogoPath"]
                        ?: ""
                    if (imgPath.isNotBlank()) {
                        val context = LocalContext.current
                        val bmp = remember(imgPath) {
                            try {
                                context.contentResolver.openInputStream(Uri.parse(imgPath))?.use {
                                    BitmapFactory.decodeStream(it)
                                }
                            } catch (_: Exception) { null }
                        }
                        if (bmp != null) {
                            Spacer(Modifier.height(12.dp))
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }

                    // Nation live stats
                    if (entry.entityType == EntityType.Nation && nationPlayerCount >= 0) {
                        Spacer(Modifier.height(10.dp))
                        InfoRow("Players", nationPlayerCount.toString())
                        if (nationBreakdown.isNotEmpty()) {
                            InfoRow(
                                "Positions",
                                nationBreakdown.entries.joinToString(", ") { "${it.key}: ${it.value}" }
                            )
                        }
                    }

                    if (entry.overall > 0f && entry.entityType == EntityType.Player) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Overall ${entry.overall.toInt()}",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF10B981)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    InfoRow("ID", entry.id)
                    InfoRow("Date", entry.date)
                    InfoRow("Description", entry.description)
                    InfoRow("Position", entry.position)
                    if (entry.secondaryPositions.isNotEmpty()) {
                        InfoRow("Secondary", entry.secondaryPositions.joinToString(", "))
                    }
                    InfoRow("Playstyle", entry.playstyle)
                    InfoRow("Nationality", entry.nationality)
                    InfoRow("Club", entry.club)

                    if (entry.tags.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Tags", fontFamily = LexendFontFamily, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        Spacer(Modifier.height(4.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            entry.tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        tag,
                                        fontFamily = LexendFontFamily,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (entry.skills.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        InfoRow("Skills", entry.skills.joinToString(", "))
                    }

                    if (entry.stats.isNotBlank()) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "STATS",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.45f)
                        )
                        Spacer(Modifier.height(6.dp))
                        entry.stats.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { pair ->
                            val parts = pair.split(":")
                            if (parts.size >= 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(parts[0].trim(), fontFamily = LexendFontFamily, fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
                                    Text(parts[1].trim(), fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    // Extra fields not already shown
                    val skip = setOf(
                        "Type", "Position", "Playstyle", "Date", "Overall",
                        "Nationality", "Club", "SecondaryPositions", "Skills",
                        "Size", "size"
                    )
                    entry.extraFields
                        .filterKeys { key -> skip.none { it.equals(key, ignoreCase = true) } }
                        .forEach { (k, v) ->
                            if (v.isNotBlank()) InfoRow(k, v)
                        }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Hold anywhere to edit, or use the button below",
                        fontFamily = LexendFontFamily,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close", color = Color.White.copy(alpha = 0.7f), fontFamily = LexendFontFamily)
                        }
                        androidx.compose.material3.Button(
                            onClick = onRequestEdit,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontFamily = LexendFontFamily, fontSize = 11.sp, color = Color.White.copy(alpha = 0.45f))
        Text(value, fontFamily = LexendFontFamily, fontSize = 15.sp, color = Color.White)
    }
}
