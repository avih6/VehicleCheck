package com.avih6.vehiclecheck.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.avih6.vehiclecheck.data.DtcCodeInfo
import com.avih6.vehiclecheck.data.DtcRepository
import com.avih6.vehiclecheck.data.DtcSeverity

@Composable
fun DtcLookupDialog(
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<DtcCodeInfo?>(null) }
    val focusManager = LocalFocusManager.current

    val liveSearchResults by remember(query) {
        derivedStateOf {
            if (query.isNotBlank()) DtcRepository.searchCodes(query) else emptyList()
        }
    }

    val popularCodes = remember {
        listOf("P0300", "P0420", "P0171", "P0128", "P0700", "C0035", "B0001", "U0100")
    }

    fun performLookup(code: String) {
        val clean = code.trim().uppercase()
        if (clean.isNotBlank()) {
            query = clean
            result = DtcRepository.lookupCode(clean)
            focusManager.clearFocus()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                    Text(
                        text = "פענוח קודי תקלה (DTC / OBD2)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.size(48.dp))
                }

                Spacer(Modifier.height(10.dp))

                // Safety Warning Banner
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "אזהרה: אין לבדוק תקלות בזמן נהיגה! בעת הופעת תקלה יש לעצור במקום בטוח בצד הדרך.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5D4037),
                            lineHeight = 15.sp
                        )
                    }
                }

                // Input Field
                OutlinedTextField(
                    value = query,
                    onValueChange = { input ->
                        query = input.uppercase().take(10)
                        if (query.length >= 2) {
                            val match = DtcRepository.searchCodes(query).firstOrNull()
                            if (match != null) {
                                result = match
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("חיפוש לייב של קוד תקלה או מילת מפתח") },
                    placeholder = { Text("P0300, P0420, חמצן, misfire...") },
                    leadingIcon = {
                        Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = ""; result = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = { performLookup(query) }),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(10.dp))

                // Quick Popular Code Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(popularCodes) { code ->
                        FilterChip(
                            selected = query == code,
                            onClick = { performLookup(code) },
                            label = { Text(code, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Live Suggestions List
                if (query.isNotBlank() && liveSearchResults.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "תוצאות חיפוש מהירות (${liveSearchResults.size}):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            liveSearchResults.forEach { item ->
                                val sevColor = Color(item.severity.colorHex)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { performLookup(item.code) }
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = item.code,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = item.titleHe,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = item.categoryHe,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(sevColor, CircleShape)
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Lookup Result Display
                if (result != null) {
                    val info = result!!
                    val severityColor = Color(info.severity.colorHex)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = BorderStroke(1.5.dp, severityColor.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = severityColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = info.severity.titleHe,
                                        color = severityColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = info.code,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = info.titleHe,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = info.titleEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "מערכת: ${info.categoryHe}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            // Description
                            Text(
                                text = "תיאור התקלה:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = info.descriptionHe,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Symptoms
                            if (info.symptomsHe.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "תסמינים אפשריים ברכב:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                info.symptomsHe.forEach { symptom ->
                                    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("• ", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                        Text(symptom, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            // Possible Causes
                            if (info.possibleCausesHe.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "סיבות נפוצות לתקלה:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                info.possibleCausesHe.forEach { cause ->
                                    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("• ", fontWeight = FontWeight.Black, color = Color(0xFFE64A19))
                                        Text(cause, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            // Solutions
                            if (info.solutionsHe.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "דרכי טיפול מומלצות:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                info.solutionsHe.forEach { sol ->
                                    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("✔ ", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        Text(sol, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                }

                // Educational / OBD2 Guide Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "איך להשיג קוד תקלה מהרכב? (מדריך OBD2)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        ObdStepItem(
                            stepNumber = "1",
                            title = "איתור שקע ה-OBD2 ברכב",
                            description = "בכל רכב משנת 2000 ומעלה קיים שקע דיאגנוסטיקה בעל 16 פינים, הממוקם בדרך כלל מתחת להגה או סמוך לדוושות הנהג."
                        )

                        ObdStepItem(
                            stepNumber = "2",
                            title = "חיבור סורק OBD2 (למשל ELM327)",
                            description = "מחברים מתאם Bluetooth / Wi-Fi אל שקע ה-OBD2 ומסובבים את מפתח הרכב למצב סוויץ' (ON) מבלי להניע."
                        )

                        ObdStepItem(
                            stepNumber = "3",
                            title = "קריאת הקוד באמצעות אפליקציה",
                            description = "מתחברים דרך הטלפון לאפליקציית סריקה (כמו Car Scanner או Torque) ולוחצים על סריקת תקלות (Read Fault Codes)."
                        )

                        ObdStepItem(
                            stepNumber = "4",
                            title = "פענוח הקוד באפליקציה",
                            description = "מעתיקים את קוד התקלה (למשל P0420) לתיבת החיפוש כאן למעלה ומקבלים הסבר מקיף, סיבות אפשריות ודרכי טיפול!"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ObdStepItem(stepNumber: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}