package com.avih6.vehiclecheck.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import com.avih6.vehiclecheck.data.VehicleRecord
import com.avih6.vehiclecheck.ui.theme.*

@Composable
fun ResultCard(
    vehicle: VehicleRecord,
    formattedPlate: String,
    testStatus: TestStatus,
    hasDisabledPermit: Boolean,
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

        // 2. Main Vehicle Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Plate & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // License Plate Badge (Israeli style)
                    Surface(
                        color = Color(0xFFFFD54F),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = formattedPlate,
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    // Action Icons
                    Row {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            val shareText = buildShareText(vehicle, formattedPlate, testStatus, hasDisabledPermit)
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

                Spacer(Modifier.height(12.dp))

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
                        title = "צבע",
                        value = vehicle.color ?: "-",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    SpecItem(
                        icon = Icons.Outlined.LocalGasStation,
                        title = "סוג דלק",
                        value = vehicle.fuelType ?: "-",
                        modifier = Modifier.weight(1f)
                    )
                    SpecItem(
                        icon = Icons.Outlined.Person,
                        title = "בעלות",
                        value = vehicle.ownership ?: "-",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Technical & Safety Specs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.spec_technical),
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
                vehicle.emissionGroup?.let { emission ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "קבוצת זיהום אוויר:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$emission / 15",
                            fontWeight = FontWeight.Bold,
                            color = if (emission <= 4) TestValidGreen else if (emission <= 10) TestExpiringSoonAmber else TestExpiredRed
                        )
                    }
                }

                // Tires
                val tireInfo = listOfNotNull(vehicle.frontTire, vehicle.rearTire).filter { it.isNotBlank() }.distinct().joinToString(" | ")
                if (tireInfo.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "מידות צמיגים:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = tireInfo, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Engine Code
                vehicle.engineModel?.let { engine ->
                    if (engine.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "דגם מנוע:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = engine, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // VIN Number with copy
                vehicle.vin?.let { vin ->
                    if (vin.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "מספר שלדה:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            }
        }

        // 4. Disabled Permit Badge
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = if (hasDisabledPermit) Color(0xFF00629E).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(
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
                    Text(
                        text = if (hasDisabledPermit) "נמצא תו נכה פעיל במאגר" else "אין תו נכה רשום במאגר",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (hasDisabledPermit) Color(0xFF00629E) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (hasDisabledPermit) "רכב זה מופיע במאגר תגי הנכה הפעילים של משרד התחבורה" else "לפי בדיקה צולבת במאגר תגי הנכה",
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
        border = androidx.compose.foundation.BorderStroke(1.5.dp, statusColor.copy(alpha = 0.6f))
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

            // Expiry Date Row
            vehicle.testExpiryDate?.let { expiry ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = statusColor.copy(alpha = 0.2f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
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

private fun buildShareText(
    vehicle: VehicleRecord,
    formattedPlate: String,
    testStatus: TestStatus,
    hasDisabledPermit: Boolean
): String {
    val statusStr = when (testStatus) {
        is TestStatus.Valid -> "טסט בתוקף (נותרו ${testStatus.daysLeft} ימים)"
        is TestStatus.ExpiringSoon -> "טסט יפוג בקרוב (נותרו ${testStatus.daysLeft} ימים)"
        is TestStatus.Expired -> "פג תוקף טסט (לפני ${testStatus.daysPassed} ימים)"
        TestStatus.Unknown -> "לא ידוע"
    }

    return """
        📋 *דוח בדיקת רכב - מספר $formattedPlate*
        🚗 יצרן ודגם: ${vehicle.make ?: ""} ${vehicle.model ?: ""}
        📅 שנת ייצור: ${vehicle.year ?: "-"} (עלייה לכביש: ${vehicle.onRoadDate ?: "-"})
        🛡️ סטטוס טסט: $statusStr
        🗓️ תוקף טסט: ${vehicle.testExpiryDate ?: "-"}
        ⛽ דלק: ${vehicle.fuelType ?: "-"}
        🎨 צבע: ${vehicle.color ?: "-"}
        👤 בעלות: ${vehicle.ownership ?: "-"}
        🔢 מספר שלדה: ${vehicle.vin ?: "-"}
        ♿ תו נכה: ${if (hasDisabledPermit) "פעיל" else "לא קיים"}
        
        נבדק באפליקציית בדיקת רכב מתוך מאגר משרד התחבורה.
    """.trimIndent()
}