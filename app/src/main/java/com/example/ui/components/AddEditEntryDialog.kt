package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DatabaseEntry
import com.example.ui.theme.LexendFontFamily
import kotlin.math.roundToInt

private val POSITIONS = listOf(
    "GK", "CB", "LB", "RB", "DMF", "CMF", "AMF",
    "LMF", "RMF", "LWF", "RWF", "SS", "CF"
)

private val SIZES = listOf("SM", "MD", "LG", "XL")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditEntryDialog(
    initialEntry: DatabaseEntry?,
    existingColumns: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (DatabaseEntry) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isEdit = initialEntry != null

    var name by remember { mutableStateOf(initialEntry?.name ?: "") }
    var isEditingName by remember { mutableStateOf(!isEdit) }
    var customId by remember { mutableStateOf(initialEntry?.id ?: "") }
    var description by remember { mutableStateOf(initialEntry?.description ?: "") }
    val tags = remember { mutableStateListOf<String>().apply { addAll(initialEntry?.tags ?: emptyList()) } }
    var newTag by remember { mutableStateOf("") }
    var customDate by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("MD") }
    var position by remember { mutableStateOf("") }

    // Stats – parsed from existing entry
    var speed by remember { mutableStateOf(0f) }
    var defense by remember { mutableStateOf(0f) }
    var attack by remember { mutableStateOf(0f) }
    var strength by remember { mutableStateOf(0f) }
    var resistance by remember { mutableStateOf(0f) }
    var flexibility by remember { mutableStateOf(0f) }
    var iq by remember { mutableStateOf(0f) }

    // Parse existing stats string when editing
    LaunchedEffect(initialEntry) {
        if (initialEntry != null) {
            val statsMap = parseStats(initialEntry.stats)
            speed = statsMap["SPEED"] ?: 0f
            defense = statsMap["DEFENSE"] ?: 0f
            attack = statsMap["ATTACK"] ?: 0f
            strength = statsMap["STRENGTH"] ?: 0f
            resistance = statsMap["RESISTANCE"] ?: 0f
            flexibility = statsMap["FLEXIBILITY"] ?: 0f
            iq = statsMap["IQ"] ?: 0f

            // Also try to recover size / position from extraFields or stats if present
            initialEntry.extraFields["Size"]?.let { size = it }
            initialEntry.extraFields["Position"]?.let { position = it }
            initialEntry.extraFields["SIZE"]?.let { size = it }
            initialEntry.extraFields["POSITION"]?.let { position = it }
        }
    }

    // Overall = average of the 7 stats, rounded to 2 decimals
    val overall = remember(speed, defense, attack, strength, resistance, flexibility, iq) {
        val values = listOf(speed, defense, attack, strength, resistance, flexibility, iq)
        val avg = values.average().toFloat()
        (avg * 100).roundToInt() / 100f
    }

    var sizeExpanded by remember { mutableStateOf(false) }
    var positionExpanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.White.copy(alpha = 0.35f),
        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        cursorColor = Color.White,
        focusedContainerColor = Color(0xFF1A1A1A),
        unfocusedContainerColor = Color(0xFF1A1A1A)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(22.dp)),
            color = Color(0xFF121212)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Header – Name + Edit icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditingName) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Enter the name...", color = Color.White.copy(alpha = 0.35f)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = name.ifBlank { "Edit Name" },
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    IconButton(onClick = { isEditingName = !isEditingName }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit name", tint = Color.White.copy(alpha = 0.75f))
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // IDENTITY
                    SectionTitle("IDENTITY")

                    FieldLabel("CUSTOM ID")
                    OutlinedTextField(
                        value = customId,
                        onValueChange = { customId = it },
                        placeholder = { Text("e.g. 42", color = Color.White.copy(alpha = 0.35f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(14.dp))

                    FieldLabel("DESCRIPTION")
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Write a short description...", color = Color.White.copy(alpha = 0.35f)) },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(14.dp))

                    FieldLabel("TAGS")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newTag,
                            onValueChange = { newTag = it },
                            placeholder = { Text("Add tag...", color = Color.White.copy(alpha = 0.35f)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp).clickable {
                                val t = newTag.trim()
                                if (t.isNotEmpty() && !tags.contains(t)) {
                                    tags.add(t)
                                    newTag = ""
                                }
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, null, tint = Color.White)
                            }
                        }
                    }
                    if (tags.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            tags.forEach { tag ->
                                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.1f)) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(tag, fontFamily = LexendFontFamily, fontSize = 13.sp, color = Color.White)
                                        Spacer(Modifier.width(4.dp))
                                        Text("×", color = Color.White.copy(alpha = 0.6f), modifier = Modifier.clickable { tags.remove(tag) })
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))

                    FieldLabel("CUSTOM DATE/TIME")
                    OutlinedTextField(
                        value = customDate,
                        onValueChange = { customDate = it },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    // APPEARANCE
                    SectionTitle("APPEARANCE")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel("SIZE")
                            ExposedDropdownMenuBox(expanded = sizeExpanded, onExpandedChange = { sizeExpanded = it }) {
                                OutlinedTextField(
                                    value = size,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sizeExpanded) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors,
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = sizeExpanded, onDismissRequest = { sizeExpanded = false }) {
                                    SIZES.forEach { option ->
                                        DropdownMenuItem(text = { Text(option) }, onClick = {
                                            size = option
                                            sizeExpanded = false
                                        })
                                    }
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel("POSITION")
                            ExposedDropdownMenuBox(expanded = positionExpanded, onExpandedChange = { positionExpanded = it }) {
                                OutlinedTextField(
                                    value = position.ifBlank { "Select..." },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = positionExpanded) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors,
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = positionExpanded, onDismissRequest = { positionExpanded = false }) {
                                    POSITIONS.forEach { option ->
                                        DropdownMenuItem(text = { Text(option) }, onClick = {
                                            position = option
                                            positionExpanded = false
                                        })
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // STATS
                    SectionTitle("STATS (0 – 100)")

                    // Overall (read-only calculated)
                    FieldLabel("OVERALL")
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "%.2f".format(overall),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))

                    // Individual stats with drag support
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DraggableStatField("SPEED", speed, { speed = it }, Modifier.weight(1f), fieldColors)
                        DraggableStatField("DEFENSE", defense, { defense = it }, Modifier.weight(1f), fieldColors)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DraggableStatField("ATTACK", attack, { attack = it }, Modifier.weight(1f), fieldColors)
                        DraggableStatField("STRENGTH", strength, { strength = it }, Modifier.weight(1f), fieldColors)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DraggableStatField("RESISTANCE", resistance, { resistance = it }, Modifier.weight(1f), fieldColors)
                        DraggableStatField("FLEXIBILITY", flexibility, { flexibility = it }, Modifier.weight(1f), fieldColors)
                    }
                    Spacer(Modifier.height(10.dp))
                    DraggableStatField("IQ", iq, { iq = it }, Modifier.fillMaxWidth(0.5f), fieldColors)

                    Spacer(Modifier.height(32.dp))
                }

                // Bottom buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isEdit && onDelete != null) {
                        TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                            Text("Delete", color = Color(0xFFEF4444), fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f), fontFamily = LexendFontFamily)
                    }
                    Button(
                        onClick = {
                            val finalName = name.trim()
                            if (finalName.isBlank()) return@Button

                            val statsStr = listOf(
                                "SPEED:${"%.2f".format(speed)}",
                                "DEFENSE:${"%.2f".format(defense)}",
                                "ATTACK:${"%.2f".format(attack)}",
                                "STRENGTH:${"%.2f".format(strength)}",
                                "RESISTANCE:${"%.2f".format(resistance)}",
                                "FLEXIBILITY:${"%.2f".format(flexibility)}",
                                "IQ:${"%.2f".format(iq)}",
                                "OVERALL:${"%.2f".format(overall)}"
                            ).joinToString(", ")

                            val extra = mutableMapOf<String, String>()
                            if (size.isNotBlank()) extra["Size"] = size
                            if (position.isNotBlank()) extra["Position"] = position

                            onSave(
                                DatabaseEntry(
                                    name = finalName,
                                    id = customId.trim(),
                                    description = description.trim(),
                                    tags = tags.toList(),
                                    stats = statsStr,
                                    extraFields = extra
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEdit) "Update" else "Save", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/** Parse "SPEED:12.00, DEFENSE:8.50, ..." into a map */
private fun parseStats(raw: String): Map<String, Float> {
    if (raw.isBlank()) return emptyMap()
    return raw.split(",", ";")
        .map { it.trim() }
        .mapNotNull { pair ->
            val parts = pair.split(":", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim().uppercase()
                val value = parts[1].trim().toFloatOrNull() ?: 0f
                key to value
            } else null
        }.toMap()
}

@Composable
private fun DraggableStatField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.TextFieldColors
) {
    var text by remember { mutableStateOf("%.2f".format(value)) }
    var isDragging by remember { mutableStateOf(false) }
    var liveValue by remember { mutableStateOf(value) }

    LaunchedEffect(value, isDragging) {
        if (!isDragging) {
            liveValue = value
            text = "%.2f".format(value)
        }
    }

    Column(modifier = modifier) {
        FieldLabel(label)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { new ->
                    if (!isDragging) {
                        text = new
                        new.toFloatOrNull()?.let {
                            onValueChange(it.coerceIn(0f, 100f))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(6.dp))
            // Press and hold this handle, then drag without releasing.
            // Drag UP = increase, DOWN = decrease. Continuous whole numbers 0..100.
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDragging) Color(0xFF10B981) else Color.White.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(width = 40.dp, height = 56.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                isDragging = true
                                liveValue = value
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                // Fast continuous: 8px ≈ 1 point
                                val delta = -dragAmount / 8f
                                liveValue = (liveValue + delta).coerceIn(0f, 100f)
                                val snapped = liveValue.roundToInt().toFloat()
                                onValueChange(snapped)
                                text = "%.2f".format(snapped)
                            }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        null,
                        tint = if (isDragging) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        null,
                        tint = if (isDragging) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = LexendFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = Color.White.copy(alpha = 0.45f),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontFamily = LexendFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = Color.White.copy(alpha = 0.55f),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}
