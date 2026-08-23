package com.avih6.vehiclecheck.ui.screens

import android.content.Intent
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
import com.avih6.vehiclecheck.data.WikimediaGalleryService
import com.avih6.vehiclecheck.ui.components.HoverTooltipIconButton
import com.avih6.vehiclecheck.ui.components.handCursor
import kotlinx.coroutines.launch

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
    var images by remember { mutableStateOf<List<CarGalleryImage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var nextOffset by remember { mutableStateOf<Int?>(null) }
    var selectedImageForViewer by remember { mutableStateOf<CarGalleryImage?>(null) }

    val popularManufacturers = remember {
        listOf(
            "הכל", "טויוטה", "יונדאי", "קיה", "מאזדה", "סקודה", "טסלה", "סובארו",
            "מרצדס", "ב.מ.וו", "אאודי", "פולקסווגן", "BYD", "ג'ילי", "MG", "קופרה",
            "שברולט", "פורד", "פיג'ו", "רנו", "סיטרואן", "וולוו", "סוזוקי", "הונדה",
            "מיצובישי", "ניסאן", "סיאט", "דאצ'יה", "לקסוס", "פורשה", "ג'יפ", "קאדילאק"
        )
    }

    fun loadInitialImages(brandOrQuery: String) {
        scope.launch {
            isLoading = true
            nextOffset = null
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
            val currentQuery = if (searchQuery.isNotBlank()) searchQuery else selectedBrand
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
            loadInitialImages(selectedBrand)
        }
    }

    // Fullscreen Image Dialog
    selectedImageForViewer?.let { imageItem ->
        Dialog(
            onDismissRequest = { selectedImageForViewer = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageItem.imageUrl)
                        .setHeader("User-Agent", "VehicleCheckApp/1.0 (https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)")
                        .crossfade(true)
                        .build(),
                    contentDescription = imageItem.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.78f),
                    contentScale = ContentScale.Fit
                )

                // Top Controls
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
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "תמונת רכב (${imageItem.title}):\n${imageItem.imageUrl}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "שתף תמונת רכב"))
                        },
                        modifier = Modifier.handCursor()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                }

                // Bottom Title
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = imageItem.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
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
            label = { Text("חיפוש כל יצרן, דגם או שנה (עברית/אנגלית)") },
            placeholder = { Text("למשל: טויוטה קורולה, Tesla Model Y, סובארו...") },
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

        Spacer(Modifier.height(8.dp))

        // All Manufacturers Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(popularManufacturers) { brand ->
                FilterChip(
                    selected = (searchQuery.isBlank() && selectedBrand == brand) || (searchQuery.trim().equals(brand, ignoreCase = true)),
                    onClick = {
                        searchQuery = ""
                        selectedBrand = brand
                        keyboardController?.hide()
                        loadInitialImages(brand)
                    },
                    label = { Text(brand, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.handCursor()
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Copyright / DMCA Safe Harbor Notice Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                text = "גלריית כל הרכבים (Wikimedia Commons)",
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
                        text = "לא נמצאו תמונות עבור \"${if (searchQuery.isNotBlank()) searchQuery else selectedBrand}\".\nבחר באחד היצרנים למעלה או חפש שם דגם.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            searchQuery = ""
                            selectedBrand = "הכל"
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
                                contentDescription = item.title,
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