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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
    viewModel: com.avih6.vehiclecheck.MainViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    val logGalleryEvent: (String, android.os.Bundle) -> Unit = { name, bundle ->
        try {
            if (viewModel != null) {
                viewModel.logEvent(name, bundle)
            } else {
                com.google.firebase.analytics.FirebaseAnalytics.getInstance(context).logEvent(name, bundle)
            }
        } catch (_: Exception) {}
    }

    var searchQuery by remember { mutableStateOf(if (initialQuery == "הכל") "" else initialQuery) }
    var selectedBrand by remember { mutableStateOf(if (initialQuery.isBlank()) "הכל" else initialQuery) }
    var selectedModel by remember { mutableStateOf("כל הדגמים") }
    var images by remember { mutableStateOf<List<CarGalleryImage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var nextOffset by remember { mutableStateOf<Int?>(null) }
    var selectedImageIndexForViewer by remember { mutableStateOf<Int?>(null) }
    var detectedPlateVehicleInfo by remember { mutableStateOf<String?>(null) }

    val fallbackManufacturers = remember {
        listOf(
            "הכל", "טויוטה", "יונדאי", "קיה", "מאזדה", "סקודה", "טסלה", "סובארו",
            "שברולט", "מרצדס", "ב.מ.וו", "אאודי", "פולקסווגן", "BYD", "ג'ילי", "MG", "קופרה",
            "פורד", "פיג'ו", "רנו", "סיטרואן", "וולוו", "סוזוקי", "הונדה",
            "מיצובישי", "ניסאן", "סיאט", "דאצ'יה", "לקסוס", "פורשה", "ג'יפ", "קאדילאק",
            "פולריס", "קאן-אם", "CFMOTO", "קלאב קאר"
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
            b.contains("וולוו") || b.contains("וולבו") || b.contains("volvo") -> listOf("XC40", "XC60", "XC90", "EX30", "EX90", "S60", "V60", "FE", "FL", "FH")
            b.contains("פולריס") || b.contains("polaris") -> listOf("RZR", "RANGER", "GENERAL", "SPORTSMAN", "SCRAMBLER")
            b.contains("קאן") || b.contains("can-am") || b.contains("can am") -> listOf("MAVERICK", "TRAXTER", "OUTLANDER", "DEFENDER", "COMMANDER")
            b.contains("cfmoto") || b.contains("סי אף מוטו") -> listOf("ZFORCE", "CFORCE", "UFORCE")
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

            val (make, model) = parseMakeAndModelFromQuery(brandOrQuery)
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

            val (make, model) = parseMakeAndModelFromQuery(currentQuery)
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

    // Fullscreen Image Dialog with Pager for swiping, Detailed Info, License & Direct Source Link
    selectedImageIndexForViewer?.let { initialIndex ->
        val pagerState = rememberPagerState(
            initialPage = initialIndex,
            pageCount = { images.size }
        )
        var showOverlays by remember { mutableStateOf(true) }

        Dialog(
            onDismissRequest = { selectedImageIndexForViewer = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        showOverlays = !showOverlays
                    },
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val imageItem = images.getOrNull(page)
                    if (imageItem != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            var isFullImageLoaded by remember(imageItem.imageUrl) { mutableStateOf(false) }

                            // 1. Instant thumbnail from cache (already loaded in grid, zero delay!)
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageItem.thumbUrl)
                                    .setHeader("User-Agent", "VehicleCheckApp/1.0 (https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            // 2. High-res image loaded smoothly on top
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageItem.imageUrl)
                                    .setHeader("User-Agent", "VehicleCheckApp/1.0 (https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)")
                                    .crossfade(true)
                                    .listener(
                                        onSuccess = { _, _ -> isFullImageLoaded = true },
                                        onError = { _, _ -> isFullImageLoaded = true }
                                    )
                                    .build(),
                                contentDescription = imageItem.altText.ifBlank { imageItem.title },
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            if (!isFullImageLoaded) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp).align(Alignment.Center),
                                    color = Color.White.copy(alpha = 0.65f),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }

                // Chevrons inside fullscreen for easy navigation
                androidx.compose.animation.AnimatedVisibility(
                    visible = showOverlays && images.size > 1,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val prev = (pagerState.currentPage - 1 + images.size) % images.size
                                pagerState.animateScrollToPage(prev)
                            }
                        },
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "התמונה הקודמת", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showOverlays && images.size > 1,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val next = (pagerState.currentPage + 1) % images.size
                                pagerState.animateScrollToPage(next)
                            }
                        },
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "התמונה הבאה", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }

                // Get current image details for overlays
                val currentImage = images.getOrNull(pagerState.currentPage)
                if (currentImage != null) {
                    // Top Action Bar (Close & Share)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showOverlays,
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { selectedImageIndexForViewer = null },
                                modifier = Modifier.handCursor()
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "סגור חלון תצוגה מקדימה", tint = Color.White)
                            }

                            if (images.size > 1) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                                ) {
                                    Text(
                                        text = "${pagerState.currentPage + 1} / ${images.size}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "תמונת רכב (${currentImage.title}):\n${currentImage.imageUrl}\nמקור: ${currentImage.descriptionUrl}")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "שתף תמונת רכב"))
                                },
                                modifier = Modifier.handCursor()
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "שתף תמונת רכב", tint = Color.White)
                            }
                        }
                    }

                    // Bottom Rich Info Card (Title, License, Artist, Clickable Source Link & Model Stats)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showOverlays,
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(start = 16.dp, end = 16.dp, bottom = 22.dp),
                            color = Color.Black.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Title / Model Name
                                Text(
                                    text = currentImage.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                // Recognized Car Model (Gov DB verified) with navigation to Statistics screen
                                val recognized = remember(currentImage.title, currentImage.description, makeToModelsMap) {
                                    detectRecognizedVehicleModel(currentImage.title, currentImage.description, makeToModelsMap)
                                }
                                if (recognized != null) {
                                    val (recMake, recModel) = recognized
                                    val fullQuery = "$recMake $recModel".trim()
                                    Spacer(Modifier.height(6.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel?.searchModelStatistics(fullQuery)
                                            viewModel?.setSelectedTab(2)
                                            selectedImageIndexForViewer = null
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.handCursor()
                                    ) {
                                        Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "דגם מאומת: $fullQuery • צפה בסטטיסטיקה ומפרט",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(Modifier.height(4.dp))

                                // Artist & License details
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (currentImage.artist.isNotBlank()) {
                                        Text(
                                            text = "יוצר: ${currentImage.artist} • ",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "רישיון: ${currentImage.license}",
                                        color = Color(0xFF81D4FA),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (currentImage.descriptionUrl.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentImage.descriptionUrl))
                                                context.startActivity(intent)
                                            } catch (_: Exception) { }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.handCursor()
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "צפייה במקור וזכויות יוצרים בוויקימדיה",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
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
                            val q = searchQuery.trim()
                            if (q.isNotBlank()) {
                                val isPlate = q.filter { it.isDigit() }.length in 5..8
                                logGalleryEvent("gallery_search_performed", android.os.Bundle().apply {
                                    putBoolean("is_plate", isPlate)
                                    if (!isPlate) putString("query", q.take(40))
                                    putInt("query_length", q.length)
                                })
                                loadInitialImages(q)
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
                val q = searchQuery.trim()
                if (q.isNotBlank()) {
                    val isPlate = q.filter { it.isDigit() }.length in 5..8
                    logGalleryEvent("gallery_search_performed", android.os.Bundle().apply {
                        putBoolean("is_plate", isPlate)
                        if (!isPlate) putString("query", q.take(40))
                        putInt("query_length", q.length)
                    })
                    loadInitialImages(q)
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
                        logGalleryEvent("gallery_brand_filter_selected", android.os.Bundle().apply {
                            putString("brand_name", brand)
                        })
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
                            logGalleryEvent("gallery_model_filter_selected", android.os.Bundle().apply {
                                putString("brand_name", selectedBrand)
                                putString("model_name", model)
                            })
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
                    text = "מוצגות %,d תמונות".format(images.size),
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
                    val displayTarget = when {
                        searchQuery.isNotBlank() -> "\"$searchQuery\""
                        selectedBrand == "הכל" -> "כל הרכבים"
                        selectedModel != "כל הדגמים" -> "\"$selectedBrand $selectedModel\""
                        else -> "\"$selectedBrand\""
                    }
                    Text(
                        text = "לא נמצאו תמונות עבור $displayTarget.\nבחר באחד היצרנים או הדגמים למעלה.",
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
                itemsIndexed(images, key = { _, item -> item.imageUrl }) { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedImageIndexForViewer = index
                                logGalleryEvent("gallery_image_viewed", android.os.Bundle().apply {
                                    putString("title", item.title.take(40))
                                    putString("license", item.license.take(30))
                                })
                            },
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

                            // Certified model chip (links to statistics)
                            val recognized = remember(item.title, item.description, makeToModelsMap) {
                                detectRecognizedVehicleModel(item.title, item.description, makeToModelsMap)
                            }
                            if (recognized != null) {
                                val (recMake, recModel) = recognized
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .clickable {
                                            viewModel?.searchModelStatistics("$recMake $recModel".trim())
                                            viewModel?.setSelectedTab(2)
                                        },
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.BarChart,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text = "$recMake $recModel".trim(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

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

fun detectRecognizedVehicleModel(
    title: String,
    description: String,
    makeToModelsMap: Map<String, List<String>> = emptyMap()
): Pair<String, String>? {
    val text = "$title $description".lowercase()

    val specificModels = listOf(
        "atto 3" to ("BYD" to "Atto 3"),
        "atto3" to ("BYD" to "Atto 3"),
        "dolphin" to ("BYD" to "Dolphin"),
        "seal u" to ("BYD" to "Seal U"),
        "seal" to ("BYD" to "Seal"),
        "tang" to ("BYD" to "Tang"),
        "han" to ("BYD" to "Han"),
        "model 3" to ("טסלה" to "Model 3"),
        "model y" to ("טסלה" to "Model Y"),
        "model s" to ("טסלה" to "Model S"),
        "model x" to ("טסלה" to "Model X"),
        "cybertruck" to ("טסלה" to "Cybertruck"),
        "corolla cross" to ("טויוטה" to "Corolla Cross"),
        "corolla" to ("טויוטה" to "Corolla"),
        "yaris cross" to ("טויוטה" to "Yaris Cross"),
        "yaris" to ("טויוטה" to "Yaris"),
        "rav4" to ("טויוטה" to "RAV4"),
        "land cruiser" to ("טויוטה" to "Land Cruiser"),
        "camry" to ("טויוטה" to "Camry"),
        "c-hr" to ("טויוטה" to "C-HR"),
        "hilux" to ("טויוטה" to "Hilux"),
        "tucson" to ("יונדאי" to "Tucson"),
        "ioniq 5" to ("יונדאי" to "Ioniq 5"),
        "ioniq 6" to ("יונדאי" to "Ioniq 6"),
        "ioniq" to ("יונדאי" to "Ioniq"),
        "kona" to ("יונדאי" to "Kona"),
        "elantra" to ("יונדאי" to "Elantra"),
        "santa fe" to ("יונדאי" to "Santa Fe"),
        "i10" to ("יונדאי" to "i10"),
        "i20" to ("יונדאי" to "i20"),
        "i30" to ("יונדאי" to "i30"),
        "sportage" to ("קיה" to "Sportage"),
        "picanto" to ("קיה" to "Picanto"),
        "niro" to ("קיה" to "Niro"),
        "ev6" to ("קיה" to "EV6"),
        "ev9" to ("קיה" to "EV9"),
        "stonic" to ("קיה" to "Stonic"),
        "sorento" to ("קיה" to "Sorento"),
        "octavia" to ("סקודה" to "Octavia"),
        "kodiaq" to ("סקודה" to "Kodiaq"),
        "karoq" to ("סקודה" to "Karoq"),
        "superb" to ("סקודה" to "Superb"),
        "kamiq" to ("סקודה" to "Kamiq"),
        "fabia" to ("סקודה" to "Fabia"),
        "enyaq" to ("סקודה" to "Enyaq"),
        "golf" to ("פולקסווגן" to "Golf"),
        "polo" to ("פולקסווגן" to "Polo"),
        "tiguan" to ("פולקסווגן" to "Tiguan"),
        "t-roc" to ("פולקסווגן" to "T-Roc"),
        "passat" to ("פולקסווגן" to "Passat"),
        "id.4" to ("פולקסווגן" to "ID.4"),
        "id.3" to ("פולקסווגן" to "ID.3"),
        "cx-5" to ("מאזדה" to "CX-5"),
        "cx-30" to ("מאזדה" to "CX-30"),
        "mazda 3" to ("מאזדה" to "Mazda 3"),
        "mazda 2" to ("מאזדה" to "Mazda 2"),
        "mazda 6" to ("מאזדה" to "Mazda 6"),
        "formentor" to ("קופרה" to "Formentor"),
        "ibiza" to ("סיאט" to "Ibiza"),
        "arona" to ("סיאט" to "Arona"),
        "ateca" to ("סיאט" to "Ateca"),
        "leon" to ("סיאט" to "Leon"),
        "carryall" to ("קלאב קאר" to "Carryall"),
        "rzr" to ("פולריס" to "RZR"),
        "sportsman" to ("פולריס" to "Sportsman"),
        "ranger" to ("פולריס" to "Ranger"),
        "general" to ("פולריס" to "General"),
        "maverick" to ("קאן-אם" to "Maverick"),
        "traxter" to ("קאן-אם" to "Traxter"),
        "outlander" to ("מיצובישי" to "Outlander"),
        "cforce" to ("CFMOTO" to "CForce"),
        "zforce" to ("CFMOTO" to "ZForce"),
        "uforce" to ("CFMOTO" to "UForce"),
        "forester" to ("סובארו" to "Forester"),
        "crosstrek" to ("סובארו" to "Crosstrek"),
        "outback" to ("סובארו" to "Outback"),
        "geometry c" to ("ג'ילי" to "Geometry C"),
        "mg 4" to ("MG" to "MG 4"),
        "mg zs" to ("MG" to "MG ZS"),
        "fe electric" to ("וולוו" to "FE"),
        "volvo fe" to ("וולוו" to "FE")
    )

    for ((key, pair) in specificModels) {
        if (text.contains(key)) {
            return pair
        }
    }

    for ((make, models) in makeToModelsMap) {
        val makeLower = make.lowercase()
        if (text.contains(makeLower)) {
            for (model in models.sortedByDescending { it.length }) {
                if (model != "כל הדגמים" && model.length >= 3 && text.contains(model.lowercase())) {
                    return Pair(make, model)
                }
            }
        }
    }

    return null
}

private fun parseMakeAndModelFromQuery(text: String): Pair<String, String> {
    val trimmed = text.trim()
    val lower = trimmed.lowercase()
    val twoWordMakes = listOf(
        "club car", "club-car", "קלאב קאר", "קלאב-קאר", "קלאבקאר",
        "land rover", "land-rover", "לנד רובר",
        "alfa romeo", "alfa-romeo", "אלפא רומיאו", "אלפא רומאו",
        "aston martin", "aston-martin", "אסטון מרטין",
        "rolls royce", "rolls-royce", "רולס רויס",
        "mercedes benz", "mercedes-benz", "מרצדס בנץ",
        "golden dragon", "golden-dragon", "גולדן דרגון",
        "king long", "king-long", "קינג לונג",
        "john deere", "john-deere", "ג'ון דיר",
        "new holland", "new-holland", "ניו הולנד",
        "massey ferguson", "massey-ferguson", "מסי פרגוסון",
        "arctic cat", "arctic-cat", "ארקטיק קאט"
    )
    for (twoWord in twoWordMakes) {
        if (lower.startsWith(twoWord)) {
            val make = trimmed.substring(0, twoWord.length).trim()
            val model = trimmed.substring(twoWord.length).trim()
            return Pair(make, model)
        }
    }
    val words = trimmed.split(" ")
    val make = words.firstOrNull() ?: ""
    val model = if (words.size > 1) words.drop(1).joinToString(" ") else ""
    return Pair(make, model)
}