package com.avih6.vehiclecheck.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
    color: String?,
    trimLevel: String? = null,
    category: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var images by remember { mutableStateOf<List<CarGalleryImage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFullscreenIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(hebrewMake, modelName, year, color, trimLevel, category) {
        isLoading = true
        images = WikimediaGalleryService.fetchCarImagesSpecific(
            make = hebrewMake.orEmpty(),
            model = modelName.orEmpty(),
            year = year,
            colorHeb = color,
            trimLevel = trimLevel,
            category = category,
            limit = 12
        )
        isLoading = false
    }

    val cardPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { images.size }
    )

    // Fullscreen Dialog with HorizontalPager (Swiping between all photos)
    selectedFullscreenIndex?.let { initialIndex ->
        val fullscreenPagerState = rememberPagerState(
            initialPage = initialIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0)),
            pageCount = { images.size }
        )
        var showOverlays by remember { mutableStateOf(true) }

        val currentFullscreenImage = images.getOrNull(fullscreenPagerState.currentPage)

        Dialog(
            onDismissRequest = {
                // Sync back to card pager
                scope.launch {
                    if (images.isNotEmpty()) {
                        cardPagerState.scrollToPage(fullscreenPagerState.currentPage)
                    }
                }
                selectedFullscreenIndex = null
            },
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
                // HorizontalPager allowing smooth swipe between all images
                HorizontalPager(
                    state = fullscreenPagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val img = images.getOrNull(page)
                    if (img != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(img.imageUrl)
                                    .setHeader("User-Agent", "VehicleCheckApp/1.0 (https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = img.altText.ifBlank { "תמונת רכב $hebrewMake $modelName" },
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                // Chevrons inside fullscreen for easy tapping or remote navigation
                androidx.compose.animation.AnimatedVisibility(
                    visible = showOverlays && images.size > 1,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val prev = (fullscreenPagerState.currentPage - 1 + images.size) % images.size
                                fullscreenPagerState.animateScrollToPage(prev)
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
                                val next = (fullscreenPagerState.currentPage + 1) % images.size
                                fullscreenPagerState.animateScrollToPage(next)
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

                // Top Bar with Close, Counter, and Share
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
                            onClick = {
                                scope.launch {
                                    if (images.isNotEmpty()) {
                                        cardPagerState.scrollToPage(fullscreenPagerState.currentPage)
                                    }
                                }
                                selectedFullscreenIndex = null
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "סגור מסך מלא", tint = Color.White)
                        }

                        if (images.size > 1) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = "${fullscreenPagerState.currentPage + 1} / ${images.size}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (currentFullscreenImage != null) {
                            IconButton(onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "תמונת רכב ($hebrewMake $modelName):\n${currentFullscreenImage.imageUrl}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "שתף תמונת רכב"))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "שתף תמונת רכב", tint = Color.White)
                            }
                        } else {
                            Spacer(Modifier.width(48.dp))
                        }
                    }
                }

                // Bottom Rich Info Card (Title, License, Artist, Clickable Source Link)
                if (currentFullscreenImage != null) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showOverlays,
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = currentFullscreenImage.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                Spacer(Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (currentFullscreenImage.artist.isNotBlank()) {
                                        Text(
                                            text = "יוצר: ${currentFullscreenImage.artist} • ",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "רישיון: ${currentFullscreenImage.license}",
                                        color = Color(0xFF81D4FA),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (currentFullscreenImage.descriptionUrl.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentFullscreenImage.descriptionUrl))
                                                context.startActivity(intent)
                                            } catch (_: Exception) { }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
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

    androidx.compose.animation.AnimatedVisibility(
        visible = images.isNotEmpty(),
        enter = androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(350)) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(350)),
        exit = androidx.compose.animation.shrinkVertically(animationSpec = androidx.compose.animation.core.tween(250)) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(250))
    ) {
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
                HorizontalPager(
                    state = cardPagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val img = images.getOrNull(page)
                    if (img != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(img.thumbUrl)
                                .setHeader("User-Agent", "VehicleCheckApp/1.0 (https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)")
                                .crossfade(true)
                                .build(),
                            contentDescription = img.altText.ifBlank { "תמונת רכב $hebrewMake $modelName" },
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { selectedFullscreenIndex = cardPagerState.currentPage },
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Navigation Chevrons (if multiple photos)
                if (images.size > 1) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val prev = (cardPagerState.currentPage - 1 + images.size) % images.size
                                cardPagerState.animateScrollToPage(prev)
                            }
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
                            scope.launch {
                                val next = (cardPagerState.currentPage + 1) % images.size
                                cardPagerState.animateScrollToPage(next)
                            }
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
            }

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
                                .size(if (cardPagerState.currentPage == index) 8.dp else 5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (cardPagerState.currentPage == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                                .clickable {
                                    scope.launch { cardPagerState.animateScrollToPage(index) }
                                }
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
                    text = "תמונה חופשית • החלק לתמונות נוספות",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    text = "${cardPagerState.currentPage + 1}/${images.size}",
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
