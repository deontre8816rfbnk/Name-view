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
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.model.EntityConstants
import com.example.model.EntityType
import com.example.ui.theme.LexendFontFamily
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditEntryDialog(
    initialEntry: DatabaseEntry?,
    existingColumns: List<String>,
    availableNations: List<String> = emptyList(),
    availableClubs: List<String> = emptyList(),
    availableTags: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (DatabaseEntry) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isEdit = initialEntry != null

    var name by remember { mutableStateOf(initialEntry?.name ?: "") }
    var customId by remember { mutableStateOf(initialEntry?.id ?: "") }
    var description by remember { mutableStateOf(initialEntry?.description ?: "") }
    var tags by remember { mutableStateOf(initialEntry?.tags?.toList() ?: emptyList()) }
    var tagInput by remember { mutableStateOf("") }
    var showTagPicker by remember { mutableStateOf(false) }

    var position by remember { mutableStateOf(initialEntry?.position ?: "CF") }
    var playstyle by remember { mutableStateOf(initialEntry?.playstyle ?: "") }
    var nationality by remember { mutableStateOf(initialEntry?.nationality ?: "") }
    var club by remember { mutableStateOf(initialEntry?.club ?: "") }
    var secondary by remember {
        mutableStateOf(initialEntry?.secondaryPositions?.toList() ?: emptyList())
    }
    var skills by remember {
        mutableStateOf(initialEntry?.skills?.toList() ?: emptyList())
    }

    // Date wheels
    var day by remember {
        mutableStateOf(
            initialEntry?.date?.split("/")?.getOrNull(0)?.toIntOrNull() ?: 1
        )
    }
    var month by remember {
        mutableStateOf(
            initialEntry?.date?.split("/")?.getOrNull(1)?.toIntOrNull() ?: 1
        )
    }
    var year by remember {
        mutableStateOf(
            initialEntry?.date?.split("/")?.getOrNull(2)?.toIntOrNull() ?: 2024
        )
    }

    // Dynamic stats map
    val statValues = remember { mutableStateMapOf<String, Float>() }
    LaunchedEffect(position, initialEntry) {
        val keys = EntityConstants.statsForPosition(position)
        keys.forEach { key ->
            if (!statValues.containsKey(key)) {
                val fromEntry = initialEntry?.extractStat(key.replace(" ", ""))
                    ?: initialEntry?.extractStat(key)
                    ?: 0f
                // also try stats string with various key forms
                val fromStats = initialEntry?.stats?.split(",", ";")
                    ?.map { it.trim() }
                    ?.firstOrNull {
                        it.substringBefore(":").trim().equals(key, ignoreCase = true) ||
                            it.substringBefore(":").trim().replace(" ", "")
                                .equals(key.replace(" ", ""), ignoreCase = true)
                    }
                    ?.substringAfter(":")
                    ?.trim()
                    ?.toFloatOrNull()
                statValues[key] = fromStats ?: fromEntry
            }
        }
        // drop keys not for this position (except keep IQ always)
        val keep = keys.toSet()
        statValues.keys.filter { it !in keep }.toList().forEach { statValues.remove(it) }
    }

    var explainStat by remember { mutableStateOf<String?>(null) }
    var nameEditable by remember { mutableStateOf(!isEdit) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.White.copy(alpha = 0.35f),
        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        cursorColor = Color.White,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedLabelColor = Color.White.copy(alpha = 0.7f),
        unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF121212)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (nameEditable) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = {
                                Text("Name", color = Color.White.copy(alpha = 0.4f), fontFamily = LexendFontFamily)
                            },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = name.ifBlank { "Untitled" },
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { nameEditable = true }) {
                            Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    SectionTitle("IDENTITY")
                    OutlinedTextField(
                        value = customId,
                        onValueChange = { customId = it },
                        label = { Text("Custom ID", fontFamily = LexendFontFamily) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description", fontFamily = LexendFontFamily) },
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    // Tags
                    FieldLabel("TAGS")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = tagInput,
                            onValueChange = { tagInput = it },
                            placeholder = {
                                Text("Type a tag…", color = Color.White.copy(alpha = 0.4f), fontFamily = LexendFontFamily)
                            },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            if (tagInput.isBlank()) {
                                showTagPicker = true
                            } else {
                                val t = tagInput.trim()
                                if (t.isNotBlank() && t !in tags) tags = tags + t
                                tagInput = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                    }
                    if (tags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable {
                                        tags = tags - tag
                                    }
                                ) {
                                    Text(
                                        tag,
                                        fontFamily = LexendFontFamily,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    SectionTitle("DATE")
                    DateWheelRow(
                        day = day, month = month, year = year,
                        onDay = { day = it }, onMonth = { month = it }, onYear = { year = it }
                    )

                    Spacer(Modifier.height(12.dp))
                    SectionTitle("PLAYER")

                    // Position
                    SimpleDropdown(
                        label = "Position",
                        value = position,
                        options = EntityConstants.POSITIONS,
                        onSelect = {
                            position = it
                            // reset playstyle when position changes
                            playstyle = EntityConstants.playstylesForPosition(it).firstOrNull() ?: ""
                        },
                        colors = fieldColors
                    )
                    Spacer(Modifier.height(8.dp))

                    // Playstyle
                    SimpleDropdown(
                        label = "Playstyle",
                        value = playstyle.ifBlank {
                            EntityConstants.playstylesForPosition(position).firstOrNull() ?: ""
                        },
                        options = EntityConstants.playstylesForPosition(position),
                        onSelect = { playstyle = it },
                        colors = fieldColors
                    )
                    Spacer(Modifier.height(8.dp))

                    // Secondary positions (max 6)
                    FieldLabel("SECONDARY POSITIONS (max 6)")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EntityConstants.POSITIONS.filter { it != position }.forEach { pos ->
                            val selected = pos in secondary
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (selected) Color.White else Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.clickable {
                                    secondary = when {
                                        selected -> secondary - pos
                                        secondary.size < 6 -> secondary + pos
                                        else -> secondary
                                    }
                                }
                            ) {
                                Text(
                                    pos,
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (selected) Color.Black else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    SimpleDropdown(
                        label = "Nationality",
                        value = nationality,
                        options = listOf("") + availableNations,
                        onSelect = { nationality = it },
                        colors = fieldColors,
                        allowEmpty = true
                    )
                    Spacer(Modifier.height(8.dp))
                    SimpleDropdown(
                        label = "Club",
                        value = club,
                        options = listOf("") + availableClubs,
                        onSelect = { club = it },
                        colors = fieldColors,
                        allowEmpty = true
                    )

                    Spacer(Modifier.height(12.dp))
                    SectionTitle("SKILLS")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EntityConstants.PLAYER_SKILLS.forEach { skill ->
                            val selected = skill in skills
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (selected) Color(0xFF10B981) else Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.clickable {
                                    skills = if (selected) skills - skill else skills + skill
                                }
                            ) {
                                Text(
                                    skill,
                                    fontFamily = LexendFontFamily,
                                    fontSize = 12.sp,
                                    color = if (selected) Color.White else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    SectionTitle("STATS (0 – 100)")
                    Text(
                        EntityConstants.VALUE_GUIDE,
                        fontFamily = LexendFontFamily,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(8.dp))

                    val statKeys = EntityConstants.statsForPosition(position)
                    statKeys.chunked(2).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { key ->
                                DraggableStatField(
                                    label = key,
                                    value = statValues[key] ?: 0f,
                                    onValueChange = { statValues[key] = it },
                                    onLabelClick = { explainStat = key },
                                    modifier = Modifier.weight(1f),
                                    colors = fieldColors
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(6.dp))
                    }

                    // Overall preview
                    val overall = if (statKeys.isNotEmpty()) {
                        statKeys.map { statValues[it] ?: 0f }.average().toFloat()
                    } else 0f
                    Text(
                        "Overall: ${overall.roundToInt()}",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(Modifier.height(24.dp))
                }

                // Fixed bottom action bar: Delete | Cancel | Update/Save
                Surface(color = Color(0xFF0D0D0D), shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isEdit && onDelete != null) {
                            Button(
                                onClick = onDelete,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3F1515),
                                    contentColor = Color(0xFFEF4444)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Delete", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A2A2A),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (name.isBlank()) return@Button
                                val dateStr = "%02d/%02d/%04d".format(day, month, year)
                                val statsStr = EntityConstants.statsForPosition(position)
                                    .joinToString(", ") { k ->
                                        "$k:${(statValues[k] ?: 0f).roundToInt()}"
                                    }
                                val keys = EntityConstants.statsForPosition(position)
                                val computedOverall = if (keys.isNotEmpty()) {
                                    keys.map { statValues[it] ?: 0f }.average().toFloat()
                                } else 0f
                                val extra = mutableMapOf(
                                    "Type" to EntityType.Player.name,
                                    "Position" to position,
                                    "Playstyle" to playstyle,
                                    "Date" to dateStr,
                                    "Overall" to computedOverall.roundToInt().toString()
                                )
                                if (nationality.isNotBlank()) extra["Nationality"] = nationality
                                if (club.isNotBlank()) extra["Club"] = club
                                if (secondary.isNotEmpty()) extra["SecondaryPositions"] = secondary.joinToString(", ")
                                if (skills.isNotEmpty()) extra["Skills"] = skills.joinToString(", ")

                                onSave(
                                    DatabaseEntry(
                                        name = name.trim(),
                                        id = customId.trim(),
                                        description = description.trim(),
                                        stats = statsStr,
                                        tags = tags,
                                        extraFields = extra
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (isEdit) "Update" else "Save",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Stat explanation popup
    explainStat?.let { stat ->
        Dialog(onDismissRequest = { explainStat = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(32.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        stat,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        EntityConstants.statExplanation(stat),
                        fontFamily = LexendFontFamily,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        EntityConstants.VALUE_GUIDE,
                        fontFamily = LexendFontFamily,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { explainStat = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("OK", color = Color.White, fontFamily = LexendFontFamily)
                    }
                }
            }
        }
    }

    // Tag picker
    if (showTagPicker) {
        Dialog(onDismissRequest = { showTagPicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A1A1A),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(320.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Pick a tag",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        availableTags.forEach { tag ->
                            Text(
                                tag,
                                fontFamily = LexendFontFamily,
                                fontSize = 15.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (tag !in tags) tags = tags + tag
                                        showTagPicker = false
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        }
                        if (availableTags.isEmpty()) {
                            Text(
                                "No tags yet",
                                color = Color.White.copy(alpha = 0.5f),
                                fontFamily = LexendFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    colors: androidx.compose.material3.TextFieldColors,
    allowEmpty: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = value.ifBlank { if (allowEmpty) "— none —" else "" },
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontFamily = LexendFontFamily) },
            trailingIcon = {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = Color.White.copy(alpha = 0.6f)
                )
            },
            colors = colors,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        )
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            if (option.isBlank() && allowEmpty) "— none —" else option,
                            fontFamily = LexendFontFamily
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DateWheelRow(
    day: Int,
    month: Int,
    year: Int,
    onDay: (Int) -> Unit,
    onMonth: (Int) -> Unit,
    onYear: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        WheelColumn("Day", day, 1..31, onDay, Modifier.weight(1f))
        WheelColumn("Month", month, 1..12, onMonth, Modifier.weight(1f))
        WheelColumn("Year", year, 1990..2035, onYear, Modifier.weight(1.2f))
    }
}

@Composable
private fun WheelColumn(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        FieldLabel(label)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(value) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = if (dragAmount < -12) 1 else if (dragAmount > 12) -1 else 0
                        if (delta != 0) {
                            val next = (value + delta).coerceIn(range.first, range.last)
                            onChange(next)
                        }
                    }
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                Text(
                    "%02d".format(value).let { if (label == "Year") value.toString() else it },
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun DraggableStatField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onLabelClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.TextFieldColors
) {
    var text by remember { mutableStateOf("%.0f".format(value)) }
    var isDragging by remember { mutableStateOf(false) }
    var liveValue by remember { mutableStateOf(value) }

    LaunchedEffect(value, isDragging) {
        if (!isDragging) {
            liveValue = value
            text = "%.0f".format(value)
        }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .clickable(onClick = onLabelClick)
                .padding(bottom = 4.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { new ->
                    if (!isDragging) {
                        text = new
                        new.toFloatOrNull()?.let { onValueChange(it.coerceIn(0f, 100f)) }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(4.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDragging) Color(0xFF10B981) else Color.White.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(width = 36.dp, height = 52.dp)
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
                                val delta = -dragAmount / 4f
                                liveValue = (liveValue + delta).coerceIn(0f, 100f)
                                val snapped = liveValue.roundToInt().toFloat()
                                onValueChange(snapped)
                                text = "%.0f".format(snapped)
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
                        Icons.Default.KeyboardArrowUp, null,
                        tint = if (isDragging) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown, null,
                        tint = if (isDragging) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
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
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontFamily = LexendFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = Color.White.copy(alpha = 0.55f),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
