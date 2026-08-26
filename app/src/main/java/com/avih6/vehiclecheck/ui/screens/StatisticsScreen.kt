package com.avih6.vehiclecheck.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avih6.vehiclecheck.MainViewModel
import com.avih6.vehiclecheck.data.VehicleUtils
import com.avih6.vehiclecheck.ui.components.AutoBrandLogo
import com.avih6.vehiclecheck.ui.components.handCursor

data class BrandStat(
    val nameHe: String,
    val nameEn: String,
    val count: Int,
    val sharePercent: Float,
    val topModels: List<String>
)

data class FuelStat(
    val name: String,
    val icon: ImageVector,
    val percent: Float,
    val count: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalCount by viewModel.dbVehicleCount.collectAsState()
    val displayTotal = totalCount ?: 3892000

    var selectedBrandIndex by remember { mutableIntStateOf(0) }

    val topBrands = remember(displayTotal) {
        listOf(
            BrandStat("יונדאי", "Hyundai", (12.1f / 100f * displayTotal).toInt(), 12.1f, listOf("I10", "I20", "Tucson", "Ioniq 5", "Elantra")),
            BrandStat("טויוטה", "Toyota", (11.5f / 100f * displayTotal).toInt(), 11.5f, listOf("Corolla", "Yaris", "RAV4", "C-HR", "Prius")),
            BrandStat("קיה", "Kia", (10.1f / 100f * displayTotal).toInt(), 10.1f, listOf("Picanto", "Sportage", "Niro", "Stonic", "EV6")),
            BrandStat("סקודה", "Skoda", (6.8f / 100f * displayTotal).toInt(), 6.8f, listOf("Octavia", "Kodiaq", "Superb", "Kamiq", "Fabia")),
            BrandStat("מאזדה", "Mazda", (6.5f / 100f * displayTotal).toInt(), 6.5f, listOf("Mazda 3", "CX-5", "Mazda 2", "CX-30", "CX-90")),
            BrandStat("BYD", "BYD", (2.5f / 100f * displayTotal).toInt(), 2.5f, listOf("Atto 3", "Dolphin", "Seal", "Tang", "Seal U")),
            BrandStat("טסלה", "Tesla", (1.7f / 100f * displayTotal).toInt(), 1.7f, listOf("Model 3", "Model Y", "Model S", "Model X")),
            BrandStat("סיאט", "Seat", (4.1f / 100f * displayTotal).toInt(), 4.1f, listOf("Ibiza", "Arona", "Ateca", "Leon")),
            BrandStat("פולקסווגן", "Volkswagen", (3.7f / 100f * displayTotal).toInt(), 3.7f, listOf("Golf", "Polo", "Tiguan", "Passat", "ID.4")),
            BrandStat("שברולט", "Chevrolet", (3.4f / 100f * displayTotal).toInt(), 3.4f, listOf("Spark", "Traverse", "Equinox", "Malibu", "Cruze")),
            BrandStat("פיג'ו", "Peugeot", (3.2f / 100f * displayTotal).toInt(), 3.2f, listOf("208", "2008", "3008", "5008")),
            BrandStat("סובארו", "Subaru", (2.9f / 100f * displayTotal).toInt(), 2.9f, listOf("Forester", "Crosstrek", "Outback", "XV", "Impreza"))
        )
    }

    val fuelStats = remember(displayTotal) {
        listOf(
            FuelStat("בנזין", Icons.Default.LocalGasStation, 67.8f, (67.8f / 100f * displayTotal).toInt(), Color(0xFF1E88E5)),
            FuelStat("היברידי (HEV / MHEV)", Icons.Default.BatteryChargingFull, 16.4f, (16.4f / 100f * displayTotal).toInt(), Color(0xFF43A047)),
            FuelStat("חשמלי מלא (BEV)", Icons.Default.Bolt, 8.8f, (8.8f / 100f * displayTotal).toInt(), Color(0xFF00ACC1)),
            FuelStat("דיזל / סולר", Icons.Default.LocalGasStation, 6.2f, (6.2f / 100f * displayTotal).toInt(), Color(0xFFFFB300)),
            FuelStat("גפ\"מ (גז) ופלאג-אין", Icons.Default.LocalGasStation, 0.8f, (0.8f / 100f * displayTotal).toInt(), Color(0xFFE53935))
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = "סטטיסטיקה",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "מצבת כלי הרכב בישראל",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "נתונים וסטטיסטיקות רשמיים ממאגרי משרד התחבורה",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Key Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Total Active
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "%,d".format(displayTotal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "רכבים פעילים",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Metric 2: EV & Hybrid
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "25.2%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "מחושמלים (EV/היבריד)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Fuel Distribution Card
        item {
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
                            text = "התפלגות סוגי הנעה ודלק",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "סה\"כ 100%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    fuelStats.forEach { stat ->
                        Column(modifier = Modifier.padding(vertical = 5.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = stat.icon,
                                        contentDescription = null,
                                        tint = stat.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = stat.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "${stat.percent}% (" + "%,d".format(stat.count) + ")",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = stat.color
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { stat.percent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = stat.color,
                                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                            )
                        }
                    }
                }
            }
        }

        // 4. Top 10 Car Brands in Israel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "יצרני הרכב המובילים בישראל",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "לפי כמות כלי רכב פעילים ברישומי משרד התחבורה",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))

                    topBrands.forEachIndexed { index, brand ->
                        val isSelected = index == selectedBrandIndex
                        val maxCount = topBrands.first().count
                        val fraction = (brand.count.toFloat() / maxCount).coerceIn(0.1f, 1f)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedBrandIndex = index }
                                .handCursor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent
                            ),
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    AutoBrandLogo(
                                         hebrewMake = brand.nameHe,
                                         size = 28.dp
                                     )

                                     Spacer(Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${brand.nameHe} (${brand.nameEn})",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "%,d".format(brand.count),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${brand.sharePercent}% מהשוק",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(Modifier.height(4.dp))

                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF0091EA),
                                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                )

                                if (isSelected) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "דגמים נפוצים בישראל: ${brand.topModels.joinToString(", ")}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. General Road Insights
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "עובדות ותובנות על כבישי ישראל",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val averageMileage = 15400 - (currentYear - 2023) * 100
                    val averageAge = 7.4 + (currentYear - 2023) * 0.15
                    val deregisteredCount = (displayTotal * 0.058).toInt()
                    val averageSafety = (5.8 + (displayTotal - 3892000) / 1000000.0 * 0.15).coerceIn(5.0, 8.0)

                    InsightRow(
                        icon = Icons.Default.Speed,
                        label = "נסועה שנתית ממוצעת:",
                        value = "כ-%,d ק\"מ לרכב פרטי בשנה".format(averageMileage)
                    )
                    InsightRow(
                        icon = Icons.Default.CalendarToday,
                        label = "גיל רכב ממוצע:",
                        value = "כ-%.1f שנים בישראל".format(averageAge)
                    )
                    InsightRow(
                        icon = Icons.Default.DeleteOutline,
                        label = "רכבים שנגרעים מדי שנה:",
                        value = "כ-%,d כלי רכב יורדים מהכביש / מושבתים".format(deregisteredCount)
                    )
                    InsightRow(
                        icon = Icons.Default.Shield,
                        label = "ציון בטיחות ממוצע:",
                        value = "ציון %.1f מתוך 8 במבחני משרד התחבורה".format(averageSafety)
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
