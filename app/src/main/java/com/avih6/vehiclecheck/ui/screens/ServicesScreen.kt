package com.avih6.vehiclecheck.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avih6.vehiclecheck.MainViewModel
import com.avih6.vehiclecheck.data.CarAppraiserRecord
import com.avih6.vehiclecheck.data.CarDealerRecord
import com.avih6.vehiclecheck.data.EvChargingStationRecord
import com.avih6.vehiclecheck.data.GarageRecord
import com.avih6.vehiclecheck.data.PartsTradeRecord
import com.avih6.vehiclecheck.data.ServicesCategory
import com.avih6.vehiclecheck.data.ServicesSpecialties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val category by viewModel.servicesCategory.collectAsState()
    val query by viewModel.servicesQuery.collectAsState()
    val garageSpecialty by viewModel.garageSpecialtyFilter.collectAsState()
    val testStationFilter by viewModel.testStationFilter.collectAsState()
    val evFilter by viewModel.evFilter.collectAsState()
    val garages by viewModel.garagesList.collectAsState()
    val chargingStations by viewModel.chargingStationsList.collectAsState()
    val carDealers by viewModel.carDealersList.collectAsState()
    val appraisers by viewModel.appraisersList.collectAsState()
    val partsTrade by viewModel.partsTradeList.collectAsState()
    val lastUpdated by viewModel.servicesLastUpdated.collectAsState()
    val totalCount by viewModel.servicesTotalCount.collectAsState()
    val isLoading by viewModel.isLoadingServices.collectAsState()

    LaunchedEffect(Unit) {
        if (garages.isEmpty() && chargingStations.isEmpty() && carDealers.isEmpty() && appraisers.isEmpty() && partsTrade.isEmpty()) {
            viewModel.fetchServices()
        }
    }

    val filteredGarages = remember(garages, garageSpecialty, testStationFilter, category) {
        if (category == ServicesCategory.GARAGES) {
            val opt = ServicesSpecialties.garageOptions.firstOrNull { it.title == garageSpecialty }
            if (opt == null || opt.dbValues.isEmpty()) garages
            else garages.filter { g -> opt.dbValues.any { valName -> g.specialty?.contains(valName, ignoreCase = true) == true } }
        } else if (category == ServicesCategory.TEST_STATIONS) {
            val opt = ServicesSpecialties.testStationOptions.firstOrNull { it.title == testStationFilter }
            if (opt == null || opt.dbValue == null) garages
            else garages.filter { g -> g.specialty?.contains(opt.dbValue, ignoreCase = true) == true }
        } else {
            garages
        }
    }

    val filteredStations = remember(chargingStations, evFilter) {
        when (evFilter) {
            "טעינה מהירה (DC)" -> chargingStations.filter { it.hasFastCharging }
            "טעינה רגילה (AC)" -> chargingStations.filter { (it.slowSockets ?: 0) > 0 }
            else -> chargingStations
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        // Main Category Tabs - Scrollable LazyRow for all 6 categories
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ServicesCategory.values()) { cat ->
                val isSelected = category == cat
                val catIcon = when (cat) {
                    ServicesCategory.TEST_STATIONS -> Icons.Default.AssignmentTurnedIn
                    ServicesCategory.GARAGES -> Icons.Default.Build
                    ServicesCategory.EV_CHARGING -> Icons.Default.EvStation
                    ServicesCategory.CAR_DEALERS -> Icons.Default.Storefront
                    ServicesCategory.APPRAISERS -> Icons.Default.VerifiedUser
                    ServicesCategory.PARTS_TRADE -> Icons.Default.CarRepair
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setServicesCategory(cat) },
                    label = { Text(cat.titleHe, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(catIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Last updated government freshness banner
        if (!lastUpdated.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Update,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "מאגר משרד התחבורה • עודכן: $lastUpdated",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Search Input
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setServicesQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    when (category) {
                        ServicesCategory.TEST_STATIONS -> "חיפוש מכון רישוי לפי עיר או שם"
                        ServicesCategory.GARAGES -> "חיפוש מוסך לפי עיר, שם או תחום"
                        ServicesCategory.EV_CHARGING -> "חיפוש עמדת טעינה לפי עיר או מפעיל"
                        ServicesCategory.CAR_DEALERS -> "חיפוש סוחר רכב לפי עיר, שם או ח.פ"
                        ServicesCategory.APPRAISERS -> "חיפוש שמאי רכב לפי עיר או שם"
                        ServicesCategory.PARTS_TRADE -> "חיפוש ספק חלפים לפי עיר, שם או תחום"
                    }
                )
            },
            placeholder = {
                Text(
                    when (category) {
                        ServicesCategory.TEST_STATIONS -> "למשל: תל אביב, קומפיוטסט, דינמומטר..."
                        ServicesCategory.GARAGES -> "למשל: חיפה, טויוטה, מכונאות, מוסך שלום..."
                        ServicesCategory.EV_CHARGING -> "למשל: ירושלים, אפקון, סונול EVI..."
                        ServicesCategory.CAR_DEALERS -> "למשל: תל אביב, אלבר, מגרש, 514..."
                        ServicesCategory.APPRAISERS -> "למשל: חיפה, לוי, משה..."
                        ServicesCategory.PARTS_TRADE -> "למשל: צמיגים, ירושלים, חלקי חילוף..."
                    }
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setServicesQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "נקה חיפוש")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController?.hide()
                viewModel.fetchServices()
            }),
            shape = RoundedCornerShape(16.dp)
        )

        // Sub-filter Row
        when (category) {
            ServicesCategory.TEST_STATIONS -> {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(com.avih6.vehiclecheck.data.ServicesSpecialties.testStationOptions) { opt ->
                        val isSel = testStationFilter == opt.title
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.setTestStationFilter(opt.title) },
                            label = { Text(opt.title, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
            ServicesCategory.GARAGES -> {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(com.avih6.vehiclecheck.data.ServicesSpecialties.garageOptions) { opt ->
                        val isSel = garageSpecialty == opt.title
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.setGarageSpecialtyFilter(opt.title) },
                            label = { Text(opt.title, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
            ServicesCategory.EV_CHARGING -> {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.avih6.vehiclecheck.data.ServicesSpecialties.evFilterOptions.forEach { opt ->
                        val isSel = evFilter == opt
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.setEvFilter(opt) },
                            label = { Text(opt, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
            else -> {}
        }

        Spacer(Modifier.height(10.dp))

        // Content Area with Loading / Empty / List
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("טוען נתונים רשמיים ממאגר משרד התחבורה...", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                when (category) {
                    ServicesCategory.TEST_STATIONS, ServicesCategory.GARAGES -> {
                        if (filteredGarages.isEmpty()) {
                            EmptyServicesView(
                                message = if (query.isNotBlank()) "לא נמצאו תוצאות עבור \"$query\"" else "לא נמצאו תוצאות במאגר",
                                onReset = { viewModel.setServicesQuery("") }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 90.dp)
                            ) {
                                item {
                                    val headerText = if (totalCount != null && totalCount!! > filteredGarages.size) {
                                        "מציג ${filteredGarages.size} מתוך %,d תוצאות במאגר משרד התחבורה".format(totalCount)
                                    } else {
                                        "נמצאו ${filteredGarages.size} תוצאות במאגר משרד התחבורה"
                                    }
                                    Text(
                                        text = headerText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                items(filteredGarages, key = { it.id ?: it.garageNumber ?: it.hashCode() }) { garage ->
                                    GarageCard(garage = garage)
                                }
                            }
                        }
                    }
                    ServicesCategory.EV_CHARGING -> {
                        if (filteredStations.isEmpty()) {
                            EmptyServicesView(
                                message = if (query.isNotBlank()) "לא נמצאו עמדות טעינה עבור \"$query\"" else "לא נמצאו עמדות במאגר",
                                onReset = { viewModel.setServicesQuery("") }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 90.dp)
                            ) {
                                item {
                                    val headerText = if (totalCount != null && totalCount!! > filteredStations.size) {
                                        "מציג ${filteredStations.size} מתוך %,d עמדות טעינה במאגר משרד התחבורה".format(totalCount)
                                    } else {
                                        "נמצאו ${filteredStations.size} עמדות טעינה ציבוריות (מאגר ארצי מלא)"
                                    }
                                    Text(
                                        text = headerText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                items(filteredStations, key = { it.id ?: it.objectId ?: it.hashCode() }) { station ->
                                    EvStationCard(station = station)
                                }
                            }
                        }
                    }
                    ServicesCategory.CAR_DEALERS -> {
                        if (carDealers.isEmpty()) {
                            EmptyServicesView(
                                message = if (query.isNotBlank()) "לא נמצאו סוחרי רכב עבור \"$query\"" else "לא נמצאו סוחרי רכב במאגר",
                                onReset = { viewModel.setServicesQuery("") }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 90.dp)
                            ) {
                                item {
                                    val headerText = if (totalCount != null && totalCount!! > carDealers.size) {
                                        "מציג ${carDealers.size} מתוך %,d סוחרי רכב מורשים במאגר משרד התחבורה".format(totalCount)
                                    } else {
                                        "נמצאו ${carDealers.size} סוחרי רכב מורשים ברישיון משרד התחבורה"
                                    }
                                    Text(
                                        text = headerText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                items(carDealers, key = { it.id ?: it.hashCode() }) { dealer ->
                                    CarDealerCard(dealer = dealer)
                                }
                            }
                        }
                    }
                    ServicesCategory.APPRAISERS -> {
                        if (appraisers.isEmpty()) {
                            EmptyServicesView(
                                message = if (query.isNotBlank()) "לא נמצאו שמאי רכב עבור \"$query\"" else "לא נמצאו שמאי רכב במאגר",
                                onReset = { viewModel.setServicesQuery("") }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 90.dp)
                            ) {
                                item {
                                    val headerText = if (totalCount != null && totalCount!! > appraisers.size) {
                                        "מציג ${appraisers.size} מתוך %,d שמאי רכב במאגר משרד התחבורה".format(totalCount)
                                    } else {
                                        "נמצאו ${appraisers.size} שמאי רכב מוסמכים (מאגר ארצי מלא)"
                                    }
                                    Text(
                                        text = headerText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                items(appraisers, key = { it.id ?: it.licenseNumber ?: it.hashCode() }) { appraiser ->
                                    CarAppraiserCard(appraiser = appraiser)
                                }
                            }
                        }
                    }
                    ServicesCategory.PARTS_TRADE -> {
                        if (partsTrade.isEmpty()) {
                            EmptyServicesView(
                                message = if (query.isNotBlank()) "לא נמצאו עסקים עבור \"$query\"" else "לא נמצאו עסקים במאגר",
                                onReset = { viewModel.setServicesQuery("") }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 90.dp)
                            ) {
                                item {
                                    val headerText = if (totalCount != null && totalCount!! > partsTrade.size) {
                                        "מציג ${partsTrade.size} מתוך %,d עסקי תעבורה במאגר משרד התחבורה".format(totalCount)
                                    } else {
                                        "נמצאו ${partsTrade.size} עסקי סחר ויבוא מוצרי תעבורה מורשים"
                                    }
                                    Text(
                                        text = headerText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                items(partsTrade, key = { it.id ?: it.hashCode() }) { record ->
                                    PartsTradeCard(record = record)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GarageCard(garage: GarageRecord) {
    val context = LocalContext.current
    val isTest = garage.isTestStation
    val badgeColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = garage.garageName ?: "ללא שם",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = badgeColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = if (isTest) "מכון רישוי (טסט)" else "מוסך מורשה",
                        color = badgeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Address & City
            val locationText = listOfNotNull(garage.address, garage.city).filter { it.isNotBlank() }.joinToString(", ")
            if (locationText.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(locationText, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(4.dp))
            }

            // Specialty
            if (!garage.specialty.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Handyman, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("התמחות: ${garage.specialty}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
            }

            if (!garage.managerName.isNullOrBlank()) {
                Text("מנהל מקצועי: ${garage.managerName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            }

            Spacer(Modifier.height(10.dp))

            // Action Buttons: Call & Navigate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!garage.phone.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${garage.phone}"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(garage.phone, style = MaterialTheme.typography.labelMedium)
                    }
                }

                Button(
                    onClick = {
                        val destination = Uri.encode("${garage.garageName ?: ""}, ${garage.address ?: ""}, ${garage.city ?: ""}")
                        val geoUri = Uri.parse("geo:0,0?q=$destination")
                        val navIntent = Intent(Intent.ACTION_VIEW, geoUri)
                        try {
                            context.startActivity(navIntent)
                        } catch (_: Exception) {
                            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$destination")
                            try { context.startActivity(Intent(Intent.ACTION_VIEW, webUri)) } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ניווט", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun EvStationCard(station: EvChargingStationRecord) {
    val context = LocalContext.current
    val opColor = Color(0xFF00ACC1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.stationName ?: "עמדת טעינה ציבורית",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                if (!station.operator.isNullOrBlank()) {
                    Surface(
                        color = opColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, opColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = station.operator,
                            color = opColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (!station.address.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(station.address, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
            }

            // Sockets summary badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = "סה\"כ שקעים: ${station.totalSockets ?: 0}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if ((station.fastSockets ?: 0) > 0) {
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "מהיר (DC): ${station.fastSockets}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                if ((station.slowSockets ?: 0) > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = "רגיל (AC): ${station.slowSockets}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Navigation Button
            Button(
                onClick = {
                    val destination = Uri.encode("${station.stationName ?: ""}, ${station.address ?: ""}")
                    val geoUri = Uri.parse("geo:0,0?q=$destination")
                    val navIntent = Intent(Intent.ACTION_VIEW, geoUri)
                    try {
                        context.startActivity(navIntent)
                    } catch (_: Exception) {
                        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$destination")
                        try { context.startActivity(Intent(Intent.ACTION_VIEW, webUri)) } catch (_: Exception) {}
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("ניווט", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun EmptyServicesView(
    message: String,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(54.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onReset,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("איפוס חיפוש", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CarDealerCard(dealer: CarDealerRecord) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dealer.name ?: "סוחר רכב מורשה",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "סוחר מורשה",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            val addressStr = listOfNotNull(dealer.address, dealer.city).filter { it.isNotBlank() }.joinToString(", ")
            if (addressStr.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = addressStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!dealer.companyId.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "ח.פ / עוסק: ${dealer.companyId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (addressStr.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        val destination = Uri.encode("${dealer.name ?: ""}, $addressStr")
                        val geoUri = Uri.parse("geo:0,0?q=$destination")
                        val navIntent = Intent(Intent.ACTION_VIEW, geoUri)
                        try {
                            context.startActivity(navIntent)
                        } catch (_: Exception) {
                            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$destination")
                            try { context.startActivity(Intent(Intent.ACTION_VIEW, webUri)) } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ניווט", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun CarAppraiserCard(appraiser: CarAppraiserRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appraiser.fullName.ifBlank { "שמאי רכב מוסמך" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = Color(0xFF1565C0).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF1565C0).copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "רישיון מס' ${appraiser.licenseNumber ?: "-"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (!appraiser.city.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "אזור / יישוב: ${appraiser.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PartsTradeCard(record: PartsTradeRecord) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.businessName ?: "עסק מוצרי תעבורה",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                if (!record.businessType.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = record.businessType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (!record.occupation.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "תחום: ${record.occupation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val addressStr = listOfNotNull(record.address, record.city).filter { it.isNotBlank() }.joinToString(", ")
            if (addressStr.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = addressStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!record.phone.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${record.phone}"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(record.phone, style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (addressStr.isNotBlank()) {
                    Button(
                        onClick = {
                            val destination = Uri.encode("${record.businessName ?: ""}, $addressStr")
                            val geoUri = Uri.parse("geo:0,0?q=$destination")
                            val navIntent = Intent(Intent.ACTION_VIEW, geoUri)
                            try {
                                context.startActivity(navIntent)
                            } catch (_: Exception) {
                                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$destination")
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, webUri)) } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ניווט", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
