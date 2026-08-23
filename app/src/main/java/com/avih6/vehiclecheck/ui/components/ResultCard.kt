package com.avih6.vehiclecheck.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avih6.vehiclecheck.R
import com.avih6.vehiclecheck.data.TestStatus
import com.avih6.vehiclecheck.data.VehicleExtraHistoryRecord
import com.avih6.vehiclecheck.data.VehicleRecord
import com.avih6.vehiclecheck.data.VehicleTechnicalSpecRecord
import com.avih6.vehiclecheck.data.VehicleUtils
import com.avih6.vehiclecheck.ui.theme.*

@Composable
fun ResultCard(
    vehicle: VehicleRecord,
    techSpec: VehicleTechnicalSpecRecord?,
    extraHistory: VehicleExtraHistoryRecord?,
    formattedPlate: String,
    testStatus: TestStatus,
    hasDisabledPermit: Boolean,
    permitIssueDate: Long?,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. MOT / Test Status Banner
        TestStatusCard(testStatus = testStatus, vehicle = vehicle)

        // 2. Main Vehicle Info & Model Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Plate & Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Israeli License Plate Badge
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
                            Text(
                                text = formattedPlate,
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                letterSpacing = 1.sp
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val cleanDigits = (vehicle.licensePlate?.toString() ?: formattedPlate).filter { it.isDigit() }
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Plate", cleanDigits))
                                    Toast.makeText(context, "מספר רכב הועתק ללוח", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(26.dp).padding(start = 6.dp)
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

                    // Action Icons: Favorite & Share
                    Row {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            val shareText = buildComprehensiveShareText(vehicle, techSpec, extraHistory, formattedPlate, testStatus, hasDisabledPermit, permitIssueDate)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "שתף דוח רכב"))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Make & Model Title
                val title = listOfNotNull(vehicle.make, vehicle.model).joinToString(" ").ifBlank { "פרטי רכב" }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                vehicle.trimLevel?.let { trim ->
                    if (trim.isNotBlank()) {
                        Text(
                            text = "רמת גימור: $trim",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Google Image Model Search Button
                OutlinedButton(
                    onClick = {
                        val searchQuery = "${vehicle.make ?: ""} ${vehicle.model ?: ""} ${vehicle.year ?: ""} ${vehicle.color ?: ""}".trim()
                        val url = "https://www.google.com/search?tbm=isch&q=" + Uri.encode(searchQuery)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("הצג תמונות דגם באינטרנט 🔍", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Core Specs Grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    SpecItem(
                        icon = Icons.Outlined.CalendarToday,
                        title = "שנת ייצור",
                        value = "${vehicle.year ?: "-"} ${vehicle.onRoadDate?.let { "($it)" } ?: ""}",
                        modifier = Modifier.weight(1f)
                    )
                    SpecItem(
                        icon = Icons.Outlined.Palette,
                        title = "צבע הרכב",
                        value = "${vehicle.color ?: "-"} ${vehicle.colorCode?.let { "(קוד $it)" } ?: ""}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    SpecItem(
                        icon = Icons.Outlined.LocalGasStation,
                        title = "סוג דלק",
                        value = vehicle.fuelType ?: "-",
                        modifier = Modifier.weight(1f)
                    )
                    SpecItem(
                        icon = Icons.Outlined.Person,
                        title = "סוג בעלות",
                        value = vehicle.ownership ?: "-",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Engine, Powertrain & Performance (כוחות סוס, נפח מנוע, גיר, הנעה, מרכב)
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
                    SpecRow(label = "כוחות סוס:", value = "$hp כ\"ס", isHighlighted = true)
                }

                techSpec?.engineDisplacement?.let { cc ->
                    SpecRow(label = "נפח מנוע:", value = "%,d סמ\"ק".format(cc))
                }

                val gearText = if (techSpec?.isAutomatic == 1) "אוטומטי" else if (techSpec?.isAutomatic == 0) "ידני" else null
                gearText?.let {
                    SpecRow(label = "תיבת הילוכים (גיר):", value = it)
                }

                val drive = techSpec?.driveType ?: if (vehicle.model?.contains("4X4", ignoreCase = true) == true) "4X4" else null
                drive?.let {
                    SpecRow(label = "טכנולוגיית הנעה:", value = it)
                }

                techSpec?.bodyType?.let { body ->
                    if (body.isNotBlank()) SpecRow(label = "סוג מרכב:", value = body)
                }

                val seats = techSpec?.seats
                val doors = techSpec?.doors
                if (seats != null || doors != null) {
                    val seatsStr = seats?.let { "$it מושבים" } ?: ""
                    val doorsStr = doors?.let { "$it דלתות" } ?: ""
                    val combined = listOf(seatsStr, doorsStr).filter { it.isNotBlank() }.joinToString(" • ")
                    SpecRow(label = "מושבים ודלתות:", value = combined)
                }
            }
        }

        // 4. Weights, Towing & Capacity
        if (techSpec != null && (techSpec.totalWeight != null || techSpec.towingCapacityWithBrakes != null || techSpec.airbags != null)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "משקלים, גרירה וקיבולת",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    techSpec.totalWeight?.let { weight ->
                        SpecRow(label = "משקל כולל:", value = "%,d ק\"ג".format(weight))
                    }

                    techSpec.towingCapacityWithBrakes?.let { tow ->
                        SpecRow(label = "כושר גרירה (עם בלמים):", value = "%,d ק\"ג".format(tow))
                    }

                    techSpec.towingCapacityWithoutBrakes?.let { towNo ->
                        SpecRow(label = "כושר גרירה (ללא בלמים):", value = "%,d ק\"ג".format(towNo))
                    }

                    techSpec.airbags?.let { bags ->
                        SpecRow(label = "מספר כריות אוויר:", value = "$bags כריות אוויר")
                    }

                    techSpec.electricWindows?.let { win ->
                        SpecRow(label = "חלונות חשמל:", value = "$win")
                    }
                }
            }
        }

        // 5. Active Safety Systems & Equipment (רשימת מערכות בטיחות עם V/X)
        if (techSpec != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "מערכות בטיחות ואבזור אקטיבי",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    SafetySystemRow(title = "בקרת שיוט אדפטיבית", isPresent = techSpec.adaptiveCruise == 1)
                    SafetySystemRow(title = "זיהוי הולכי רגל ובלימה", isPresent = techSpec.pedestrianDetection == 1)
                    SafetySystemRow(title = "זיהוי בשטח נסתר (שטח מת)", isPresent = techSpec.blindSpotDetection == 1)
                    SafetySystemRow(title = "זיהוי תמרורי תנועה", isPresent = techSpec.trafficSignDetection == 1)
                    SafetySystemRow(title = "בקרת סטייה מנתיב", isPresent = techSpec.laneDepartureWarning == 1)
                    SafetySystemRow(title = "מערכת עזר לבלימה", isPresent = techSpec.brakeAssist == 1)
                    SafetySystemRow(title = "מצלמת רוורס", isPresent = techSpec.reverseCamera == 1)
                    SafetySystemRow(title = "תאורה אוטומטית בנסיעה קדימה", isPresent = techSpec.autoHeadlights == 1)
                    SafetySystemRow(title = "חישוקי מגנזיום / סגסוגת קלה", isPresent = techSpec.alloyWheels == 1)
                    SafetySystemRow(title = "חלון בגג (סאן-רוף)", isPresent = techSpec.sunroof == 1)
                    SafetySystemRow(title = "הגה כוח", isPresent = techSpec.powerSteering == 1)
                }
            }
        }

        // 6. Mileage & Vehicle History Card (from data.gov.il)
        if (extraHistory != null && (extraHistory.lastTestMileage != null || extraHistory.originality != null || extraHistory.engineNumber != null)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "נתוני טסט, קילומטראז' והיסטוריה",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    extraHistory.lastTestMileage?.let { km ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "קילומטראז' בטסט האחרון:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "%,d ק\"מ".format(km), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    extraHistory.originality?.let { orig ->
                        if (orig.isNotBlank()) {
                            SpecRow(label = "מקוריות הרכב:", value = orig)
                        }
                    }

                    extraHistory.engineNumber?.let { engNum ->
                        if (engNum.isNotBlank()) {
                            SpecRow(label = "מספר מנוע:", value = engNum)
                        }
                    }
                }
            }
        }

        // 7. Technical, Safety & Environmental Specs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "מפרט טכני, בטיחות וזיהום",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Safety Rating Bar
                vehicle.safetyRating?.let { rating ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "רמת אבזור בטיחותי:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$rating / 8",
                                fontWeight = FontWeight.Bold,
                                color = if (rating >= 6) TestValidGreen else if (rating >= 3) TestExpiringSoonAmber else TestExpiredRed,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (rating >= 6) TestValidGreen else if (rating >= 3) TestExpiringSoonAmber else TestExpiredRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Emission Group
                val emission = techSpec?.emissionGroup ?: vehicle.emissionGroup
                emission?.let {
                    SpecRow(label = "קבוצת זיהום אוויר:", value = "$it / 15")
                }

                // Green Index
                techSpec?.greenIndex?.let { green ->
                    SpecRow(label = "מדד ירוק:", value = "%.0f".format(green))
                }

                // Tires
                val tireInfo = listOfNotNull(vehicle.frontTire, vehicle.rearTire).filter { it.isNotBlank() }.distinct().joinToString(" | ")
                if (tireInfo.isNotBlank()) {
                    SpecRow(label = "מידות צמיגים מאושרות:", value = tireInfo)
                }

                // Engine Model Code
                vehicle.engineModel?.let { engine ->
                    if (engine.isNotBlank()) SpecRow(label = "דגם מנוע:", value = engine)
                }

                // VIN Number with copy
                vehicle.vin?.let { vin ->
                    if (vin.isNotBlank()) {
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
                }

                // Codes
                vehicle.registrationDirective?.let {
                    SpecRow(label = "הוראת רישום:", value = "$it")
                }
                vehicle.makeCode?.let {
                    SpecRow(label = "מספר תוצרת:", value = "$it")
                }
                vehicle.modelCd?.let {
                    SpecRow(label = "מספר דגם:", value = "$it")
                }
            }
        }

        // 8. Disabled Permit Badge & Issue Date
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
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
                    imageVector = if (hasDisabledPermit) Icons.Default.Accessible else Icons.Outlined.Info,
                    contentDescription = null,
                    tint = if (hasDisabledPermit) Color(0xFF00629E) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
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
private fun TestStatusCard(testStatus: TestStatus, vehicle: VehicleRecord) {
    val (statusColor, statusTitle, statusSubtitle, icon) = when (testStatus) {
        is TestStatus.Valid -> {
            Tuple4(
                TestValidGreen,
                "טסט בתוקף",
                "נותרו עוד ${testStatus.daysLeft} ימים עד למבחן הרישוי הבא",
                Icons.Default.CheckCircle
            )
        }
        is TestStatus.ExpiringSoon -> {
            Tuple4(
                TestExpiringSoonAmber,
                "טסט יפוג בקרוב!",
                "נותרו ${testStatus.daysLeft} ימים בלבד לתוקף הטסט",
                Icons.Default.Warning
            )
        }
        is TestStatus.Expired -> {
            Tuple4(
                TestExpiredRed,
                "פג תוקף הטסט!",
                "הטסט פג תוקף לפני ${testStatus.daysPassed} ימים",
                Icons.Default.Error
            )
        }
        TestStatus.Unknown -> {
            Tuple4(
                MaterialTheme.colorScheme.onSurfaceVariant,
                "סטטוס טסט לא ידוע",
                "לא נמצא תאריך תוקף במאגר",
                Icons.Default.HelpOutline
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f)),
        border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(statusColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = statusSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Expiry Date & Last Test Date Row
            if (vehicle.testExpiryDate != null || vehicle.lastTestDate != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = statusColor.copy(alpha = 0.2f))
                
                vehicle.testExpiryDate?.let { expiry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "תוקף רישיון רכב (טסט):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = expiry,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                vehicle.lastTestDate?.let { lastTest ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "מבחן רישוי אחרון שבוצע:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = lastTest,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
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

@Composable
private fun SpecItem(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

private fun buildComprehensiveShareText(
    vehicle: VehicleRecord,
    techSpec: VehicleTechnicalSpecRecord?,
    extraHistory: VehicleExtraHistoryRecord?,
    formattedPlate: String,
    testStatus: TestStatus,
    hasDisabledPermit: Boolean,
    permitIssueDate: Long?
): String {
    val statusStr = when (testStatus) {
        is TestStatus.Valid -> "טסט בתוקף (נותרו ${testStatus.daysLeft} ימים)"
        is TestStatus.ExpiringSoon -> "טסט יפוג בקרוב (נותרו ${testStatus.daysLeft} ימים)"
        is TestStatus.Expired -> "פג תוקף טסט (לפני ${testStatus.daysPassed} ימים)"
        TestStatus.Unknown -> "לא ידוע"
    }

    val mileageStr = extraHistory?.lastTestMileage?.let { "\n🛣️ קילומטראז' בטסט: %,d ק\"מ".format(it) } ?: ""
    val hpStr = techSpec?.horsepower?.let { "\n🐎 כוחות סוס: $it כ\"ס" } ?: ""
    val ccStr = techSpec?.engineDisplacement?.let { "\n⚙️ נפח מנוע: %,d סמ\"ק".format(it) } ?: ""
    val driveStr = techSpec?.driveType?.let { "\n🚙 הנעה: $it" } ?: ""
    val permitDateStr = if (hasDisabledPermit && permitIssueDate != null) " (הופק: ${VehicleUtils.formatPermitDate(permitIssueDate)})" else ""

    return """
        📋 *דוח בדיקת רכב מקיף - מספר $formattedPlate*
        🚗 יצרן ודגם: ${vehicle.make ?: ""} ${vehicle.model ?: ""} (${vehicle.trimLevel ?: ""})
        📅 שנת ייצור: ${vehicle.year ?: "-"} (עלייה לכביש: ${vehicle.onRoadDate ?: "-"})
        🛡️ סטטוס טסט: $statusStr
        🗓️ תוקף טסט: ${vehicle.testExpiryDate ?: "-"} (מבחן אחרון: ${vehicle.lastTestDate ?: "-"})$hpStr$ccStr$driveStr
        ⛽ דלק: ${vehicle.fuelType ?: "-"}$mileageStr
        🎨 צבע: ${vehicle.color ?: "-"}
        👤 בעלות: ${vehicle.ownership ?: "-"}
        🛡️ ציון בטיחות: ${vehicle.safetyRating ?: "-"} / 8
        🍃 קבוצת זיהום: ${vehicle.emissionGroup ?: "-"} / 15
        🔢 מספר שלדה: ${vehicle.vin ?: "-"}
        ♿ תו נכה: ${if (hasDisabledPermit) "פעיל ✅$permitDateStr" else "לא קיים ❌"}
        
        נבדק באפליקציית בדיקת רכב מתוך מאגר משרד התחבורה.
    """.trimIndent()
}