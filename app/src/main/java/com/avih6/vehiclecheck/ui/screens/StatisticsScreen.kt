package com.avih6.vehiclecheck.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val fleetStats by viewModel.nationalFleetStats.collectAsState()
    var showFleetBreakdown by remember { mutableStateOf(false) }
    val displayTotal = totalCount ?: fleetStats.activePrivate

    val modelQuery by viewModel.modelSearchQuery.collectAsState()
    val modelSuggestions by viewModel.modelSuggestions.collectAsState()
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
            BrandStat("יונדאי", "Hyundai", (12.1f / 100f * displayTotal).toInt(), 12.1f, listOf("טוסון (Tucson)", "איוניק 5 (Ioniq 5)", "אלנטרה (Elantra)", "קונה (Kona)", "i10", "i20", "סנטה פה (Santa Fe)")),
            BrandStat("טויוטה", "Toyota", (11.5f / 100f * displayTotal).toInt(), 11.5f, listOf("קורולה (Corolla)", "יאריס (Yaris)", "ראב 4 (RAV4)", "C-HR", "קאמרי (Camry)", "פריוס (Prius)", "לנד קרוזר (Land Cruiser)")),
            BrandStat("קיה", "Kia", (10.1f / 100f * displayTotal).toInt(), 10.1f, listOf("פיקנטו (Picanto)", "ספורטאז' (Sportage)", "נירו (Niro)", "סטוניק (Stonic)", "EV6", "סורנטו (Sorento)", "סיד (Ceed)")),
            BrandStat("סקודה", "Skoda", (6.8f / 100f * displayTotal).toInt(), 6.8f, listOf("אוקטביה (Octavia)", "קודיאק (Kodiaq)", "סופרב (Superb)", "קאמיק (Kamiq)", "פאביה (Fabia)", "אניאק (Enyaq)")),
            BrandStat("מאזדה", "Mazda", (6.5f / 100f * displayTotal).toInt(), 6.5f, listOf("מאזדה 3 (Mazda 3)", "CX-5", "מאזדה 2 (Mazda 2)", "CX-30", "מאזדה 6", "CX-60")),
            BrandStat("סיאט", "Seat", (4.1f / 100f * displayTotal).toInt(), 4.1f, listOf("איביזה (Ibiza)", "ארונה (Arona)", "אטקה (Ateca)", "לאון (Leon)")),
            BrandStat("פולקסווגן", "Volkswagen", (3.7f / 100f * displayTotal).toInt(), 3.7f, listOf("גולף (Golf)", "פולו (Polo)", "טיגואן (Tiguan)", "ID.4", "פאסאט (Passat)", "טי-רוק (T-Roc)")),
            BrandStat("שברולט", "Chevrolet", (3.4f / 100f * displayTotal).toInt(), 3.4f, listOf("ספארק (Spark)", "טראוורס (Traverse)", "אקווינוקס (Equinox)", "בלייזר (Blazer)", "קרוז (Cruze)")),
            BrandStat("פיג'ו", "Peugeot", (3.2f / 100f * displayTotal).toInt(), 3.2f, listOf("208", "2008", "3008", "5008", "308", "פרטנר (Partner)")),
            BrandStat("סובארו", "Subaru", (2.9f / 100f * displayTotal).toInt(), 2.9f, listOf("פורסטר (Forester)", "קרוסטרק / XV", "אאוטבק (Outback)", "אימפרזה (Impreza)", "B4")),
            BrandStat("BYD", "BYD", (2.5f / 100f * displayTotal).toInt(), 2.5f, listOf("אטו 3 (Atto 3)", "דולפין (Dolphin)", "סיל (Seal)", "סיל U", "טאנג (Tang)")),
            BrandStat("טסלה", "Tesla", (1.7f / 100f * displayTotal).toInt(), 1.7f, listOf("מודל 3 (Model 3)", "מודל Y (Model Y)", "מודל S", "מודל X", "סייברטראק (Cybertruck)"))
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
                        if (!lastUpdated.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "עודכן: $lastUpdated",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Key National Metrics Grid (Active Fleet, Grand Total with Vintage & Tzama, Share)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Top Row: Total Active in Israel & Grand Total in Registry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric 1: Total Active in Israel (Private + Commercial + Heavy + Motorcycles)
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
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
                                text = "%,d".format(fleetStats.totalActive),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "סה״כ פעילים בישראל",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "פרטי, משא ודו-גלגלי",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Metric 2: Grand Total Vehicles Ever (Including Vintage & Tzama)
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "%,d".format(fleetStats.grandTotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0288D1),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "סה״כ כלי רכב בכללי",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "כולל ישנים וצמ״א",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Sub Row: Private & Commercial Active + Electrified Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric 3: Private & Commercial
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "%,d".format(fleetStats.activePrivate),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "פרטי ומסחרי פעיל",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${"%.1f".format(fleetStats.activePrivatePercent)}% מהפעילים",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Metric 4: Electrified Share (Consistent layout: Number on top, Title, Percentage on bottom)
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "%,d".format((0.252f * fleetStats.activePrivate).toInt()),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "מחושמלים (EV/היבריד)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "25.2% מהפעילים",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Interactive Expandable Fleet Breakdown Card (Includes Tzama, Vintage, Heavy, etc.)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFleetBreakdown = !showFleetBreakdown },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "פילוח מצבת הרכבים והמאגרים",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "התפלגות פעילים, ירדו מהכביש, צמ״א והיסטוריים",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showFleetBreakdown = !showFleetBreakdown },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (showFleetBreakdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (showFleetBreakdown) "כווץ" else "הרחב",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        AnimatedVisibility(visible = showFleetBreakdown) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Section 1: Active breakdown
                                Text(
                                    text = "כלי רכב פעילים על הכביש (סה״כ %,d)".format(fleetStats.totalActive),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                FleetBreakdownRow(
                                    title = "רכב פרטי ומסחרי פעיל",
                                    count = fleetStats.activePrivate,
                                    percent = fleetStats.activePrivatePercent,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                FleetBreakdownRow(
                                    title = "משאיות, אוטובוסים ורכב כבד (>3.5 טון)",
                                    count = fleetStats.activeHeavy,
                                    percent = fleetStats.activeHeavyPercent,
                                    color = Color(0xFF1976D2)
                                )
                                FleetBreakdownRow(
                                    title = "אופנועים וקטנועים (דו-גלגלי)",
                                    count = fleetStats.activeMotorcycles,
                                    percent = fleetStats.activeMotorcyclesPercent,
                                    color = Color(0xFF0097A7)
                                )

                                Spacer(Modifier.height(4.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Section 2: Inactive & Vintage
                                Text(
                                    text = "כלי רכב שירדו מהכביש וביטולים בעבר (סה״כ %,d)".format(fleetStats.totalInactive),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                )
                                FleetSimpleCountRow(
                                    title = "הורדו מהכביש ב-2017 עד היום",
                                    count = fleetStats.inactive2017
                                )
                                FleetSimpleCountRow(
                                    title = "הורדו מהכביש בשנים 2010–2016",
                                    count = fleetStats.inactive2010_2016
                                )
                                FleetSimpleCountRow(
                                    title = "הורדו מהכביש בשנים 2000–2009",
                                    count = fleetStats.inactive2000_2009
                                )
                                FleetSimpleCountRow(
                                    title = "רכבים ישנים והיסטוריים",
                                    subtitle = "לפני שנת 2000 (סוסיתא, כרמל ורכבי אספנות)",
                                    count = fleetStats.inactiveVintagePre2000,
                                    badge = "היסטורי"
                                )

                                Spacer(Modifier.height(4.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Section 3: Tzama (Heavy Engineering Equipment)
                                Text(
                                    text = "ציוד מכני הנדסי (צמ״א / צמ״ה)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF57C00)
                                )
                                FleetSimpleCountRow(
                                    title = "ציוד הנדסי כבד ברישיון",
                                    subtitle = "שופלים, מחפרים, דחפורים, מנופים וטרקטורים",
                                    count = fleetStats.engineeringEquipment,
                                    badge = "צמ״א",
                                    badgeColor = Color(0xFFF57C00)
                                )

                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "סה״כ כלל כלי הרכב שנרשמו אי פעם בישראל:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "%,d".format(fleetStats.grandTotal),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Animated National Fleet Trend Graph (Smooth Line Chart)
        item {
            NationalFleetTrendGraph(
                totalCount = displayTotal,
                lastUpdated = lastUpdated
            )
        }

        // 4. Interactive Model Search Section
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

                    // Live Autocomplete / Model Suggestions Dropdown
                    AnimatedVisibility(
                        visible = modelQuery.isNotBlank() && modelSuggestions.isNotEmpty() && !isSearchingModel && selectedModelDetail == null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp,
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "הצעות לדגמים תואמים:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${modelSuggestions.size} תוצאות",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )

                                modelSuggestions.take(6).forEachIndexed { index, suggestion ->
                                    Surface(
                                        onClick = {
                                            focusManager.clearFocus()
                                            viewModel.selectModelSuggestion(suggestion)
                                        },
                                        color = Color.Transparent,
                                        modifier = Modifier.fillMaxWidth().handCursor()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AutoBrandLogo(
                                                hebrewMake = suggestion.brandHebrew,
                                                size = 30.dp
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                val titleText = if (suggestion.modelHebrew.contains(suggestion.brandHebrew, ignoreCase = true)) {
                                                    suggestion.modelHebrew
                                                } else {
                                                    "${suggestion.brandHebrew} ${suggestion.modelHebrew}"
                                                }
                                                val subtitleText = if (suggestion.modelEnglish.contains(suggestion.brandEnglish, ignoreCase = true)) {
                                                    suggestion.modelEnglish
                                                } else {
                                                    "${suggestion.brandEnglish} ${suggestion.modelEnglish}"
                                                }
                                                Text(
                                                    text = titleText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = subtitleText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "בחר",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(12.dp).padding(start = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (index < modelSuggestions.take(6).size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                        }
                    }

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
                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Model Statistics Result Card or Loading/Error State
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
                                        text = "דגמים נפוצים בישראל (לחץ לניתוח):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    androidx.compose.foundation.lazy.LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(brand.topModels.size) { mIdx ->
                                            val model = brand.topModels[mIdx]
                                            val cleanModelName = model.substringBefore(" (").trim()
                                            val fullModelQuery = "${brand.nameHe} $cleanModelName"
                                            AssistChip(
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    viewModel.searchModelStatistics(fullModelQuery)
                                                },
                                                label = {
                                                    Text(
                                                        text = model,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.DirectionsCar,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                    }
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

data class ChartPointData(
    val labelBottom: String,
    val valueLabel: String,
    val numericValue: Float
)

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

    val dailyAverage = remember(dailyData) {
        if (dailyData.isNotEmpty()) kotlin.math.round(dailyData.map { it.count }.average()).toInt() else 845
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

    val monthlyTotalDeliveries = remember(monthlyData) {
        monthlyData.sumOf { it.count }
    }

    val monthlyAverage = remember(monthlyData) {
        if (monthlyData.isNotEmpty()) kotlin.math.round(monthlyData.map { it.count }.average()).toInt() else 29263
    }

    val monthlyTrendStatus = remember(monthlyData) {
        if (monthlyData.size >= 2) {
            val lastMonth = monthlyData.last().count
            val prevMonth = monthlyData[monthlyData.lastIndex - 1].count
            val diffPercent = ((lastMonth - prevMonth).toDouble() / prevMonth) * 100.0
            when {
                diffPercent > 3.0 -> "מגמת עלייה במסירות (+%.1f%%)".format(diffPercent)
                diffPercent < -3.0 -> "מגמת ירידה במסירות (%.1f%%)".format(diffPercent)
                else -> "קצב מסירות יציב (%.1f%%)".format(diffPercent)
            }
        } else {
            "קצב מסירות יציב"
        }
    }

    val annualGrowthPercent = remember(fleetHistory) {
        if (fleetHistory.size >= 2) {
            val last = fleetHistory.last().count
            val prev = fleetHistory[fleetHistory.size - 2].count
            if (prev > 0) ((last - prev).toDouble() / prev * 100.0) else 2.8
        } else 2.8
    }

    val annualAverageGrowth = remember(fleetHistory) {
        if (fleetHistory.size >= 2) {
            val totalGrowth = fleetHistory.last().count - fleetHistory.first().count
            val yearsSpan = fleetHistory.last().year - fleetHistory.first().year
            if (yearsSpan > 0) totalGrowth / yearsSpan else 105000
        } else 105000
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
                            1 -> "$dailyAverage ממוצע ליום"
                            2 -> "כ-%,d בחודש (ממוצע)".format(monthlyAverage)
                            else -> "+%.1f%% שנתי".format(annualGrowthPercent)
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
                val currentPoints = remember(selectedChartMode, totalCount) {
                    when (selectedChartMode) {
                        1 -> dailyData.map { ChartPointData(it.dayShort, "${it.count}", it.count.toFloat()) }
                        2 -> monthlyData.map { ChartPointData(it.monthName, "%.1fK".format(it.count / 1000f), it.count.toFloat()) }
                        else -> fleetHistory.map { ChartPointData("${it.year % 100}'", "%.2fM".format(it.count / 1000000f), it.count.toFloat()) }
                    }
                }

                val maxVal = remember(currentPoints) { currentPoints.maxOfOrNull { it.numericValue }?.coerceAtLeast(1f) ?: 1f }
                val minVal = remember(currentPoints) { currentPoints.minOfOrNull { it.numericValue }?.coerceAtLeast(0f) ?: 0f }

                val primaryColor = MaterialTheme.colorScheme.primary
                val cyanColor = Color(0xFF00E5FF)
                val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val topPadding = 20.dp.toPx()
                            val bottomPadding = 12.dp.toPx()
                            val usableHeight = height - topPadding - bottomPadding
                            val stepX = if (currentPoints.size > 1) width / (currentPoints.size - 1) else width

                            val points = currentPoints.mapIndexed { index, item ->
                                val x = if (currentPoints.size > 1) index * stepX else width / 2
                                val normalized = if (maxVal > minVal) {
                                    ((item.numericValue - minVal * 0.85f) / (maxVal - minVal * 0.85f)).coerceIn(0.12f, 1f)
                                } else 0.5f
                                val y = height - bottomPadding - (normalized * usableHeight * animProgress.value)
                                Offset(x, y)
                            }

                            // Draw subtle dashed grid lines
                            for (i in 0..2) {
                                val gridY = topPadding + (usableHeight * i / 2)
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, gridY),
                                    end = Offset(width, gridY),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                )
                            }

                            if (points.isNotEmpty()) {
                                val strokePath = Path()
                                val fillPath = Path()

                                strokePath.moveTo(points.first().x, points.first().y)
                                fillPath.moveTo(points.first().x, height - bottomPadding)
                                fillPath.lineTo(points.first().x, points.first().y)

                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val pPrev = if (i > 0) points[i - 1] else p0
                                    val pNext = if (i < points.size - 2) points[i + 2] else p1
                                    val controlPoint1 = Offset(
                                        x = p0.x + (p1.x - pPrev.x) / 5f,
                                        y = (p0.y + (p1.y - pPrev.y) / 5f).coerceIn(topPadding, height - bottomPadding)
                                    )
                                    val controlPoint2 = Offset(
                                        x = p1.x - (pNext.x - p0.x) / 5f,
                                        y = (p1.y - (pNext.y - p0.y) / 5f).coerceIn(topPadding, height - bottomPadding)
                                    )
                                    strokePath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                                    fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                                }

                                fillPath.lineTo(points.last().x, height - bottomPadding)
                                fillPath.close()

                                // Glowing gradient fill under curve
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            cyanColor.copy(alpha = 0.35f * animProgress.value),
                                            primaryColor.copy(alpha = 0.08f * animProgress.value),
                                            Color.Transparent
                                        ),
                                        startY = topPadding,
                                        endY = height
                                    )
                                )

                                // Smooth curved line stroke
                                drawPath(
                                    path = strokePath,
                                    brush = Brush.horizontalGradient(listOf(cyanColor, primaryColor)),
                                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )

                                // Circular point indicators
                                points.forEachIndexed { idx, pt ->
                                    val isLast = idx == points.lastIndex
                                    drawCircle(
                                        color = if (isLast) cyanColor else primaryColor,
                                        radius = if (isLast) 5.5.dp.toPx() else 4.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = if (isLast) 2.5.dp.toPx() else 1.8.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }

                        // Top value labels
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            currentPoints.forEachIndexed { idx, item ->
                                val isLast = idx == currentPoints.lastIndex
                                Text(
                                    text = item.valueLabel,
                                    fontSize = 8.5.sp,
                                    fontWeight = if (isLast) FontWeight.Black else FontWeight.Bold,
                                    color = if (isLast) cyanColor else primaryColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Bottom labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        currentPoints.forEachIndexed { idx, item ->
                            val isLast = idx == currentPoints.lastIndex
                            Text(
                                text = item.labelBottom,
                                fontSize = 10.sp,
                                fontWeight = if (isLast) FontWeight.Black else FontWeight.Normal,
                                color = if (isLast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
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
                        2 -> "סך מסירות מתחילת השנה: %,d".format(monthlyTotalDeliveries)
                        else -> "סך כלי רכב בישראל: %,d".format(totalCount)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (selectedChartMode) {
                        1 -> "ממוצע: כ-$dailyAverage רכבים ביום"
                        2 -> monthlyTrendStatus
                        else -> "גידול ממוצע: כ-%,d בשנה".format(annualAverageGrowth)
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
                            text = if (detail.safetyScore != null && detail.safetyScore > 0) {
                                if (detail.safetyScore % 1.0 == 0.0) "${detail.safetyScore.toInt()}/8" else "%.1f/8".format(detail.safetyScore)
                            } else "—",
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

            // Year Distribution Breakdown Chart (Smooth Line Chart)
            if (detail.yearDistribution.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "התפלגות כלי רכב פעילים לפי שנתונים:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))

                val sorted = remember(detail.yearDistribution) { detail.yearDistribution.sortedBy { it.year } }
                val maxCount = remember(sorted) { sorted.maxOfOrNull { it.activeCount }?.coerceAtLeast(1) ?: 1 }
                val minCount = remember(sorted) { sorted.minOfOrNull { it.activeCount }?.coerceAtLeast(0) ?: 0 }

                val primaryColor = MaterialTheme.colorScheme.primary
                val cyanColor = Color(0xFF00E5FF)
                val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                val topPadding = 20.dp.toPx()
                                val bottomPadding = 12.dp.toPx()
                                val usableHeight = height - topPadding - bottomPadding
                                val stepX = if (sorted.size > 1) width / (sorted.size - 1) else width

                                val points = sorted.mapIndexed { index, item ->
                                    val x = if (sorted.size > 1) index * stepX else width / 2
                                    val normalized = if (maxCount > minCount) {
                                        ((item.activeCount - minCount).toFloat() / (maxCount - minCount)).coerceIn(0.1f, 1f)
                                    } else 0.5f
                                    val y = height - bottomPadding - (normalized * usableHeight * animProgress.value)
                                    Offset(x, y)
                                }

                                // Draw subtle dashed grid lines
                                for (i in 0..2) {
                                    val gridY = topPadding + (usableHeight * i / 2)
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(0f, gridY),
                                        end = Offset(width, gridY),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                    )
                                }

                                if (points.isNotEmpty()) {
                                    val strokePath = Path()
                                    val fillPath = Path()

                                    strokePath.moveTo(points.first().x, points.first().y)
                                    fillPath.moveTo(points.first().x, height - bottomPadding)
                                    fillPath.lineTo(points.first().x, points.first().y)

                                    for (i in 0 until points.size - 1) {
                                        val p0 = points[i]
                                        val p1 = points[i + 1]
                                        val pPrev = if (i > 0) points[i - 1] else p0
                                        val pNext = if (i < points.size - 2) points[i + 2] else p1
                                        val controlPoint1 = Offset(
                                            x = p0.x + (p1.x - pPrev.x) / 5f,
                                            y = (p0.y + (p1.y - pPrev.y) / 5f).coerceIn(topPadding, height - bottomPadding)
                                        )
                                        val controlPoint2 = Offset(
                                            x = p1.x - (pNext.x - p0.x) / 5f,
                                            y = (p1.y - (pNext.y - p0.y) / 5f).coerceIn(topPadding, height - bottomPadding)
                                        )
                                        strokePath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                                        fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                                    }

                                    fillPath.lineTo(points.last().x, height - bottomPadding)
                                    fillPath.close()

                                    // Gradient fill
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                cyanColor.copy(alpha = 0.35f * animProgress.value),
                                                primaryColor.copy(alpha = 0.08f * animProgress.value),
                                                Color.Transparent
                                            ),
                                            startY = topPadding,
                                            endY = height
                                        )
                                    )

                                    // Smooth line stroke
                                    drawPath(
                                        path = strokePath,
                                        brush = Brush.horizontalGradient(listOf(cyanColor, primaryColor)),
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )

                                    // Data point circles
                                    points.forEachIndexed { idx, pt ->
                                        val isLast = idx == points.lastIndex
                                        drawCircle(
                                            color = if (isLast) cyanColor else primaryColor,
                                            radius = if (isLast) 5.dp.toPx() else 4.dp.toPx(),
                                            center = pt
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = if (isLast) 2.5.dp.toPx() else 1.8.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                }
                            }

                            // Value labels above chart
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                sorted.forEach { yr ->
                                    val countText = if (yr.activeCount >= 1000) "%.1fK".format((yr.activeCount * animProgress.value) / 1000f)
                                    else "%,d".format((yr.activeCount * animProgress.value).toInt())
                                    Text(
                                        text = countText,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // Year labels underneath
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            sorted.forEach { yr ->
                                Text(
                                    text = "${yr.year}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
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

@Composable
private fun FleetBreakdownRow(
    title: String,
    count: Int,
    percent: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%,d".format(count),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(6.dp))
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${"%.1f".format(percent)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun FleetSimpleCountRow(
    title: String,
    count: Int,
    subtitle: String? = null,
    badge: String? = null,
    badgeColor: Color = Color(0xFF0288D1)
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (badge != null) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = "%,d".format(count),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
