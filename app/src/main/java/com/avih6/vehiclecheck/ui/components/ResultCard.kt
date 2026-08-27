package com.avih6.vehiclecheck.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avih6.vehiclecheck.data.*

@Composable
fun ResultCard(
    vehicle: VehicleRecord,
    techSpec: VehicleTechnicalSpecRecord?,
    importerInfo: VehicleImporterPriceRecord?,
    extraHistory: VehicleExtraHistoryRecord?,
    formattedPlate: String,
    testStatus: TestStatus,
    hasDisabledPermit: Boolean,
    permitIssueDate: Long?,
    isOffRoad: Boolean,
    offRoadDate: String?,
    stats: ModelStatistics,
    recalls: List<VehicleRecallRestrictionRecord>,
    recallDetail: RecallDetailRecord?,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    isEngineeringEquipment: Boolean = false,
    equipmentDetails: EngineeringEquipmentRecord? = null,
    alternateEquipment: EngineeringEquipmentRecord? = null,
    alternateVehicle: VehicleRecord? = null,
    onToggleEquipment: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showStatsDialog by remember { mutableStateOf(false) }
    val tabs = if (isEngineeringEquipment) listOf("כללי", "מפרט") else listOf("כללי", "מפרט", "בטיחות", "סביבה", "סטטיסטיקה")

    val brandLogoUrl = remember(vehicle.make) {
        VehicleUtils.getBrandLogoUrl(vehicle.make)
    }

    if (showStatsDialog) {
        VehicleStatsDialog(
            vehicle = vehicle,
            stats = stats,
            onDismiss = { showStatsDialog = false }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. Alternate Equipment Banner (צמ"ה)
        alternateEquipment?.let { eq ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleEquipment() }
                    .handCursor(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFF9800).copy(alpha = 0.12f),
                border = BorderStroke(1.5.dp, Color(0xFFFF9800).copy(alpha = 0.8f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Construction,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "מספר רכב זהה קיים בכלי ציוד מכני הנדסי (צמ\"ה)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${eq.makeName ?: "צמ\"ה"} ${eq.modelName ?: ""} (${eq.vehicleType ?: ""}) — לחץ לצפייה",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 0. Alternate Motor Vehicle Banner
        alternateVehicle?.let { altV ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleEquipment() }
                    .handCursor(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "מספר רכב זהה קיים במאגר כלי הרכב המנועיים",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${altV.make ?: ""} ${altV.model ?: ""} (${altV.year ?: ""}) — לחץ לצפייה",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 0. Off-Road / Cancellation Alert Badge (if vehicle is cancelled)
        if (isOffRoad) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFC62828).copy(alpha = 0.15f),
                border = BorderStroke(1.5.dp, Color(0xFFC62828).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (!offRoadDate.isNullOrBlank()) "ירד מהכביש בתאריך: $offRoadDate" else "רכב זה ירד מהכביש (רישוי מבוטל)",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFC62828),
                        fontSize = 15.sp
                    )
                }
            }
        }

        // 0.1 Open Recall Restriction Alert Badge (if vehicle has open recall)
        if (recalls.isNotEmpty()) {
            val firstRecall = recalls.first()
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.12f),
                border = BorderStroke(1.5.dp, Color(0xFFD32F2F).copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "קריאת שירות (ריקול) פתוחה לרכב!",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD32F2F),
                            fontSize = 15.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    firstRecall.faultDescription?.let {
                        Text(
                            text = "תיאור התקלה: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    firstRecall.faultType?.let {
                        Text(
                            text = "מכלול תקלה: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    firstRecall.openDate?.let {
                        Text(
                            text = "תאריך פתיחה: ${VehicleUtils.formatDate(it)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    recallDetail?.repairMethod?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "הנחיות תיקון: $it",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!recallDetail?.telephone.isNullOrBlank() || !recallDetail?.importerName.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "יבואן: ${recallDetail?.importerName ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                recallDetail?.telephone?.let { phone ->
                                    Text(
                                        text = "📞 $phone",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "הערה: קריאת ריקול פתוחה מחייבת תיקון במוסך היבואן (ללא עלות) כתנאי לחידוש רישיון הרכב ומעבר טסט.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD32F2F),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 1. Vehicle 360 Showcase Image
        VehicleImageShowcase(
            hebrewMake = vehicle.make,
            modelName = vehicle.model,
            year = vehicle.year,
            color = vehicle.color
        )

        // 2. License Plate Badge & Action Buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: License Plate & Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Israeli License Plate Badge with IL flag
                    Surface(
                        color = Color(0xFFFFD54F),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(2.dp, Color.Black),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Surface(
                                color = Color(0xFF1565C0),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("IL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                    Text("ישראל", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                                }
                            }
                            Text(
                                text = formattedPlate,
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 21.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            HoverTooltipIconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val cleanDigits = (vehicle.licensePlate?.toString() ?: formattedPlate).filter { it.isDigit() }
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Plate", cleanDigits))
                                    Toast.makeText(context, "מספר רכב הועתק ללוח", Toast.LENGTH_SHORT).show()
                                },
                                tooltipText = "העתק מספר רכב",
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = "העתק מספר רכב",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Actions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HoverTooltipIconButton(
                            onClick = onToggleFavorite,
                            tooltipText = if (isFavorite) "הסר ממועדפים" else "הוסף למועדפים"
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HoverTooltipIconButton(
                            onClick = {
                                val shareText = buildComprehensiveShareText(
                                    vehicle, techSpec, importerInfo, extraHistory, formattedPlate, testStatus, hasDisabledPermit, permitIssueDate, isOffRoad, offRoadDate, recalls
                                )
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "שתף דוח רכב"))
                            },
                            tooltipText = "שתף דוח בדיקת רכב"
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Brand Emblem Badge (Centered & High Contrast)
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(6.dp)) {
                        AutoBrandLogo(
                            hebrewMake = vehicle.make,
                            isEngineeringEquipment = isEngineeringEquipment,
                            size = 78.dp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                val cleanMake = vehicle.make?.replace(" ארה\"ב", "")?.replace(" יפן", "")?.replace(" קוריאה", "") ?: vehicle.make.orEmpty()
                Text(
                    text = "$cleanMake ${vehicle.year ?: ""}".trim().ifBlank { "פרטי רכב" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                vehicle.model?.let { mod ->
                    if (mod.isNotBlank() && !mod.equals(cleanMake, ignoreCase = true)) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = mod.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Active / Off-Road Pill Badge
                Surface(
                    color = if (isOffRoad) Color(0xFFD32F2F).copy(alpha = 0.18f) else Color(0xFF2E7D32).copy(alpha = 0.18f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isOffRoad) Color(0xFFD32F2F) else Color(0xFF2E7D32))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isOffRoad) Icons.Default.Cancel else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isOffRoad) Color(0xFFFF5252) else Color(0xFF66BB6A),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isOffRoad) "רכב לא פעיל (ירד מהכביש)" else "רכב פעיל ברישיון",
                            color = if (isOffRoad) Color(0xFFFF5252) else Color(0xFF66BB6A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                vehicle.trimLevel?.let { trim ->
                    if (trim.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "רמת גימור: $trim",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Mileage in last test badge
                extraHistory?.lastTestMileage?.let { km ->
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "%,d - קילומטראז' מעודכן מהטסט האחרון".format(km),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // 3. Quick Status Cards Row (Disabled Permit, Ownership, Test, Recall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Disabled Permit
            StatusPill(
                title = "תג נכה",
                value = if (hasDisabledPermit) "פעיל ✅" else "ללא",
                isPositive = hasDisabledPermit,
                icon = Icons.AutoMirrored.Filled.Accessible,
                modifier = Modifier.weight(1f)
            )

            // Ownership
            val ownerStr = if (!vehicle.ownership.isNullOrBlank()) vehicle.ownership else "פרטי"
            val isCompany = ownerStr.contains("חברה") || ownerStr.contains("ליסינג") || ownerStr.contains("השכרה")
            StatusPill(
                title = "בעלות",
                value = ownerStr,
                isPositive = !isCompany,
                icon = if (isCompany) Icons.Default.Business else Icons.Default.Person,
                modifier = Modifier.weight(1f)
            )

            // Test Status
            val testPositive = testStatus is TestStatus.Valid
            val testTitle = when (testStatus) {
                is TestStatus.Valid -> "בתוקף"
                is TestStatus.ExpiringSoon -> "יפוג בקרוב"
                is TestStatus.Expired -> "לא בתוקף"
                is TestStatus.OffRoad -> "לא בתוקף"
                TestStatus.Unknown -> if (isOffRoad) "לא בתוקף" else "אין מידע"
            }
            StatusPill(
                title = "טסט",
                value = testTitle,
                isPositive = testPositive,
                icon = if (testPositive) Icons.Default.CalendarToday else Icons.Default.EventBusy,
                modifier = Modifier.weight(1f)
            )

            // Recall Pill
            val hasRecall = recalls.isNotEmpty()
            StatusPill(
                title = "ריקול",
                value = if (hasRecall) "פתוח ⚠️" else "תקין ✅",
                isPositive = !hasRecall,
                icon = if (hasRecall) Icons.Default.Warning else Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
        }

        // 4. Importer & Price Banner
        val price = importerInfo?.importerPrice
        val impName = importerInfo?.importerName
        if (price != null || impName != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("₪", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        price?.let {
                            Text(
                                text = "מחיר יבואן בעלייה לכביש: ₪%,d".format(it),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        impName?.let {
                            Text(
                                text = "יבואן: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 5. Navigation Tab Bar (כללי, מפרט, בטיחות, סביבה, סטטיסטיקה)
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            edgePadding = 0.dp,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // 6. Tab Content Switcher
        if (isEngineeringEquipment) {
            when (selectedTab) {
                0 -> EngineeringGeneralTabContent(vehicle, equipmentDetails, testStatus)
                1 -> EngineeringTechSpecContent(vehicle, equipmentDetails)
            }
        } else {
            when (selectedTab) {
                0 -> GeneralTabContent(
                    vehicle = vehicle,
                    techSpec = techSpec,
                    importerInfo = importerInfo,
                    extraHistory = extraHistory,
                    testStatus = testStatus,
                    hasDisabledPermit = hasDisabledPermit,
                    permitIssueDate = permitIssueDate,
                    recalls = recalls,
                    recallDetail = recallDetail,
                    stats = stats,
                    onShowAllCounts = { showStatsDialog = true }
                )
                1 -> TechSpecTabContent(vehicle, techSpec)
                2 -> SafetyTabContent(vehicle, techSpec)
                3 -> EnvironmentTabContent(vehicle, techSpec)
                4 -> StatisticsTabContent(vehicle, stats)
            }
        }
    }
}

@Composable
private fun GeneralTabContent(
    vehicle: VehicleRecord,
    techSpec: VehicleTechnicalSpecRecord?,
    importerInfo: VehicleImporterPriceRecord?,
    extraHistory: VehicleExtraHistoryRecord?,
    testStatus: TestStatus,
    hasDisabledPermit: Boolean,
    permitIssueDate: Long?,
    recalls: List<VehicleRecallRestrictionRecord>,
    recallDetail: RecallDetailRecord?,
    stats: ModelStatistics,
    onShowAllCounts: () -> Unit
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Active vs Inactive Same Model Vehicles Count Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "כמות כלי רכב הפעילים, הקיימים מאותו סוג הרכב",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "${stats.totalInactive} " + if (stats.totalInactive == 1) "לא פעיל" else "לא פעילים",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Circular Progress Ring
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { (stats.activePercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF0091EA),
                            trackColor = Color(0xFFEF5350),
                            strokeWidth = 6.dp,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.1f%%".format(stats.activePercentage),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "פעילים",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "${stats.totalActive} " + if (stats.totalActive == 1) "פעיל" else "פעילים",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF0091EA)
                    )
                }

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = onShowAllCounts) {
                    Text(
                        text = "הצג את כל הכמויות",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0091EA)
                    )
                }
            }
        }

        // MOT Test & License Dates Card (מועדי רישוי ומבחני טסט)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "מועדי רישוי ומבחני טסט",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Last test date (טסט אחרון)
                val lastTestFormatted = vehicle.lastTestDate?.let { VehicleUtils.formatDate(it) } 
                    ?: VehicleUtils.getEstimatedLastTestDate(vehicle.testExpiryDate, null)?.let { VehicleUtils.formatDate(it) }
                    ?: "אין מידע"
                SpecRow(
                    label = "טסט אחרון (מבחן רישוי אחרון):",
                    value = lastTestFormatted
                )

                // Next test / Expiry date (טסט הבא)
                val expiryFormatted = vehicle.testExpiryDate?.let { VehicleUtils.formatDate(it) } ?: "אין מידע"
                SpecRow(
                    label = "טסט הבא (תוקף רישיון רכב):",
                    value = expiryFormatted,
                    isHighlighted = vehicle.testExpiryDate != null
                )

                // Status duration diff badge (זמן שנותר / איחור)
                val testDiff = VehicleUtils.calculateDateDifferenceHebrew(vehicle.testExpiryDate)
                if (testDiff != null) {
                    val isValid = testStatus is TestStatus.Valid || testStatus is TestStatus.ExpiringSoon
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isValid) Color(0xFF2E7D32).copy(alpha = 0.12f) else Color(0xFFC62828).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = testDiff,
                            color = if (isValid) Color(0xFF4CAF50) else Color(0xFFFF5252),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Off-road cancellation date
                if (isOffRoad && !offRoadDate.isNullOrBlank()) {
                    val offRoadDiff = VehicleUtils.calculateDateDifferenceHebrew(offRoadDate)
                    SpecRow(
                        label = "מועד הורדה מהכביש (ביטול רישום):",
                        value = offRoadDate
                    )
                    if (offRoadDiff != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFC62828).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "מועד ביטול: $offRoadDiff",
                                color = Color(0xFFFF5252),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // On-road date (מועד עלייה לכביש)
                SpecRow(
                    label = "מועד עלייה לכביש:",
                    value = vehicle.onRoadDate?.let { VehicleUtils.formatDate(it) } ?: "אין מידע"
                )

                // First registration date (תאריך רישום ראשוני)
                SpecRow(
                    label = "תאריך רישום ראשוני:",
                    value = extraHistory?.firstRegistrationDate?.let { VehicleUtils.formatDate(it) } ?: "אין מידע"
                )
            }
        }

        // Fees & Vehicle Type Card (סוג רכב, בעלות ואגרות רישוי)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "סוג רכב, בעלות ואגרות רישוי",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Vehicle Type selector chips (פרטי P / מסחרי M)
                val isCommercial = vehicle.modelType == "M" || (vehicle.modelType?.contains("מסחרי", ignoreCase = true) == true)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "סוג רכב (ייעוד):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Private Chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isCommercial) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (!isCommercial) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("P", fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (!isCommercial) MaterialTheme.colorScheme.primary else Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Text("פרטי", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (!isCommercial) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray)
                            }
                        }

                        // Commercial Chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCommercial) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isCommercial) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("M", fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (isCommercial) MaterialTheme.colorScheme.primary else Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Text("מסחרי", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isCommercial) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Ownership (בעלות)
                val ownershipStr = vehicle.ownership ?: "פרטי"
                SpecRow("סוג בעלות רשומה:", ownershipStr)

                // Annual licensing fee
                val feeGroup = techSpec?.feeGroup ?: (vehicle.modelCode?.takeIf { it.length >= 2 }?.takeLast(1)?.toIntOrNull() ?: 4)
                val annualFee = VehicleUtils.calculateAnnualLicensingFee(feeGroup, vehicle.year)
                SpecRow("אגרת רישוי שנתית משוערת:", "₪%,d".format(annualFee), isHighlighted = true)

                Spacer(Modifier.height(8.dp))

                // Fee Group selector row (1 2 3 4 5 6 7)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "קבוצת אגרה:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..7).forEach { groupNum ->
                            val isSelected = groupNum == feeGroup
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$groupNum",
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                techSpec?.countryOfOrigin?.let {
                    if (it.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        CountrySpecRow("ארץ ייצור:", it)
                    }
                }
                vehicle.fuelType?.let {
                    if (it.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        FuelSpecRow("סוג דלק:", it)
                    }
                }
                vehicle.color?.let {
                    if (it.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        ColorSpecRow("צבע:", it)
                    }
                }
                extraHistory?.originality?.let {
                    if (it.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        SpecRow("מקוריות:", it)
                    }
                }
            }
        }

        // Vehicle Modifications Card (האם הותקן גפ"מ, שינוי צבע, שינוי צמיג, וו גרירה)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "שינויים, תוספות וגפ\"מ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                SpecRow("האם הותקנה מערכת גפ\"מ (גז):", if (extraHistory?.lpgInstalled == 1) "כן (מותקנת)" else "לא")
                SpecRow("האם בוצע שינוי צבע:", if (extraHistory?.colorChange == 1) "כן (שינוי רשום)" else "לא")
                SpecRow("האם בוצע שינוי במידת צמיג:", if (extraHistory?.tireChange == 1) "כן (מאושר ברישיון)" else "לא")
                val towHook = if ((techSpec?.towingCapacityWithBrakes ?: 0) > 0) "מורשה לגרירה (עד ${techSpec?.towingCapacityWithBrakes} ק\"ג)" else "ללא רישום וו גרירה"
                SpecRow("וו גרירה:", towHook)
            }
        }

        // Tires & Wheels Card
        val hasTireInfo = !vehicle.frontTire.isNullOrBlank() || !vehicle.rearTire.isNullOrBlank() || techSpec?.tpms == 1 || techSpec?.alloyWheels == 1
        if (hasTireInfo) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "מפרט צמיגים וגלגלים",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    vehicle.frontTire?.let {
                        if (it.isNotBlank()) SpecRow("צמיג קדמי מאושר:", it)
                    }
                    vehicle.rearTire?.let {
                        if (it.isNotBlank()) SpecRow("צמיג אחורי מאושר:", it)
                    }
                    if (techSpec?.tpms == 1) {
                        SpecRow("חיישני לחץ אוויר בצמיגים (TPMS):", "מותקן ומאושר")
                    }
                    if (techSpec?.alloyWheels == 1) {
                        SpecRow("ג'נטים / גלגלי סגסוגת קלה:", "כן")
                    }
                }
            }
        }

        // Identifiers & Codes Card with Copy
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "מספרי זיהוי וקודים רשמיים",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                vehicle.vin?.let { vin ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "מספר שלדה (VIN):", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = vin, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("VIN", vin))
                                    Toast.makeText(context, "מספר שלדה הועתק ללוח", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp).padding(start = 4.dp)
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy VIN", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                extraHistory?.engineNumber?.let { SpecRow("מספר מנוע:", it) }
                vehicle.engineModel?.let { SpecRow("דגם מנוע:", it) }
                vehicle.registrationDirective?.let { SpecRow("מספר הוראת רישום:", "$it") }
                vehicle.makeCode?.let { SpecRow("קוד תוצרת:", "$it") }
                vehicle.modelCd?.let { SpecRow("קוד דגם משרד התחבורה:", "$it") }
            }
        }

        // Disabled Permit Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = if (hasDisabledPermit) Color(0xFF00629E).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            border = BorderStroke(
                1.dp,
                if (hasDisabledPermit) Color(0xFF00629E).copy(alpha = 0.4f) else Color.Transparent
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Accessible,
                    contentDescription = if (hasDisabledPermit) "תו נכה פעיל" else "אין תו נכה",
                    tint = if (hasDisabledPermit) Color(0xFF00629E) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    val dateFormatted = VehicleUtils.formatPermitDate(permitIssueDate)
                    Text(
                        text = if (hasDisabledPermit) "נמצא תו נכה פעיל במאגר ✅" else "אין תו נכה רשום במאגר",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (hasDisabledPermit) Color(0xFF00629E) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (hasDisabledPermit && dateFormatted.isNotBlank()) "תאריך הפקת תג: $dateFormatted" else if (hasDisabledPermit) "רכב זה מופיע במאגר תגי הנכה של משרד התחבורה" else "לפי בדיקה צולבת במאגר תגי הנכה",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TechSpecTabContent(
    vehicle: VehicleRecord,
    techSpec: VehicleTechnicalSpecRecord?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Powertrain & Performance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "מנוע, ביצועים והנעה",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                techSpec?.horsepower?.let { hp ->
                    SpecRow("כוחות סוס:", "$hp כ\"ס", isHighlighted = true)
                }
                techSpec?.engineDisplacement?.let { cc ->
                    SpecRow("נפח מנוע:", "%,d סמ\"ק".format(cc))
                }
                val gearText = if (techSpec?.isAutomatic == 1) "אוטומטי" else if (techSpec?.isAutomatic == 0) "ידני" else null
                gearText?.let { SpecRow("תיבת הילוכים:", it) }

                val drive = techSpec?.driveType ?: if (vehicle.model?.contains("4X4", ignoreCase = true) == true) "4X4" else null
                drive?.let { SpecRow("הנעה:", it) }

                techSpec?.powertrainTech?.let {
                    if (it.isNotBlank()) SpecRow("טכנולוגיית הנעה:", it)
                }
                techSpec?.bodyType?.let {
                    if (it.isNotBlank()) SpecRow("סוג מרכב:", it)
                }
            }
        }

        // Dimensions, Weights & Capacity
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "מידות, משקלים וקיבולת",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val seats = techSpec?.seats
                val doors = techSpec?.doors
                if (seats != null || doors != null) {
                    SpecRow("מושבים ודלתות:", "${seats ?: "-"} מושבים • ${doors ?: "-"} דלתות")
                }

                techSpec?.airbags?.let { SpecRow("כריות אוויר:", "$it כריות אוויר") }
                techSpec?.electricWindows?.let { SpecRow("חלונות חשמל:", "$it") }
                techSpec?.totalWeight?.let { SpecRow("משקל כולל:", "%,d ק\"ג".format(it)) }
                techSpec?.towingCapacityWithBrakes?.let { SpecRow("כושר גרירה עם בלמים:", "%,d ק\"ג".format(it)) }
                techSpec?.towingCapacityWithoutBrakes?.let { SpecRow("כושר גרירה בלי בלמים:", "%,d ק\"ג".format(it)) }
            }
        }

        // Comfort & Equipment Checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "אבזור נוחות ומרכב",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                SafetySystemRow("מזגן מקורי", isPresent = (techSpec?.airConditioning ?: 1) == 1)
                SafetySystemRow("מערכת בלמי ABS", isPresent = (techSpec?.abs ?: 1) == 1)
                SafetySystemRow("בקרת יציבות אלקטרונית (ESP)", isPresent = (techSpec?.stabilityControl ?: 1) == 1)
                SafetySystemRow("הגה כוח", isPresent = (techSpec?.powerSteering ?: 1) == 1)
                SafetySystemRow("חישוקי מגנזיום / סגסוגת קלה", isPresent = techSpec?.alloyWheels == 1)
                SafetySystemRow("חלון בגג (סאן-רוף)", isPresent = techSpec?.sunroof == 1)
            }
        }
    }
}

