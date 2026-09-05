package com.avih6.vehiclecheck.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
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
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by remember { mutableStateOf("") }
    var selectedResult by remember { mutableStateOf<DtcCodeInfo?>(null) }
    var isGuideExpanded by remember { mutableStateOf(false) }

    val liveSearchResults by remember(query) {
        derivedStateOf {
            DtcRepository.searchCodes(query)
        }
    }

    val popularCodes = remember {
        listOf("P0300", "P0420", "P0171", "P0128", "P0700", "C0035", "B0001", "U0100")
    }

    fun selectCode(code: String) {
        selectedResult = DtcRepository.lookupCode(code)
        query = ""
        keyboardController?.hide()
    }

    fun copyDtcDetails(info: DtcCodeInfo) {
        val shareText = buildString {
            appendLine("קוד תקלה: ${info.code}")
            appendLine("תיאור: ${info.titleHe} (${info.titleEn})")
            appendLine("רמת חומרה: ${info.severity.titleHe}")
            appendLine("מערכת: ${info.categoryHe}")
            appendLine()
            appendLine(info.descriptionHe)
            if (info.symptomsHe.isNotEmpty()) {
                appendLine("\nתסמינים ברכב:")
                info.symptomsHe.forEach { appendLine("• $it") }
            }
            if (info.possibleCausesHe.isNotEmpty()) {
                appendLine("\nגורמים אפשריים:")
                info.possibleCausesHe.forEach { appendLine("• $it") }
            }
            if (info.solutionsHe.isNotEmpty()) {
                appendLine("\nדרכי טיפול:")
                info.solutionsHe.forEach { appendLine("✔ $it") }
            }
            appendLine("\nשותף מאפליקציית בודק רכב (VehicleCheck)")
        }
        clipboardManager.setText(AnnotatedString(shareText))
        Toast.makeText(context, "קוד התקלה הועתק ללוח", Toast.LENGTH_SHORT).show()
    }

    fun shareDtcDetails(info: DtcCodeInfo) {
        val shareText = buildString {
            appendLine("🚗 פירוט קוד תקלה מהרכב: ${info.code}")
            appendLine("📌 ${info.titleHe} (${info.titleEn})")
            appendLine("⚠️ חומרה: ${info.severity.titleHe}")
            appendLine("🔧 מערכת: ${info.categoryHe}")
            appendLine()
            appendLine(info.descriptionHe)
            if (info.symptomsHe.isNotEmpty()) {
                appendLine("\nתסמינים:")
                info.symptomsHe.forEach { appendLine("• $it") }
            }
            if (info.possibleCausesHe.isNotEmpty()) {
                appendLine("\nגורמים אפשריים:")
                info.possibleCausesHe.forEach { appendLine("• $it") }
            }
            if (info.solutionsHe.isNotEmpty()) {
                appendLine("\nדרכי טיפול מומלצות:")
                info.solutionsHe.forEach { appendLine("✔ $it") }
            }
            appendLine("\nנשלח מ-VehicleCheck")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "פירוט תקלת רכב ${info.code}")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "שיתוף תקלת רכב"))
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
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
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
                query = input.uppercase().take(50)
                if (query.isBlank()) {
                    selectedResult = null
                } else {
                    val exact = DtcRepository.lookupCode(query.trim())
                    if (exact != null) {
                        selectedResult = exact
                    } else if (selectedResult != null && !selectedResult!!.code.equals(query.trim(), ignoreCase = true)) {
                        selectedResult = null
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("חיפוש קוד תקלה או מילת מפתח") },
            placeholder = { Text("למשל: P0300, P0420, חמצן, misfire...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        selectedResult = null
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "נקה חיפוש")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController?.hide()
                val exact = DtcRepository.lookupCode(query.trim())
                if (exact != null) {
                    selectedResult = exact
                }
            }),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(10.dp))



        // Quick Popular Codes Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(popularCodes) { code ->
                val isSelected = (selectedResult?.code == code)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedResult = null
                        } else {
                            selectCode(code)
                        }
                    },
                    label = { Text(code, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Expandable OBD2 Diagnostic Guide Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isGuideExpanded = !isGuideExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "איך להשיג קוד תקלה מהרכב? (מדריך OBD2)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isGuideExpanded) "לחץ לסגירת המדריך" else "לחץ לפתיחת מדריך 4 שלבים פשוטים",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = { isGuideExpanded = !isGuideExpanded }) {
                        Icon(
                            imageVector = if (isGuideExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isGuideExpanded) "כווץ מדריך" else "הרחב מדריך",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isGuideExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
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
        }

        Spacer(Modifier.height(14.dp))

        // Selected Code Full Breakdown Result Display
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

                        Spacer(Modifier.width(10.dp))

                        Surface(
                            color = severityColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, severityColor.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(severityColor, CircleShape)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = info.severity.titleHe,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = severityColor,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        IconButton(onClick = { selectedResult = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "סגור כרטיס",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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

                    Spacer(Modifier.height(6.dp))

                    // System Category chip
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = "מערכת: ${info.categoryHe}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Action buttons: Copy & Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { copyDtcDetails(info) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("העתק", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = { shareDtcDetails(info) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("שתף למוסך", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(10.dp))

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
                        modifier = Modifier.padding(vertical = 4.dp),
                        lineHeight = 18.sp
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

        // Live Search Results / Code Suggestions List
        if (selectedResult == null && liveSearchResults.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (query.isNotBlank()) "תוצאות חיפוש (${liveSearchResults.size})" else "קודי תקלות במערכת (${liveSearchResults.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (selectedResult != null) {
                    TextButton(onClick = { selectedResult = null }) {
                        Text("הצג את כל הרשימה", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                liveSearchResults.forEach { item ->
                    val isCurrent = (selectedResult?.code == item.code)
                    val itemColor = Color(item.severity.colorHex)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedResult = item
                                keyboardController?.hide()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isCurrent) 1.5.dp else 1.dp,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = item.code,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.titleHe,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.titleEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(itemColor, CircleShape)
                            )

                            Spacer(Modifier.width(6.dp))

                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Not found or invalid code card
        if (selectedResult == null && query.isNotBlank() && liveSearchResults.isEmpty()) {
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "קוד תקלה לא נמצא",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "הערך שהוזן (\"$query\") אינו מוכר כקוד תקלה או אינו בפורמט תקני של OBD2.\n\nקוד תקני מורכב מאות אחת (P למנוע, C לשלדה, B למרכב, U לרשת) ואחריה 4 תווים (למשל: P0420, P0300, B0010, C0040).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}