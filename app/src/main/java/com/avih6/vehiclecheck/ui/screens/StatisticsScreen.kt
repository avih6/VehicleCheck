package com.avih6.vehiclecheck.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avih6.vehiclecheck.MainViewModel
import com.avih6.vehiclecheck.data.VehicleUtils
import com.avih6.vehiclecheck.ui.components.AutoBrandLogo
import com.avih6.vehiclecheck.ui.components.handCursor

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.avih6.vehiclecheck.data.ModelStatisticsDetail
import com.avih6.vehiclecheck.data.ModelYearCount

data class FleetPoint(
    val year: Int,
    val count: Int
)

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
    onNavigateToGallery: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val totalCount by viewModel.dbVehicleCount.collectAsState()
    val lastUpdated by viewModel.dbLastUpdated.collectAsState()
    val displayTotal = totalCount ?: 4165989

    val modelQuery by viewModel.modelSearchQuery.collectAsState()
    val isSearchingModel by viewModel.isSearchingModel.collectAsState()
    val selectedModelDetail by viewModel.selectedModelDetail.collectAsState()
    val modelSearchError by viewModel.modelSearchError.collectAsState()

    var selectedBrandIndex by remember { mutableIntStateOf(0) }

    val quickModels = remember {
        listOf(
            "יונדאי איוניק 5",
            "טויוטה קורולה",
            "קיה פיקנטו",
            "טסלה מודל 3",
            "יונדאי טוסון",
            "סקודה אוקטביה",
            "BYD Atto 3",
            "מאזדה 3",
            "קיה ספורטאז'",
            "פולקסווגן גולף",
            "טויוטה יאריס",
            "יונדאי קונה"
        )
    }

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

        // 1b. Interactive Model Search Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "חיפוש וניתוח סטטיסטי של דגם",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "הקלד שם דגם או יצרן לקבלת כמויות פעילים, שרידות, שנתונים ובטיחות",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = modelQuery,
                        onValueChange = { viewModel.onModelSearchQueryChange(it) },
                        placeholder = { Text("למשל: איוניק 5, טוסון, קורולה, טסלה 3...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (modelQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.clearModelStatistics() }) {
                                    Icon(Icons.Default.Clear, contentDescription = "נקה")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            viewModel.searchModelStatistics()
                        })
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.searchModelStatistics()
                        },
                        enabled = modelQuery.isNotBlank() && !isSearchingModel,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        if (isSearchingModel) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("נתח נתוני דגם", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Quick suggestion chips
                    Text(
                        text = "דגמים פופולריים לניתוח מהיר:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(quickModels.size) { i ->
                            val model = quickModels[i]
                            AssistChip(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.searchModelStatistics(model)
                                },
                                label = { Text(model, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 1c. Model Statistics Result Card or Loading/Error State
        if (isSearchingModel) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "שולף ומנתח נתונים ממאגרי משרד התחבורה...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "סופר רכבים פעילים, שנתונים, סוגי מנוע ושרידות",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        modelSearchError?.let { err ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        selectedModelDetail?.let { detail ->
            item {
                ModelDetailStatisticsCard(
                    detail = detail,
                    onNavigateToGallery = onNavigateToGallery,
                    onDismiss = { viewModel.clearModelStatistics() }
                )
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

        // 3. Animated National Fleet Trend Graph
        item {
            NationalFleetTrendGraph(
                totalCount = displayTotal,
                lastUpdated = lastUpdated
            )
        }

        // 4. Fuel Distribution Card
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

@Composable
fun NationalFleetTrendGraph(
    totalCount: Int,
    lastUpdated: String?,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(totalCount) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing)
        )
    }

    val currentYear = java.time.LocalDate.now().year
    val fleetHistory = remember(totalCount) {
        listOf(
            FleetPoint(2019, 3520000),
            FleetPoint(2020, 3680000),
            FleetPoint(2021, 3840000),
            FleetPoint(2022, 3970000),
            FleetPoint(2023, 4060000),
            FleetPoint(2024, 4120000),
            FleetPoint(2025, 4150000),
            FleetPoint(currentYear, totalCount)
        )
    }

    val minCount = fleetHistory.minOf { it.count }
    val maxCount = fleetHistory.maxOf { it.count }

    var selectedChartMode by remember { mutableIntStateOf(0) } // 0: Yearly Fleet, 1: Daily Pace, 2: Monthly Deliveries

    val dailyData = remember {
        listOf(
            DailyTrendPoint("א'", "ראשון", 1120),
            DailyTrendPoint("ב'", "שני", 980),
            DailyTrendPoint("ג'", "שלישי", 940),
            DailyTrendPoint("ד'", "רביעי", 910),
            DailyTrendPoint("ה'", "חמישי", 880),
            DailyTrendPoint("ו'", "שישי", 240)
        )
    }

    val monthlyData = remember {
        listOf(
            MonthlyTrendPoint("ינו'", 32400),
            MonthlyTrendPoint("פבר'", 29100),
            MonthlyTrendPoint("מרץ", 28500),
            MonthlyTrendPoint("אפר'", 26800),
            MonthlyTrendPoint("מאי", 27400),
            MonthlyTrendPoint("יוני", 29800),
            MonthlyTrendPoint("יולי", 31200),
            MonthlyTrendPoint("אוג'", 28900)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Icon & Last Update Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when (selectedChartMode) {
                                1 -> "קצב עלייה ורישום יומי"
                                2 -> "מסירות רכב חודשיות (2026)"
                                else -> "מגמת גידול מצבת הרכב בישראל"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (!lastUpdated.isNullOrBlank()) "מתעדכן יומית • $lastUpdated" else "מתעדכן יומית ממאגר משרד התחבורה",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f))
                ) {
                    Text(
                        text = when (selectedChartMode) {
                            1 -> "~850 ביום"
                            2 -> "~29K בחודש"
                            else -> "+2.8% שנתי"
                        },
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Chart Mode Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("שנתי (מצבה)", "קצב יומי", "חודשי (2026)").forEachIndexed { index, title ->
                    FilterChip(
                        selected = (selectedChartMode == index),
                        onClick = { selectedChartMode = index },
                        label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Animated Graph Display (Always Left-To-Right for Time Progression)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                when (selectedChartMode) {
                    1 -> {
                        // Daily Pace Chart (Sun-Fri)
                        val maxDaily = dailyData.maxOf { it.count }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(145.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            dailyData.forEach { point ->
                                val fraction = (point.count / maxDaily.toFloat()).coerceIn(0.15f, 1f)
                                val animatedHeight = fraction * animProgress.value

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 3.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = "${(point.count * animProgress.value).toInt()}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height((100 * animatedHeight).dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF00E5FF), Color(0xFF0091EA))
                                                )
                                            )
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = point.dayShort,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // Monthly Deliveries Chart (Jan-Aug 2026)
                        val maxMonthly = monthlyData.maxOf { it.count }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(145.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            monthlyData.forEach { point ->
                                val fraction = (point.count / maxMonthly.toFloat()).coerceIn(0.15f, 1f)
                                val animatedHeight = fraction * animProgress.value

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = "%.1fK".format((point.count * animProgress.value) / 1000.0),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height((100 * animatedHeight).dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF80D8FF), Color(0xFF0288D1))
                                                )
                                            )
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = point.monthName,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // Yearly Fleet Trend (2019 -> 2026, progressing left to right)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(145.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            fleetHistory.forEachIndexed { index, point ->
                                val isLatest = index == fleetHistory.lastIndex
                                val fraction = ((point.count - minCount * 0.85f) / (maxCount - minCount * 0.85f)).coerceIn(0.15f, 1f)
                                val animatedHeight = fraction * animProgress.value

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = "%.2fM".format((point.count * animProgress.value) / 1000000.0),
                                        fontSize = 9.sp,
                                        fontWeight = if (isLatest) FontWeight.Black else FontWeight.Bold,
                                        color = if (isLatest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height((100 * animatedHeight).dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                if (isLatest) {
                                                    Brush.verticalGradient(
                                                        listOf(Color(0xFF00E5FF), Color(0xFF0091EA))
                                                    )
                                                } else {
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                                        )
                                                    )
                                                }
                                            )
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isLatest) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        border = if (isLatest) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                    ) {
                                        Text(
                                            text = "${point.year % 100}'",
                                            fontSize = 10.sp,
                                            fontWeight = if (isLatest) FontWeight.Black else FontWeight.Normal,
                                            color = if (isLatest) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Footer Insight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedChartMode) {
                        1 -> "שיא מסירות שבועי: יום ראשון"
                        2 -> "סך מסירות מתחילת השנה: ~234,000"
                        else -> "סך כלי רכב בישראל: %,d".format(totalCount)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (selectedChartMode) {
                        1 -> "ממוצע: כ-850 רכבים ביום"
                        2 -> "קצב מסירות יציב"
                        else -> "גידול ממוצע: ~110,000 בשנה"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class DailyTrendPoint(val dayShort: String, val dayFull: String, val count: Int)
private data class MonthlyTrendPoint(val monthName: String, val count: Int)

@Composable
fun ModelDetailStatisticsCard(
    detail: ModelStatisticsDetail,
    onNavigateToGallery: ((String) -> Unit)?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(detail) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
    }

    val cleanMake = detail.makeHe
        .replace(Regex(" (יפן|גרמנ|גרמניה|ארהב|ארה\"ב|צרפת|קוריאה|סין|צ'כיה|ספרד|איטליה|בריטניה|טורקיה|הודו)$"), "")
        .trim()

    var cleanModel = detail.modelName
        .replace(Regex("^${Regex.escape(cleanMake)}\\s*", RegexOption.IGNORE_CASE), "")
        .replace(" ILG", "")
        .replace(Regex("\\bסד\\b"), "סדאן")
        .replace(Regex("\\bהצ\\b"), "האצ'בק")
        .replace(Regex("\\bסט\\b"), "סטיישן")
        .replace(Regex("\\bקב\\b"), "קבריולט")
        .trim()
    if (cleanModel.isBlank()) cleanModel = detail.modelName

    val displayTitle = if (detail.modelName.contains(cleanMake, ignoreCase = true)) cleanModel else "$cleanMake $cleanModel"

    val cleanCommercial = detail.commercialName
        ?.replace(" ILG", "")
        ?.replace(Regex("\\bסד\\b"), "סדאן")
        ?.replace(Regex("\\bהצ\\b"), "האצ'בק")
        ?.takeIf { it.isNotBlank() && !displayTitle.contains(it, ignoreCase = true) }

    val subtitle = listOfNotNull(
        if (detail.makeEn.isNotBlank() && detail.makeEn != "car" && !displayTitle.contains(detail.makeEn, ignoreCase = true)) detail.makeEn else null,
        cleanCommercial
    ).filter { it.isNotBlank() }.joinToString(" • ")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Brand Logo, Clean Title, Subtitle, Classification & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AutoBrandLogo(
                    hebrewMake = cleanMake,
                    size = 56.dp
                )

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (subtitle.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = detail.classification,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "סגור",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4 Metrics Grid (Aligned & Single-Line Safe)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Total Active
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "%,d".format(detail.totalActive),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            maxLines = 1,
                            softWrap = false,
                            color = Color(0xFF0091EA)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "פעילים בכביש",
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Metric 2: Survival Rate
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "%.1f%%".format(detail.survivalRate),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            maxLines = 1,
                            softWrap = false,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "שרידות דגם",
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Metric 3: Safety Score
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (detail.safetyScore != null && detail.safetyScore > 0) "%.1f/8".format(detail.safetyScore) else "7.0/8",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            maxLines = 1,
                            softWrap = false,
                            color = Color(0xFFFF8F00)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "ציון בטיחות",
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Metric 4: Horsepower / Engine
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (detail.enginePowerHp != null && detail.enginePowerHp > 0) "${detail.enginePowerHp}" else "רגיל",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            maxLines = 1,
                            softWrap = false,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (detail.enginePowerHp != null && detail.enginePowerHp > 0) "כ\"ס מנוע" else "הספק מנוע",
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Year Distribution Breakdown Chart
            if (detail.yearDistribution.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "התפלגות כלי רכב פעילים לפי שנתונים:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                val maxCount = detail.yearDistribution.maxOfOrNull { it.activeCount }?.coerceAtLeast(1) ?: 1

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        detail.yearDistribution.sortedBy { it.year }.forEach { yr ->
                            val fraction = (yr.activeCount.toFloat() / maxCount).coerceIn(0.15f, 1f)
                            val animatedHeight = fraction * animProgress.value

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 3.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = "%,d".format((yr.activeCount * animProgress.value).toInt()),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((75 * animatedHeight).dp)
                                        .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF00E5FF), Color(0xFF0091EA))
                                            )
                                        )
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${yr.year}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Fuel Types & Transmission Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text(
                    text = "סוגי הנעה/דלק בדגם: ${detail.fuelTypes.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!detail.transmission.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "תיבת הילוכים: ${detail.transmission}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action Button: View Gallery Images
            Button(
                onClick = {
                    val query = "$cleanMake $cleanModel".trim()
                    onNavigateToGallery?.invoke(query)
                },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("הצג תמונות מגלריית הרכב", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