@Composable
private fun SafetyTabContent(
    vehicle: VehicleRecord,
    techSpec: VehicleTechnicalSpecRecord?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Safety Level Score Banner & Visual Bar (0 to 8)
        val score = techSpec?.safetyEquipmentLevel ?: vehicle.safetyRating ?: 0
        val detailedScore = techSpec?.safetyScore

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "רמת אבזור בטיחותי: $score מתוך 8",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (detailedScore != null) {
                    Text(
                        text = "ניקוד בטיחותי: %.1f".format(detailedScore),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(14.dp))

                // 0-8 Colored Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    val colors = listOf(
                        Color(0xFFD32F2F), // 0 Red
                        Color(0xFFE64A19), // 1 Orange
                        Color(0xFFFFA000), // 2 Amber
                        Color(0xFFFBC02D), // 3 Yellow
                        Color(0xFF689F38), // 4 Light Green
                        Color(0xFF388E3C), // 5 Green
                        Color(0xFF00897B), // 6 Teal
                        Color(0xFF1976D2), // 7 Blue
                        Color(0xFF303F9F)  // 8 Dark Blue
                    )
                    (0..8).forEach { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(colors[index]),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$index",
                                color = Color.White,
                                fontWeight = if (score == index) FontWeight.Black else FontWeight.Normal,
                                fontSize = if (score == index) 14.sp else 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Full Active Safety Systems Checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "מערכות בטיחות אקטיביות",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                SafetySystemRow("בקרת סטייה מנתיב", isPresent = techSpec?.laneDepartureWarning == 1)
                SafetySystemRow("מערכת אקטיבית למניעת סטייה מנתיב", isPresent = techSpec?.activeLaneDeparture == 1)
                SafetySystemRow("בקרת שיוט אדפטיבית", isPresent = techSpec?.adaptiveCruise == 1)
                SafetySystemRow("מערכת עזר לבלמים", isPresent = techSpec?.brakeAssist == 1)
                SafetySystemRow("בלימה אוטומטית בנסיעה לאחור", isPresent = techSpec?.reverseAutoBraking == 1)
                SafetySystemRow("בלימת חירום לפני הולכי רגל ואופניים", isPresent = techSpec?.pedestrianBicycleEmergencyBrake == 1)
                SafetySystemRow("תאורה אוטומטית בנסיעה קדימה", isPresent = techSpec?.autoHeadlights == 1)
                SafetySystemRow("שליטה אוטומטית באורות גבוהים", isPresent = techSpec?.autoHighBeam == 1)
                SafetySystemRow("בקרת מהירות חכמה", isPresent = techSpec?.intelligentSpeedAssist == 1)
                SafetySystemRow("ניטור מרחק מלפנים", isPresent = techSpec?.forwardCollisionWarning == 1)
                SafetySystemRow("חיישני לחץ אוויר בצמיגים (TPMS)", isPresent = techSpec?.tpms == 1)
                SafetySystemRow("חיישני חגורות בטיחות", isPresent = techSpec?.seatbeltSensors == 1)
                SafetySystemRow("מצלמת רוורס", isPresent = techSpec?.reverseCamera == 1)
                SafetySystemRow("הכנה למנעולי אלכוהול", isPresent = techSpec?.alcoholLockReady == 1)
                SafetySystemRow("זיהוי מצב התקרבות מסוכנת", isPresent = techSpec?.dangerousApproachDetection == 1)
                SafetySystemRow("זיהוי הולכי רגל", isPresent = techSpec?.pedestrianDetection == 1)
                SafetySystemRow("זיהוי תמרורי תנועה", isPresent = techSpec?.trafficSignDetection == 1)
                SafetySystemRow("זיהוי בשטח נסתר (שטח מת)", isPresent = techSpec?.blindSpotDetection == 1)
                SafetySystemRow("מערכת אקטיבית למניעת התנגשות צד", isPresent = techSpec?.sideCollisionPrevention == 1)
                SafetySystemRow("זיהוי רכב דו גלגלי", isPresent = techSpec?.twoWheelerDetection == 1)
            }
        }
    }
}

