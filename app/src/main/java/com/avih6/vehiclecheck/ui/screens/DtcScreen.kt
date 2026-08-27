package com.avih6.vehiclecheck.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avih6.vehiclecheck.data.DtcCodeInfo
import com.avih6.vehiclecheck.data.DtcRepository
import com.avih6.vehiclecheck.data.DtcSeverity

@Composable
fun DtcScreen(
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by remember { mutableStateOf("") }
    var selectedResult by remember { mutableStateOf<DtcCodeInfo?>(null) }

    val liveSearchResults by remember(query) {
        derivedStateOf {
            if (query.isNotBlank()) DtcRepository.searchCodes(query) else emptyList()
        }
    }

    val popularCodes = remember {
        listOf("P0300", "P0420", "P0171", "P0128", "P0700", "C0035", "B0001", "U0100")
    }

    fun selectCode(code: String) {
        query = code
        selectedResult = DtcRepository.lookupCode(code)
        keyboardController?.hide()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Critical Safety Warning Banner
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = BorderStroke(1.dp, Color(0xFFFFB74D)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "אזהרת בטיחות חשובה לנהג",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = "אין להשתמש באפליקציה או לבדוק תקלות בזמן נהיגה! בעת הופעת נורת אזהרה, עצור תמיד במקום בטוח בצד הדרך, כבה את המנוע, ורק אז בצע בדיקה או פנה לסיוע מקצועי.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5D4037),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Search Input Field
        OutlinedTextField(
            value = query,
            onValueChange = { input ->
                query = input.uppercase().take(10)
                if (query.length >= 2) {
                    val match = DtcRepository.searchCodes(query).firstOrNull()
                    if (match != null) {
                        selectedResult = match
                    }
                } else if (query.isEmpty()) {
                    selectedResult = null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("חיפוש לייב של קוד תקלה או מילת מפתח") },
            placeholder = { Text("למשל: P0300, P0420, חמצן, misfire...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = ""; selectedResult = null }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = {
                if (query.isNotBlank()) selectCode(query)
            }),
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
                    selected = (selectedResult?.code == code),
                    onClick = { selectCode(code) },
                    label = { Text(code, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 1. Initial State: Guide appears when nothing is searched/selected
        if (selectedResult == null && query.isBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
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
                        description = "מעתיקים את קוד התקלה (למשל P0420) לתיבת החיפוש כאן למעלה או לוחצים על אחד מהקודים הנפוצים כדי לקבל הסבר מלא!"
                    )
                }
            }
        }

        // 2. Selected Code Full Breakdown Result Display (Replaces guide upon selection)
        if (selectedResult != null) {
            val info = selectedResult!!
            val severityColor = Color(info.severity.colorHex)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                border = BorderStroke(1.5.dp, severityColor.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = info.code,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            color = severityColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, severityColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = info.severity.titleHe,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = severityColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = info.titleHe,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = info.titleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    // Description
                    Text(
                        text = "פירוט התקלה:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = info.descriptionHe,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Symptoms
                    if (info.symptomsHe.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "תסמינים אופייניים ברכב:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        info.symptomsHe.forEach { sym ->
                            Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("• ", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text(sym, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Possible Causes
                    if (info.possibleCausesHe.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "גורמים אפשריים לתקלה:",
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