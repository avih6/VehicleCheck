package com.avih6.vehiclecheck.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.avih6.vehiclecheck.data.VehicleUtils
import com.avih6.vehiclecheck.data.WikimediaGalleryService
import kotlinx.coroutines.launch

@Composable
fun GalleryScreen(
    initialQuery: String = "Subaru Forester",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf(initialQuery) }
    var images by remember { mutableStateOf<List<CarGalleryImage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageForViewer by remember { mutableStateOf<CarGalleryImage?>(null) }

    val popularSuggestions = remember {
        listOf("Subaru Forester", "Toyota Corolla", "Hyundai Tucson", "Kia Sportage", "Mazda 3", "Tesla Model 3", "Skoda Octavia", "Daihatsu Charade")
    }

    fun loadImages(query: String) {
        if (query.isBlank()) return
        scope.launch {
            isLoading = true
            val words = query.trim().split(" ")
            val make = words.firstOrNull() ?: ""
            val model = if (words.size > 1) words.drop(1).joinToString(" ") else ""
            images = WikimediaGalleryService.fetchCarImages(make, model)
            isLoading = false
        }
    }

    LaunchedEffect(searchQuery) {
        loadImages(searchQuery)
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
                    IconButton(onClick = { selectedImageForViewer = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    IconButton(onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "תמונת רכב (${imageItem.title}):\n${imageItem.imageUrl}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "שתף תמונת רכב"))
                    }) {
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
            .padding(16.dp)
    ) {
        // Search Header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("חיפוש תמונות רכב (עברית או אנגלית)") },
            placeholder = { Text("למשל: Subaru Forester, טויוטה קורולה...") },
            leadingIcon = {
                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                IconButton(onClick = {
                    keyboardController?.hide()
                    loadImages(searchQuery)
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController?.hide()
                loadImages(searchQuery)
            }),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(10.dp))

        // Quick Suggestions
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(popularSuggestions) { item ->
                FilterChip(
                    selected = searchQuery == item,
                    onClick = {
                        searchQuery = item
                        keyboardController?.hide()
                        loadImages(item)
                    },
                    label = { Text(item, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Copyright / DMCA Notice Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "כל התמונות בגלריה מוצגות ברישיון שימוש חופשי (Wikimedia Commons). אם הנך סבור שקיימת פגיעה בזכויות יוצרים, אנא פנה אלינו באמצעות כפתור \"יצירת קשר\" בתפריט להסרה מיידית.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Header info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "גלריית תמונות חופשית (Wikimedia Commons)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            if (images.isNotEmpty()) {
                Text(
                    text = "${images.size} תמונות",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(10.dp))
                    Text("טוען תמונות...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        text = "לא נמצאו תמונות עבור \"$searchQuery\".\nנסה לבחור באחד הדגמים למעלה או חפש שם באנגלית.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { loadImages("Subaru Forester") }) {
                        Text("הצג תמונות של Subaru Forester")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
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
                                color = Color.Black.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}