@Composable
private fun EnvironmentTabContent(
    vehicle: VehicleRecord,
    techSpec: VehicleTechnicalSpecRecord?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val group = techSpec?.emissionGroup ?: vehicle.emissionGroup ?: 15

        // Pollution Group Visual Bar (1 to 15)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "קבוצת זיהום אוויר: $group מתוך 15",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                techSpec?.greenIndex?.let {
                    Text(
                        text = "מדד ירוק רשמי: %.1f".format(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(14.dp))

                // 1-15 Colored Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    (1..15).forEach { index ->
                        val color = when (index) {
                            in 1..4 -> Color(0xFF2E7D32)
                            in 5..8 -> Color(0xFF689F38)
                            in 9..11 -> Color(0xFFFBC02D)
                            in 12..13 -> Color(0xFFE64A19)
                            else -> Color(0xFFC62828)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$index",
                                color = Color.White,
                                fontWeight = if (group == index) FontWeight.Black else FontWeight.Normal,
                                fontSize = if (group == index) 13.sp else 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Environmental Standards Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "תקינה וממיר",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                techSpec?.standardType?.let { SpecRow("סוג תקינה:", it) }
                techSpec?.catalystType?.let { SpecRow("סוג ממיר:", it) }
            }
        }

        // Emissions Table (CO, CO2, NOX, HC, PM10)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "טבלת כמויות פליטה מהרכב",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("מזהם", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                    Text("עירוני", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("בין-עירוני", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("WLTP", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                EmissionRow("פחמן דו חמצני (CO2)", techSpec?.cityCO2?.let { "%.0f".format(it) }, techSpec?.hwayCO2?.let { "%.0f".format(it) }, techSpec?.wltpCO2?.let { "%.0f".format(it) })
                EmissionRow("פחמן חד חמצני (CO)", techSpec?.cityCO?.let { "%.4f".format(it) }, techSpec?.hwayCO?.let { "%.4f".format(it) }, techSpec?.wltpCO?.let { "%.1f".format(it) })
                EmissionRow("תחמוצות חנקן (NOX)", techSpec?.cityNOX?.let { "%.4f".format(it) }, techSpec?.hwayNOX?.let { "%.4f".format(it) }, techSpec?.wltpNOX?.let { "%.1f".format(it) })
                EmissionRow("פחמימנים (HC)", techSpec?.cityHC?.let { "%.4f".format(it) }, techSpec?.hwayHC?.let { "%.4f".format(it) }, techSpec?.wltpHC?.let { "%.1f".format(it) })
                EmissionRow("חלקיקים (PM10)", techSpec?.cityPM10?.let { "%.4f".format(it) }, techSpec?.hwayPM10?.let { "%.4f".format(it) }, techSpec?.wltpPM?.let { "%.2f".format(it) })
            }
        }
    }
}

