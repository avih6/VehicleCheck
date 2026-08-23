package com.avih6.vehiclecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avih6.vehiclecheck.data.VehicleUtils

@Composable
fun VehicleImageShowcase(
    hebrewMake: String?,
    modelName: String?,
    year: Int?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val (makeEn, modelEn) = remember(hebrewMake, modelName) {
        VehicleUtils.getEnglishMakeAndModel(hebrewMake, modelName)
    }

    val brandLogoUrl = remember(hebrewMake) {
        VehicleUtils.getBrandLogoUrl(hebrewMake)
    }

    val angles = remember {
        listOf("01", "05", "09", "13", "17", "21", "25", "29")
    }

    var selectedAngleIndex by remember { mutableIntStateOf(0) }
    var isImageError by remember { mutableStateOf(false) }
    var isImageLoading by remember { mutableStateOf(true) }

    val angle = angles[selectedAngleIndex]

    val imageUrl = remember(makeEn, modelEn, angle) {
        "https://cdn.imagin.studio/getimage?customer=hrjavascript-mastery&make=$makeEn&modelFamily=$modelEn&zoomType=fullscreen&angle=$angle"
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
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isImageLoading && !isImageError) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }

                if (!isImageError) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Vehicle Model Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentScale = ContentScale.Fit,
                        onLoading = { isImageLoading = true },
                        onSuccess = {
                            isImageLoading = false
                            isImageError = false
                        },
                        onError = {
                            isImageLoading = false
                            isImageError = true
                        }
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(brandLogoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Brand Logo",
                            modifier = Modifier.size(70.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = listOfNotNull(hebrewMake, modelName).joinToString(" "),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 360 Rotation Controls (if image loaded)
                if (!isImageError) {
                    IconButton(
                        onClick = {
                            selectedAngleIndex = (selectedAngleIndex - 1 + angles.size) % angles.size
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Angle right", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            selectedAngleIndex = (selectedAngleIndex + 1) % angles.size
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Angle left", tint = Color.White)
                    }
                }
            }

            if (!isImageError) {
                Spacer(Modifier.height(8.dp))

                // Angle Dot Indicators (360 Carousel)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    angles.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (selectedAngleIndex == index) 9.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedAngleIndex == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                                .clickable { selectedAngleIndex = index }
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "תצוגת 360° • התמונה להמחשה בלבד",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
