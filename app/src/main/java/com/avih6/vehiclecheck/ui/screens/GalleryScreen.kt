package com.avih6.vehiclecheck.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avih6.vehiclecheck.data.CarGalleryImage
import com.avih6.vehiclecheck.data.NetworkClient
import com.avih6.vehiclecheck.data.WikimediaGalleryService
import com.avih6.vehiclecheck.ui.components.HoverTooltipIconButton
import com.avih6.vehiclecheck.ui.components.handCursor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GalleryScreen(
    initialQuery: String = "הכל",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    var searchQuery by remember { mutableStateOf(if (initialQuery == "הכל") "" else initialQuery) }
    var selectedBrand by remember { mutableStateOf(if (initialQuery.isBlank()) "הכל" else initialQuery) }
    var selectedModel by remember { mutableStateOf("כל הדגמים") }
    var images by remember { mutableStateOf<List<CarGalleryImage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var nextOffset by remember { mutableStateOf<Int?>(null) }
    var selectedImageForViewer by remember { mutableStateOf<CarGalleryImage?>(null) }
    var detectedPlateVehicleInfo by remember { mutableStateOf<String?>(null) }

    val fallbackManufacturers = remember {
        listOf(
            "הכל", "טויוטה", "יונדאי", "קיה", "מאזדה", "סקודה", "טסלה", "סובארו",
            "שברולט", "מרצדס", "ב.מ.וו", "אאודי", "פולקסווגן", "BYD", "ג'ילי", "MG", "קופרה",
            "פורד", "פיג'ו", "רנו", "סיטרואן", "וולוו", "סוזוקי", "הונדה",
            "מיצובישי", "ניסאן", "סיאט", "דאצ'יה", "לקסוס", "פורשה", "ג'יפ", "קאדילאק"
        )
    }

    var dynamicManufacturers by remember { mutableStateOf<List<String>>(fallbackManufacturers) }
    var makeToModelsMap by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }

    // Load full list of certified car makes and models dynamically from Government Database
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    NetworkClient.apiService.getAllRecalls(limit = 4000, sort = "_id desc")
                }
                val records = resp.result?.records ?: emptyList()
                
                val modelMap = mutableMapOf<String, MutableSet<String>>()
                val liveMakes = records.mapNotNull { it.makeName?.trim() }
                    .filter { it.isNotBlank() && it.length > 1 }
                    .distinct()
                    .sorted()

                records.forEach { rec ->
                    val make = rec.makeName?.trim().orEmpty()
                    val mod = rec.model?.trim().orEmpty()
                    if (make.isNotBlank() && mod.isNotBlank()) {
                        val items = mod.split(",", ";", "/").map { it.trim() }.filter { it.length >= 2 }
                        modelMap.getOrPut(make) { mutableSetOf() }.addAll(items)
                    }
                }

                if (liveMakes.isNotEmpty()) {
                    dynamicManufacturers = listOf("הכל") + liveMakes
                }
                makeToModelsMap = modelMap.mapValues { listOf("כל הדגמים") + it.value.sorted() }
            } catch (_: Exception) {
                // Fallback
            }
        }
    }

    fun getModelsForCurrentBrand(brand: String): List<String> {
        val fromGov = makeToModelsMap[brand]
        if (!fromGov.isNullOrEmpty()) return fromGov

        // Brand-specific fallback models
        val b = brand.lowercase()
        val defaultList = when {
            b.contains("שברולט") || b.contains("chevrolet") -> listOf("CORVETTE", "CAMARO", "SPARK", "MALIBU", "TRAVERSE", "BLAZER", "EQUINOX", "CRUZE", "SILVERADO", "TAHOE", "SUBURBAN", "TRAX", "BOLT")
            b.contains("טויוטה") || b.contains("toyota") -> listOf("COROLLA", "YARIS", "RAV4", "LAND CRUISER", "CAMRY", "HILUX", "C-HR", "PRIUS", "AYGO", "HIGHLANDER", "BZ4X")
            b.contains("יונדאי") || b.contains("hyundai") -> listOf("TUCSON", "IONIQ 5", "IONIQ 6", "KONA", "ELANTRA", "I10", "I20", "I30", "SANTA FE", "BAYON", "STARIA", "VENUE")
            b.contains("קיה") || b.contains("kia") -> listOf("SPORTAGE", "PICANTO", "NIRO", "EV6", "EV9", "STONIC", "SELTOS", "CARNIVAL", "SORENTO", "CEED", "RIO")
            b.contains("סובארו") || b.contains("subaru") -> listOf("FORESTER", "OUTBACK", "CROSSTREK", "XV", "IMPREZA", "BRZ", "SOLTERRA", "EVOLTIS")
            b.contains("מאזדה") || b.contains("mazda") -> listOf("MAZDA 3", "MAZDA 2", "MAZDA 6", "CX-5", "CX-30", "CX-60", "CX-90", "MX-5")
            b.contains("סקודה") || b.contains("skoda") -> listOf("OCTAVIA", "SUPERB", "KODIAQ", "KAROQ", "KAMIQ", "FABIA", "SCALA", "ENYAQ")
            b.contains("טסלה") || b.contains("tesla") -> listOf("MODEL 3", "MODEL Y", "MODEL S", "MODEL X", "CYBERTRUCK")
            b.contains("מרצדס") || b.contains("mercedes") -> listOf("A-CLASS", "C-CLASS", "E-CLASS", "S-CLASS", "GLA", "GLB", "GLC", "GLE", "GLS", "EQA", "EQB", "EQE", "EQS", "G-CLASS")
            b.contains("ב.מ.וו") || b.contains("bmw") -> listOf("SERIES 1", "SERIES 3", "SERIES 5", "SERIES 7", "X1", "X3", "X5", "X6", "X7", "I4", "IX", "M3", "M5")
            b.contains("אאודי") || b.contains("audi") -> listOf("A3", "A4", "A6", "A8", "Q3", "Q5", "Q7", "Q8", "E-TRON", "RS3", "RS6", "TT")
            b.contains("פולקסווגן") || b.contains("volkswagen") || b.contains("vw") -> listOf("GOLF", "POLO", "TIGUAN", "PASSAT", "T-ROC", "TAIGO", "ID.4", "ID.5", "TOUAREG")
            b.contains("byd") || b.contains("בי ואי די") -> listOf("ATTO 3", "DOLPHIN", "SEAL", "TANG", "HAN", "SEAL U")
            b.contains("ג'ילי") || b.contains("geely") -> listOf("GEOMETRY C", "GEOMETRY E", "EX5")
            b.contains("mg") || b.contains("אם ג'י") -> listOf("MG 4", "MG ZS", "MG 5", "EHS", "MARVEL R", "CYBERSTER")
            b.contains("קופרה") || b.contains("cupra") -> listOf("FORMENTOR", "LEON", "ATECA", "BORN", "TAVASCAN")
            b.contains("פורד") || b.contains("ford") -> listOf("FOCUS", "KUGA", "PUMA", "MUSTANG", "MUSTANG MACH-E", "EXPLORER", "RANGER", "BRONCO")
            b.contains("פיג'ו") || b.contains("peugeot") -> listOf("208", "2008", "308", "3008", "5008", "408", "508")
            b.contains("רנו") || b.contains("renault") -> listOf("CLIO", "CAPTUR", "MEGANE", "ARKANA", "AUSTRAL", "ZOE")
            b.contains("וולוו") || b.contains("volvo") -> listOf("XC40", "XC60", "XC90", "EX30", "EX90", "S60", "V60")
            b.contains("סיאט") || b.contains("seat") -> listOf("IBIZA", "LEON", "ARONA", "ATECA", "TARRACO")
            b.contains("סוזוקי") || b.contains("suzuki") -> listOf("SWIFT", "VITARA", "S-CROSS", "IGNIS", "JIMNY")
            b.contains("הונדה") || b.contains("honda") -> listOf("CIVIC", "HR-V", "CR-V", "JAZZ", "ACCORD")
            b.contains("מיצובישי") || b.contains("mitsubishi") -> listOf("OUTLANDER", "ECLIPSE CROSS", "SPACE STAR", "ASX", "TRITON")
            b.contains("ניסאן") || b.contains("nissan") -> listOf("QASHQAI", "X-TRAIL", "JUKE", "MICRA", "LEAF", "ARIYA")
            b.contains("פורשה") || b.contains("porsche") -> listOf("911", "CAYENNE", "MACAN", "PANAMERA", "TAYCAN", "718 BOXSTER")
            else -> emptyList()
        }

        return if (defaultList.isNotEmpty()) listOf("כל הדגמים") + defaultList else emptyList()
    }

    fun loadInitialImages(brandOrQuery: String) {
        scope.launch {
            isLoading = true
            nextOffset = null
            detectedPlateVehicleInfo = null

            val cleanDigits = brandOrQuery.filter { it.isDigit() }
            if (cleanDigits.length in 5..8) {
                // Smart Government database license plate lookup
                try {
                    val vehResp = withContext(Dispatchers.IO) {
                        NetworkClient.apiService.getPrivateVehicle(filters = "{\"mispar_rechev\": $cleanDigits}")
                    }
                    val veh = vehResp.result?.records?.firstOrNull()
                    if (veh != null) {
                        val make = veh.make.orEmpty()
                        val model = veh.model.orEmpty()
                        val yr = veh.year?.toString().orEmpty()
                        detectedPlateVehicleInfo = "$make $model $yr".trim()
                        
                        val page = WikimediaGalleryService.fetchGalleryPage(make, model, offset = 0, limit = 40)
                        images = page.images
                        nextOffset = page.nextOffset
                        isLoading = false
                        return@launch
                    }
                } catch (_: Exception) {
                    // Fallback to text query
                }
            }

            val words = brandOrQuery.trim().split(" ")
            val make = words.firstOrNull() ?: ""
            val model = if (words.size > 1) words.drop(1).joinToString(" ") else ""
            val page = WikimediaGalleryService.fetchGalleryPage(make, model, offset = 0, limit = 40)
            images = page.images
            nextOffset = page.nextOffset
            isLoading = false
        }
    }

    fun loadMoreImages() {
        val offset = nextOffset ?: return
        if (isLoadingMore || isLoading) return
        scope.launch {
            isLoadingMore = true
            val currentQuery = if (detectedPlateVehicleInfo != null) {
                detectedPlateVehicleInfo!!
            } else if (searchQuery.isNotBlank()) {
                searchQuery
            } else if (selectedModel != "כל הדגמים") {
                "$selectedBrand $selectedModel"
            } else {
                selectedBrand
            }

            val words = currentQuery.trim().split(" ")
            val make = words.firstOrNull() ?: ""
            val model = if (words.size > 1) words.drop(1).joinToString(" ") else ""
            val page = WikimediaGalleryService.fetchGalleryPage(make, model, offset = offset, limit = 40)
            
            // Deduplicate by URL
            val existingUrls = images.map { it.imageUrl }.toSet()
            val newImages = page.images.filterNot { it.imageUrl in existingUrls }
            images = images + newImages
            nextOffset = page.nextOffset
            isLoadingMore = false
        }
    }

    LaunchedEffect(selectedBrand) {
        if (searchQuery.isBlank()) {
            selectedModel = "כל הדגמים"
            loadInitialImages(selectedBrand)
        }
    }

    // Fullscreen Image Dialog with Detailed Info, License & Direct Source Link
    selectedImageForViewer?.let { imageItem ->
        Dialog(
            onDismissRequest = { selectedImageForViewer = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageItem.imageUrl)
                        .setHeader("User-Agent", "VehicleCheckApp/1.0 (https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)")
                        .crossfade(true)
                        .build(),
                    contentDescription = imageItem.altText.ifBlank { imageItem.title },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.72f),
                    contentScale = ContentScale.Fit
                )

                // Top Action Bar (Close & Share)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedImageForViewer = null },
                        modifier = Modifier.handCursor()
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "סגור חלון תצוגה מקדימה", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "תמונת רכב (${imageItem.title}):\n${imageItem.imageUrl}\nמקור: ${imageItem.descriptionUrl}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "שתף תמונת רכב"))
                        },
                        modifier = Modifier.handCursor()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "שתף תמונת רכב", tint = Color.White)
                    }
                }

                // Bottom Rich Info Card (Title, License, Artist, Clickable Source Link)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title / Model Name
                        Text(
                            text = imageItem.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        // Artist & License details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (imageItem.artist.isNotBlank()) {
                                Text(
                                    text = "יוצר: ${imageItem.artist} • ",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "רישיון: ${imageItem.license}",
                                color = Color(0xFF81D4FA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Open in Wikimedia Commons Button
                        if (imageItem.descriptionUrl.isNotBlank()) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(imageItem.descriptionUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) { }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.handCursor()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "צפייה במקור וזכויות יוצרים בוויקימדיה",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val availableModelsForCurrentBrand = remember(selectedBrand, makeToModelsMap) {
        if (selectedBrand == "הכל") emptyList() else getModelsForCurrentBrand(selectedBrand)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Search Header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("חיפוש מספר רכב, יצרן או דגם (עברית/אנגלית)") },
            placeholder = { Text("למשל: 12-345-67, שברולט קורבט, Tesla Model Y...") },
            leadingIcon = {
                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty()) {
                        HoverTooltipIconButton(
                            onClick = {
                                searchQuery = ""
                                loadInitialImages(selectedBrand)
                            },
                            tooltipText = "נקה חיפוש"
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            if (searchQuery.isNotBlank()) {
                                loadInitialImages(searchQuery)
                            } else {
                                loadInitialImages(selectedBrand)
                            }
                        },
                        modifier = Modifier.handCursor()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController?.hide()
                if (searchQuery.isNotBlank()) {
                    loadInitialImages(searchQuery)
                } else {
                    loadInitialImages(selectedBrand)
                }
            }),
            shape = RoundedCornerShape(16.dp)
        )

        // Live Gov Vehicle Identification Badge
        if (detectedPlateVehicleInfo != null) {
            Spacer(Modifier.height(6.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "זוהה ממאגר משרד התחבורה: $detectedPlateVehicleInfo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Row 1: All Manufacturers Filter Chips (Loaded dynamically from Gov database)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(dynamicManufacturers) { brand ->
                FilterChip(
                    selected = (searchQuery.isBlank() && selectedBrand == brand) || (searchQuery.trim().equals(brand, ignoreCase = true)),
                    onClick = {
                        searchQuery = ""
                        selectedBrand = brand
                        selectedModel = "כל הדגמים"
                        keyboardController?.hide()
                        loadInitialImages(brand)
                    },
                    label = { Text(brand, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.handCursor()
                )
            }
        }

        // Row 2: Certified Models for Selected Brand
        if (availableModelsForCurrentBrand.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Text(
                        text = "דגמים:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                items(availableModelsForCurrentBrand) { model ->
                    FilterChip(
                        selected = selectedModel == model,
                        onClick = {
                            selectedModel = model
                            searchQuery = ""
                            keyboardController?.hide()
                            if (model == "כל הדגמים") {
                                loadInitialImages(selectedBrand)
                            } else {
                                loadInitialImages("$selectedBrand $model")
                            }
                        },
                        label = { Text(model, fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.handCursor()
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Copyright / DMCA Safe Harbor Notice Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "כל התמונות בגלריה מוצגות ברישיון שימוש חופשי (Wikimedia Commons). דיווח זכויות יוצרים בכפתור \"יצירת קשר\".",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Header info
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedBrand != "הכל") {
                    if (selectedModel != "כל הדגמים") "גלריית $selectedBrand $selectedModel" else "גלריית כל רכבי $selectedBrand"
                } else "גלריית כל הרכבים (Wikimedia Commons)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            if (images.isNotEmpty()) {
                Text(
                    text = "מוצגות ${images.size} תמונות",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(10.dp))
                    Text("טוען רכבים מהמאגר...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (images.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "לא נמצאו תמונות עבור \"${if (searchQuery.isNotBlank()) searchQuery else "$selectedBrand $selectedModel"}\".\nבחר באחד היצרנים או הדגמים למעלה.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            searchQuery = ""
                            selectedBrand = "הכל"
                            selectedModel = "כל הדגמים"
                            loadInitialImages("הכל")
                        },
                        modifier = Modifier.handCursor()
                    ) {
                        Text("הצג את כל הרכבים")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(images, key = { it.imageUrl }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedImageForViewer = item },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(item.thumbUrl)
                                    .setHeader("User-Agent", "VehicleCheckApp/1.0 (https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.altText.ifBlank { item.title },
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Title overlay on bottom
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(),
                                color = Color.Black.copy(alpha = 0.55f)
                            ) {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Load more footer / button
                if (nextOffset != null) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingMore) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Text("טוען עוד רכבים...", style = MaterialTheme.typography.bodySmall)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { loadMoreImages() },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(0.6f).handCursor()
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("טען עוד רכבים")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}