@Composable
private fun EmissionRow(label: String, city: String?, hway: String?, wltp: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
        Text(text = city ?: "-", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(text = hway ?: "-", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(text = wltp ?: "-", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
private fun StatusPill(
    title: String,
    value: String,
    isPositive: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String, isHighlighted: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CountrySpecRow(label: String, country: String) {
    val flagUrl = VehicleUtils.getCountryFlagUrl(country)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (flagUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(flagUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = country,
                    modifier = Modifier
                        .size(width = 24.dp, height = 16.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    contentScale = ContentScale.FillBounds
                )
            }
            Text(
                text = country,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ColorSpecRow(label: String, colorName: String) {
    val (colorLong, borderLong) = VehicleUtils.getColorVisual(colorName)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(16.dp),
                shape = CircleShape,
                color = Color(colorLong),
                border = if (borderLong != null) BorderStroke(1.5.dp, Color(borderLong)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {}
            Text(
                text = colorName,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FuelSpecRow(label: String, fuelType: String) {
    val f = fuelType.trim().lowercase()
    val (icon, tint) = when {
        f.contains("חשמל") || f.contains("חשמלי") -> Pair(Icons.Default.Bolt, Color(0xFF00ACC1))
        f.contains("היבריד") || f.contains("היברידי") -> Pair(Icons.Default.BatteryChargingFull, Color(0xFF43A047))
        f.contains("סולר") || f.contains("דיזל") -> Pair(Icons.Default.LocalGasStation, Color(0xFFFFB300))
        f.contains("גז") || f.contains("גפ\"מ") -> Pair(Icons.Default.LocalGasStation, Color(0xFFE53935))
        else -> Pair(Icons.Default.LocalGasStation, Color(0xFF1E88E5))
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = fuelType,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = fuelType,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SafetySystemRow(title: String, isPresent: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        if (isPresent) {
            Icon(Icons.Default.CheckCircle, contentDescription = "קיים", tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
        } else {
            Icon(Icons.Default.Cancel, contentDescription = "לא קיים", tint = Color(0xFFC62828), modifier = Modifier.size(20.dp))
        }
    }
}

private fun buildComprehensiveShareText(
    vehicle: VehicleRecord,
    techSpec: VehicleTechnicalSpecRecord?,
    importerInfo: VehicleImporterPriceRecord?,
    extraHistory: VehicleExtraHistoryRecord?,
    formattedPlate: String,
    testStatus: TestStatus,
    hasDisabledPermit: Boolean,
    permitIssueDate: Long?,
    isOffRoad: Boolean,
    offRoadDate: String?,
    recalls: List<VehicleRecallRestrictionRecord>
): String {
    val statusStr = when (testStatus) {
        is TestStatus.Valid -> "טסט בתוקף (נותרו ${testStatus.daysLeft} ימים)"
        is TestStatus.ExpiringSoon -> "טסט יפוג בקרוב (נותרו ${testStatus.daysLeft} ימים)"
        is TestStatus.Expired -> "פג תוקף טסט (לפני ${testStatus.daysPassed} ימים)"
        is TestStatus.OffRoad -> "ירד מהכביש בתאריך: ${offRoadDate ?: "בוטל"}"
        TestStatus.Unknown -> "לא ידוע"
    }

    val offRoadAlert = if (isOffRoad) "🚫 *סטטוס רכב: ירד מהכביש (${offRoadDate ?: "בוטל"})*\n" else ""
    val recallAlert = if (recalls.isNotEmpty()) "⚠️ *קריאת ריקול פתוחה:* ${recalls.first().faultDescription ?: "קיימת קריאת שירות פתוחה"}\n" else ""
    val mileageStr = extraHistory?.lastTestMileage?.let { "\n🛣️ קילומטראז' בטסט: %,d ק\"מ".format(it) } ?: ""
    val hpStr = techSpec?.horsepower?.let { "\n🐎 כוחות סוס: $it כ\"ס" } ?: ""
    val ccStr = techSpec?.engineDisplacement?.let { "\n⚙️ נפח מנוע: %,d סמ\"ק".format(it) } ?: ""
    val driveStr = techSpec?.driveType?.let { "\n🚙 הנעה: $it" } ?: ""
    val priceStr = importerInfo?.importerPrice?.let { "\n💰 מחיר יבואן מקורי: ₪%,d".format(it) } ?: ""
    val permitDateStr = if (hasDisabledPermit && permitIssueDate != null) " (הופק: ${VehicleUtils.formatPermitDate(permitIssueDate)})" else ""

    return """
        📋 *דוח בדיקת רכב מקיף - מספר $formattedPlate*
        $offRoadAlert$recallAlert🚗 יצרן ודגם: ${vehicle.make ?: ""} ${vehicle.model ?: ""} (${vehicle.trimLevel ?: ""})
        📅 שנת ייצור: ${vehicle.year ?: "-"} (עלייה לכביש: ${vehicle.onRoadDate?.let { VehicleUtils.formatDate(it) } ?: "-"})$priceStr
        🛡️ סטטוס טסט: $statusStr
        🗓️ תוקף טסט: ${vehicle.testExpiryDate?.let { VehicleUtils.formatDate(it) } ?: "-"} (מבחן אחרון: ${vehicle.lastTestDate?.let { VehicleUtils.formatDate(it) } ?: "-"})$hpStr$ccStr$driveStr
        ⛽ דלק: ${vehicle.fuelType ?: "-"}$mileageStr
        🎨 צבע: ${vehicle.color ?: "-"}
        👤 בעלות: ${vehicle.ownership ?: "-"}
        🛡️ ציון בטיחות: ${vehicle.safetyRating ?: "-"}/8
        🍃 קבוצת זיהום: ${vehicle.emissionGroup ?: "-"}/15
        🔢 מספר שלדה: ${vehicle.vin ?: "-"}
        ♿ תו נכה: ${if (hasDisabledPermit) "פעיל ✅$permitDateStr" else "לא קיים ❌"}
        
        נבדק באפליקציית בדיקת רכב מתוך מאגר משרד התחבורה.
    """.trimIndent()
}

@Composable
private fun StatisticsTabContent(
    vehicle: VehicleRecord,
    stats: ModelStatistics
) {
    val context = LocalContext.current
    val brandLogoUrl = VehicleUtils.getBrandLogoUrl(vehicle.make)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Model & Brand Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(brandLogoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Brand Emblem",
                    modifier = Modifier.size(72.dp).padding(4.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${vehicle.make.orEmpty()} ${vehicle.model.orEmpty()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "שנת ייצור: ${vehicle.year ?: ""} • רמת גימור: ${vehicle.trimLevel ?: "סטנדרט"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Active vs Inactive Ratio Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "יחס כלי רכב פעילים מול מבוטלים / נגרעים מהכביש",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%,d".format(stats.totalInactive),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFFEF5350)
                        )
                        Text(
                            text = "לא פעילים (נגרעו)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { (stats.activePercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF0091EA),
                            trackColor = Color(0xFFEF5350).copy(alpha = 0.3f),
                            strokeWidth = 7.dp,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.1f%%".format(stats.activePercentage),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "פעילים",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%,d".format(stats.totalActive),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF0091EA)
                        )
                        Text(
                            text = "פעילים בכביש",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Breakdown By Year Table/Bars
        if (stats.breakdownByYear.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "התפלגות כלי רכב פעילים לפי שנתוני ייצור",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    stats.breakdownByYear.forEach { yearItem ->
                        val isCurrentVehicleYear = yearItem.year == vehicle.year
                        val maxCount = stats.breakdownByYear.maxOfOrNull { it.activeCount }?.coerceAtLeast(1) ?: 1
                        val fraction = (yearItem.activeCount.toFloat() / maxCount).coerceIn(0.05f, 1f)

                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "שנת ${yearItem.year}",
                                        fontWeight = if (isCurrentVehicleYear) FontWeight.Black else FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isCurrentVehicleYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isCurrentVehicleYear) {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "רכב זה",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = if (yearItem.activeCount == 1) "1 פעיל" else "%,d פעילים".format(yearItem.activeCount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isCurrentVehicleYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            // Progress bar
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = if (isCurrentVehicleYear) MaterialTheme.colorScheme.primary else Color(0xFF0091EA),
                                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineeringGeneralTabContent(
    vehicle: VehicleRecord,
    equipmentDetails: EngineeringEquipmentRecord?,
    testStatus: TestStatus
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Test Dates Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "מועדי רישוי ומבחני טסט",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                SpecRow(
                    label = "תוקף רישיון כלי (טסט עד):",
                    value = vehicle.testExpiryDate?.let { VehicleUtils.formatDate(it) } ?: "אין מידע",
                    isHighlighted = vehicle.testExpiryDate != null
                )
                SpecRow(
                    label = "תאריך רישום ראשוני:",
                    value = vehicle.onRoadDate?.let { VehicleUtils.formatDate(it) } ?: "אין מידע"
                )
            }
        }

        // Equipment Basic Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "פרטי כלי צמ\"ה",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                equipmentDetails?.vehicleType?.let {
                    SpecRow("סוג ציוד מכני:", it)
                }
                equipmentDetails?.horsepower?.let {
                    SpecRow("הספק מנוע:", "$it כ\"ס")
                }
                equipmentDetails?.driveType?.let {
                    FuelSpecRow("סוג הנעה:", it)
                }
                equipmentDetails?.restriction1?.let {
                    if (it.isNotBlank()) SpecRow("הגבלות תנועה:", it)
                }
            }
        }

        // Identifiers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "מספרי זיהוי ורישום רשמיים",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val context = LocalContext.current
                val vin = equipmentDetails?.vin ?: vehicle.vin
                vin?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "מספר שלדה (VIN):", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = it, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("VIN", it))
                                    android.widget.Toast.makeText(context, "מספר שלדה הועתק ללוח", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp).padding(start = 4.dp)
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy VIN", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineeringTechSpecContent(
    vehicle: VehicleRecord,
    equipmentDetails: EngineeringEquipmentRecord?
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "מפרט טכני ומשקלים",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                equipmentDetails?.modelName?.let {
                    SpecRow("דגם כלי:", it)
                }
                equipmentDetails?.makeName?.let {
                    SpecRow("שם יצרן:", it)
                }
                equipmentDetails?.horsepower?.let {
                    SpecRow("הספק מנוע:", "$it כ\"ס")
                }
                equipmentDetails?.totalWeightTon?.let {
                    SpecRow("משקל כולל:", "$it טון")
                }
                equipmentDetails?.weightTon?.let {
                    SpecRow("משקל עצמי:", "$it טון")
                }
                equipmentDetails?.liftingCapacityTon?.let {
                    SpecRow("כושר הרמה / מטען:", "$it טון")
                }
                equipmentDetails?.restriction1?.let {
                    if (it.isNotBlank()) SpecRow("הגבלת מהירות ותנועה:", it)
                }
            }
        }
    }
}

@Composable
fun AutoBrandLogo(
    hebrewMake: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 80.dp,
    isEngineeringEquipment: Boolean = false
) {
    var logoUrlIndex by remember(hebrewMake) { mutableIntStateOf(0) }
    val slug = remember(hebrewMake) { VehicleUtils.getBrandSlug(hebrewMake) }
    
    val urls = remember(slug) {
        val cleanSlug = slug.replace("-", "")
        val underscoreSlug = slug.replace("-", "_")
        listOf(
            "https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/$slug.png",
            "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/logos/optimized/$slug.png",
            "https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/thumb/$slug.png",
            "https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/local-logos/$slug.png",
            "https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/$cleanSlug.png",
            "https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/$underscoreSlug.png",
            "https://cdn.jsdelivr.net/gh/vehiclespecs/brand-logos@master/logos/png/$slug.png",
            "https://cdn.jsdelivr.net/gh/vehiclespecs/brand-logos@master/logos/svg/$slug.svg",
            "https://logo.clearbit.com/$slug.com",
            "https://logo.clearbit.com/$cleanSlug.com",
            "https://img.logo.dev/$slug.com?token=pk_anonymous",
            "https://www.google.com/s2/favicons?domain=$slug.com&sz=128",
            "https://icons.duckduckgo.com/ip3/$slug.com.ico"
        ).distinct()
    }

    if (logoUrlIndex >= urls.size || slug == "car") {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            shape = CircleShape,
            modifier = modifier.size(size)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isEngineeringEquipment) Icons.Default.Construction else Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(urls[logoUrlIndex])
                .setHeader("User-Agent", "VehicleCheckApp/1.0 (https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)")
                .crossfade(true)
                .build(),
            contentDescription = "סמל יצרן $hebrewMake",
            modifier = modifier
                .size(size)
                .padding(2.dp),
            contentScale = ContentScale.Fit,
            onError = {
                logoUrlIndex++
            }
        )
    }
}

