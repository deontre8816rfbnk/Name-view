package com.example.ui.components

import android.content.Intent

import android.graphics.BitmapFactory

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.graphics.asImageBitmap

import androidx.compose.foundation.layout.aspectRatio

import androidx.compose.foundation.Image

import androidx.activity.result.contract.ActivityResultContracts

import androidx.activity.compose.rememberLauncherForActivityResult

import android.net.Uri

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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

/**
 * Unified add/edit dialog for Manager, Club, and Nation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntityEditDialog(
    entityType: EntityType,
    initialEntry: DatabaseEntry?,
    availableNations: List<String> = emptyList(),
    availableTags: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (DatabaseEntry) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isEdit = initialEntry != null
    val titleLabel = when (entityType) {
        EntityType.Manager -> "Manager"
        EntityType.Club -> "Club"
        EntityType.Nation -> "Nation"
        else -> "Entity"
    }

    var name by remember { mutableStateOf(initialEntry?.name ?: "") }
    var customId by remember { mutableStateOf(initialEntry?.id ?: "") }
    var description by remember { mutableStateOf(initialEntry?.description ?: "") }
    var tags by remember { mutableStateOf(initialEntry?.tags?.toList() ?: emptyList()) }
    var tagInput by remember { mutableStateOf("") }
    var showTagPicker by remember { mutableStateOf(false) }

    var day by remember {
        mutableStateOf(initialEntry?.date?.split("/")?.getOrNull(0)?.toIntOrNull() ?: 1)
    }
    var month by remember {
        mutableStateOf(initialEntry?.date?.split("/")?.getOrNull(1)?.toIntOrNull() ?: 1)
    }
    var year by remember {
        mutableStateOf(initialEntry?.date?.split("/")?.getOrNull(2)?.toIntOrNull() ?: 2024)
    }

    // Manager-specific
    var nationality by remember { mutableStateOf(initialEntry?.nationality ?: "") }
    var managerPlaystyles by remember {
        mutableStateOf(
            (initialEntry?.extraFields?.get("Playstyles") ?: "")
                .split(",").map { it.trim() }.filter { it.isNotBlank() }
        )
    }
    var managerSkills by remember {
        mutableStateOf(initialEntry?.skills?.toList() ?: emptyList())
    }
    val managerStatValues = remember { mutableStateMapOf<String, Float>() }
    LaunchedEffect(Unit) {
        EntityConstants.MANAGER_STATS.forEach { key ->
            managerStatValues[key] = initialEntry?.extractStat(key) ?: 0f
        }
    }

    // Club-specific
    var teamStrength by remember {
        mutableStateOf(initialEntry?.extraFields?.get("TeamStrength") ?: "")
    }
    var clubStarRating by remember {
        mutableStateOf(initialEntry?.extraFields?.get("ClubStarRating") ?: "3")
    }
    var teamPlaystyle by remember {
        mutableStateOf(initialEntry?.extraFields?.get("TeamPlaystyleProficiency") ?: "")
    }
    var collectiveCondition by remember {
        mutableStateOf(initialEntry?.extraFields?.get("CollectiveCondition") ?: "C")
    }

    // Nation-specific (player count is calculated later; show stored value)
    var playerCountNote by remember {
        mutableStateOf(initialEntry?.extraFields?.get("PlayerCount") ?: "")
    }

    // Flag / Logo local URI
    var imagePath by remember {
        mutableStateOf(
            initialEntry?.extraFields?.get("FlagPath")
                ?: initialEntry?.extraFields?.get("LogoPath")
                ?: ""
        )
    }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }
            imagePath = uri.toString()
        }
    }

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
            modifier = Modifier.fillMaxSize().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF121212)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (isEdit) "Edit $titleLabel" else "New $titleLabel",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    // Unified fields
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name", fontFamily = LexendFontFamily) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
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
                    Text("TAGS", fontFamily = LexendFontFamily, fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = tagInput,
                            onValueChange = { tagInput = it },
                            placeholder = { Text("Tag…", color = Color.White.copy(alpha = 0.4f)) },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            if (tagInput.isBlank()) showTagPicker = true
                            else {
                                val t = tagInput.trim()
                                if (t.isNotBlank() && t !in tags) tags = tags + t
                                tagInput = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                    }
                    if (tags.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                            tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable { tags = tags - tag }
                                ) {
                                    Text(tag, fontFamily = LexendFontFamily, fontSize = 13.sp, color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("DATE", fontFamily = LexendFontFamily, fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
                    EntityDateWheels(day, month, year, { day = it }, { month = it }, { year = it })

                    // Type-specific sections
                    when (entityType) {
                        EntityType.Manager -> {
                            Spacer(Modifier.height(12.dp))
                            Text("MANAGER", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White.copy(alpha = 0.45f))
                            Spacer(Modifier.height(8.dp))
                            EntitySimpleDropdown("Nationality", nationality, listOf("") + availableNations, { nationality = it }, fieldColors)

                            Spacer(Modifier.height(8.dp))
                            Text("PLAYSTYLES", fontFamily = LexendFontFamily, fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
                            val allPlaystyles = listOf(
                                "Possession Game", "Quick Counter", "Long Ball Counter",
                                "Out Wide", "Long Ball", "Balanced"
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                allPlaystyles.forEach { ps ->
                                    val sel = ps in managerPlaystyles
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (sel) Color.White else Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier.clickable {
                                            managerPlaystyles = if (sel) managerPlaystyles - ps else managerPlaystyles + ps
                                        }
                                    ) {
                                        Text(ps, fontFamily = LexendFontFamily, fontSize = 12.sp,
                                            color = if (sel) Color.Black else Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text("SKILLS", fontFamily = LexendFontFamily, fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                EntityConstants.MANAGER_SKILLS.forEach { skill ->
                                    val sel = skill in managerSkills
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (sel) Color(0xFF10B981) else Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier.clickable {
                                            managerSkills = if (sel) managerSkills - skill else managerSkills + skill
                                        }
                                    ) {
                                        Text(skill, fontFamily = LexendFontFamily, fontSize = 12.sp, color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text("STATS", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White.copy(alpha = 0.45f))
                            EntityConstants.MANAGER_STATS.forEach { key ->
                                EntityStatRow(
                                    label = key,
                                    value = managerStatValues[key] ?: 0f,
                                    onValueChange = { managerStatValues[key] = it },
                                    colors = fieldColors
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                        }

                        EntityType.Club -> {
                            Spacer(Modifier.height(12.dp))
                            Text("CLUB", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White.copy(alpha = 0.45f))
                            Spacer(Modifier.height(8.dp))
                            ImagePickBlock(
                                imagePath = imagePath,
                                label = "Club logo",
                                onPick = { imagePicker.launch(arrayOf("image/*")) },
                                onClear = { imagePath = "" }
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = teamStrength,
                                onValueChange = { teamStrength = it },
                                label = { Text("Team Strength (1000–3100+)", fontFamily = LexendFontFamily) },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            EntitySimpleDropdown(
                                "Star Rating", clubStarRating,
                                listOf("1", "2", "3", "4", "5"),
                                { clubStarRating = it }, fieldColors
                            )
                            Spacer(Modifier.height(8.dp))
                            EntitySimpleDropdown(
                                "Team Playstyle", teamPlaystyle,
                                listOf("", "Possession Game", "Quick Counter", "Long Ball Counter", "Out Wide", "Long Ball"),
                                { teamPlaystyle = it }, fieldColors
                            )
                            Spacer(Modifier.height(8.dp))
                            EntitySimpleDropdown(
                                "Collective Condition", collectiveCondition,
                                listOf("A", "B", "C", "D", "E"),
                                { collectiveCondition = it }, fieldColors
                            )
                        }

                        EntityType.Nation -> {
                            Spacer(Modifier.height(12.dp))
                            Text("NATION", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White.copy(alpha = 0.45f))
                            Spacer(Modifier.height(8.dp))
                            ImagePickBlock(
                                imagePath = imagePath,
                                label = "Nation flag",
                                onPick = { imagePicker.launch(arrayOf("image/*")) },
                                onClear = { imagePath = "" }
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Player count and position breakdown are calculated from players with this nationality.",
                                fontFamily = LexendFontFamily,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            if (playerCountNote.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Stored player count: $playerCountNote",
                                    fontFamily = LexendFontFamily,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }

                        else -> {}
                    }

                    Spacer(Modifier.height(24.dp))
                }

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
                            if (name.isBlank()) return@Button
                            val dateStr = "%02d/%02d/%04d".format(day, month, year)
                            val extra = mutableMapOf(
                                "Type" to entityType.name,
                                "Date" to dateStr
                            )
                            var statsStr = ""

                            when (entityType) {
                                EntityType.Manager -> {
                                    if (nationality.isNotBlank()) extra["Nationality"] = nationality
                                    if (managerPlaystyles.isNotEmpty()) extra["Playstyles"] = managerPlaystyles.joinToString(", ")
                                    if (managerSkills.isNotEmpty()) extra["Skills"] = managerSkills.joinToString(", ")
                                    statsStr = EntityConstants.MANAGER_STATS.joinToString(", ") { k ->
                                        "$k:${(managerStatValues[k] ?: 0f).roundToInt()}"
                                    }
                                }
                                EntityType.Club -> {
                                    if (teamStrength.isNotBlank()) extra["TeamStrength"] = teamStrength
                                    extra["ClubStarRating"] = clubStarRating
                                    if (teamPlaystyle.isNotBlank()) extra["TeamPlaystyleProficiency"] = teamPlaystyle
                                    extra["CollectiveCondition"] = collectiveCondition
                                    if (imagePath.isNotBlank()) extra["LogoPath"] = imagePath
                                }
                                EntityType.Nation -> {
                                    if (playerCountNote.isNotBlank()) extra["PlayerCount"] = playerCountNote
                                    if (imagePath.isNotBlank()) extra["FlagPath"] = imagePath
                                }
                                else -> {}
                            }

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
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEdit) "Update" else "Save", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showTagPicker) {
        Dialog(onDismissRequest = { showTagPicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A1A1A),
                modifier = Modifier.fillMaxWidth().padding(24.dp).height(320.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Pick a tag", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    availableTags.forEach { tag ->
                        Text(
                            tag, fontFamily = LexendFontFamily, fontSize = 15.sp, color = Color.White,
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (tag !in tags) tags = tags + tag
                                showTagPicker = false
                            }.padding(vertical = 10.dp)
                        )
                    }
                    if (availableTags.isEmpty()) {
                        Text("No tags yet", color = Color.White.copy(alpha = 0.5f), fontFamily = LexendFontFamily)
                    }
                }
            }
        }
    }
}

@Composable
private fun EntitySimpleDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    colors: androidx.compose.material3.TextFieldColors
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = value.ifBlank { "— none —" },
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontFamily = LexendFontFamily) },
            trailingIcon = {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null, tint = Color.White.copy(alpha = 0.6f)
                )
            },
            colors = colors,
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(if (option.isBlank()) "— none —" else option, fontFamily = LexendFontFamily) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun EntityDateWheels(
    day: Int, month: Int, year: Int,
    onDay: (Int) -> Unit, onMonth: (Int) -> Unit, onYear: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        EntityWheel("Day", day, 1..31, onDay, Modifier.weight(1f))
        EntityWheel("Month", month, 1..12, onMonth, Modifier.weight(1f))
        EntityWheel("Year", year, 1990..2035, onYear, Modifier.weight(1.2f))
    }
}

@Composable
private fun EntityWheel(
    label: String, value: Int, range: IntRange, onChange: (Int) -> Unit, modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontFamily = LexendFontFamily, fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth().pointerInput(value) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    val delta = if (dragAmount < -12) 1 else if (dragAmount > 12) -1 else 0
                    if (delta != 0) onChange((value + delta).coerceIn(range.first, range.last))
                }
            }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                Text(
                    if (label == "Year") value.toString() else "%02d".format(value),
                    fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White
                )
                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EntityStatRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    colors: androidx.compose.material3.TextFieldColors
) {
    var text by remember { mutableStateOf("%.0f".format(value)) }
    var isDragging by remember { mutableStateOf(false) }
    var live by remember { mutableStateOf(value) }
    LaunchedEffect(value, isDragging) {
        if (!isDragging) { live = value; text = "%.0f".format(value) }
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontFamily = LexendFontFamily, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.width(140.dp))
        OutlinedTextField(
            value = text,
            onValueChange = {
                if (!isDragging) {
                    text = it
                    it.toFloatOrNull()?.let { v -> onValueChange(v.coerceIn(0f, 100f)) }
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
            modifier = Modifier.size(width = 36.dp, height = 48.dp).pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { isDragging = true; live = value },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        live = (live - dragAmount / 4f).coerceIn(0f, 100f)
                        val s = live.roundToInt().toFloat()
                        onValueChange(s)
                        text = "%.0f".format(s)
                    }
                )
            }
        ) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}


@Composable
private fun ImagePickBlock(
    imagePath: String,
    label: String,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(imagePath) {
        if (imagePath.isBlank()) null
        else try {
            context.contentResolver.openInputStream(Uri.parse(imagePath))?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) { null }
    }

    Column {
        Text(label, fontFamily = LexendFontFamily, fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
        Spacer(Modifier.height(6.dp))
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .aspectRatio(1f)
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onPick) {
                    Text("Change", color = Color.White, fontFamily = LexendFontFamily)
                }
                TextButton(onClick = onClear) {
                    Text("Remove", color = Color(0xFFEF4444), fontFamily = LexendFontFamily)
                }
            }
        } else {
            Button(
                onClick = onPick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Insert picture", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold)
            }
        }
    }
}
