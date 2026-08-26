package com.avih6.vehiclecheck.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
fun VehicleImageShowcase(
    hebrewMake: String?,
    modelName: String?,
    year: Int?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var images by remember { mutableStateOf<List<CarGalleryImage>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var showFullscreen by remember { mutableStateOf(false) }

    val brandLogoUrl = remember(hebrewMake) {
        VehicleUtils.getBrandLogoUrl(hebrewMake)
    }

    LaunchedEffect(hebrewMake, modelName) {
        isLoading = true
        images = WikimediaGalleryService.fetchCarImages(hebrewMake.orEmpty(), modelName.orEmpty())
        currentIndex = 0
        isLoading = false
    }

    val currentImage = images.getOrNull(currentIndex)

    // Fullscreen Dialog
    if (showFullscreen && currentImage != null) {
        Dialog(
            onDismissRequest = { showFullscreen = false },
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
                        .data(currentImage.imageUrl)
                        .setHeader("User-Agent", "VehicleCheckApp/1.0 (https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)")
                        .crossfade(true)
                        .build(),
                    contentDescription = currentImage.altText.ifBlank { "תמונת רכב $hebrewMake $modelName" },
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
                    IconButton(onClick = { showFullscreen = false }) {
                        Icon(Icons.Default.Close, contentDescription = "סגור מסך מלא", tint = Color.White)
                    }

                    IconButton(onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "תמונת רכב ($hebrewMake $modelName):\n${currentImage.imageUrl}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "שתף תמונת רכב"))
                    }) {
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
                            text = currentImage.title,
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
                            if (currentImage.artist.isNotBlank()) {
                                Text(
                                    text = "יוצר: ${currentImage.artist} • ",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "רישיון: ${currentImage.license}",
                                color = Color(0xFF81D4FA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Open in Wikimedia Commons Button
                        if (currentImage.descriptionUrl.isNotBlank()) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentImage.descriptionUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) { }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                } else if (currentImage != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentImage.thumbUrl)
                            .setHeader("User-Agent", "VehicleCheckApp/1.0 (https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)")
                            .crossfade(true)
                            .build(),
                        contentDescription = currentImage.altText.ifBlank { "תמונת רכב $hebrewMake $modelName" },
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showFullscreen = true },
                        contentScale = ContentScale.Crop
                    )

                    // Navigation Chevrons (if multiple photos)
                    if (images.size > 1) {
                        IconButton(
                            onClick = {
                                currentIndex = (currentIndex - 1 + images.size) % images.size
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 6.dp)
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                .size(34.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "התמונה הקודמת", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                currentIndex = (currentIndex + 1) % images.size
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 6.dp)
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                .size(34.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "התמונה הבאה", tint = Color.White)
                        }
                    }
                } else {
                    // Fallback to generic vehicle / machinery placeholder
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val isMachinery = listOf("komatsu", "caterpillar", "cat", "jcb", "bobcat", "deere", "צמ\"ה", "מחפר", "שופל").any {
                            hebrewMake.orEmpty().contains(it, ignoreCase = true) || modelName.orEmpty().contains(it, ignoreCase = true)
                        }

                        Surface(
                            color = if (isMachinery) Color(0xFFFF9800).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isMachinery) Icons.Default.Construction else Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = if (isMachinery) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = listOfNotNull(hebrewMake, modelName).joinToString(" "),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (!isLoading && images.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))

                // Dot Indicators
                if (images.size > 1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        images.indices.take(8).forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (currentIndex == index) 8.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentIndex == index)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .clickable { currentIndex = index }
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "תמונה חופשית • מקור: ויקימדיה",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${currentIndex + 1}/${images.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
