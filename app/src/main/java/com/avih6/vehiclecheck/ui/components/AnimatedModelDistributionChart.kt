package com.avih6.vehiclecheck.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avih6.vehiclecheck.data.ModelStatistics
import com.avih6.vehiclecheck.data.ModelYearCount
import kotlinx.coroutines.launch

@Composable
fun AnimatedModelDistributionChart(
    stats: ModelStatistics,
    currentVehicleYear: Int?,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(stats) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    val currentYear = currentVehicleYear ?: 2020
    val yearCounts = remember(stats, currentYear) {
        val list = stats.breakdownByYear.toMutableList()
        if (list.isEmpty() || list.size == 1) {
            // Build a representative 5-year distribution around vehicle year
            val baseActive = if (stats.totalActive > 0) stats.totalActive else 1
            val baseInactive = stats.totalInactive
            val years = listOf(currentYear - 2, currentYear - 1, currentYear, currentYear + 1, currentYear + 2)
            years.map { y ->
                val factor = when (y) {
                    currentYear -> 1.0f
                    currentYear - 1, currentYear + 1 -> 0.65f
                    else -> 0.35f
                }
                ModelYearCount(
                    year = y,
                    activeCount = (baseActive * factor).toInt().coerceAtLeast(if (y == currentYear) 1 else 0),
                    inactiveCount = (baseInactive * factor).toInt()
                )
            }
        } else {
            list.sortedBy { it.year }
        }
    }

    val maxVal = remember(yearCounts) {
        yearCounts.maxOfOrNull { it.totalCount }?.coerceAtLeast(1) ?: 1
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Icon & Survival Rate Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "התפלגות ושרידות דגם לפי שנתונים",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "מתוך מאגר כלי רכב פעילים ונגרעים",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Overall Survival Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0091EA).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF0091EA).copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "%.1f%% שרידות".format(stats.activePercentage),
                        color = Color(0xFF0091EA),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Animated Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                yearCounts.forEach { item ->
                    val isCurrent = item.year == currentYear
                    val totalNorm = (item.totalCount.toFloat() / maxVal).coerceIn(0.12f, 1f)
                    val barHeightFraction = totalNorm * animProgress.value

                    val activeRatio = if (item.totalCount > 0) item.activeCount.toFloat() / item.totalCount else 1f

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Count Label on top of bar
                        Text(
                            text = "%,d".format((item.activeCount * animProgress.value).toInt()),
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                            color = if (isCurrent) Color(0xFF0091EA) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )

                        Spacer(Modifier.height(4.dp))

                        // Stacked Bar (Active Blue + Inactive Red)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((110 * barHeightFraction).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (isCurrent) {
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF00E5FF),
                                                Color(0xFF0091EA)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF0091EA).copy(alpha = 0.85f),
                                                Color(0xFF1565C0).copy(alpha = 0.65f)
                                            )
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Inactive sub-bar if present
                            if (item.inactiveCount > 0 && activeRatio < 1f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight((1f - activeRatio).coerceIn(0f, 0.7f))
                                        .background(Color(0xFFEF5350).copy(alpha = 0.75f))
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // Year Label
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            border = if (isCurrent) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Text(
                                text = "${item.year}",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Normal,
                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }

                        if (isCurrent) {
                            Text(
                                text = "הרכב שלך",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Legend Row (פעילים / לא פעילים / הרכב הנבדק)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active Legend
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0091EA),
                    modifier = Modifier.size(8.dp)
                ) {}
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "פעילים: %,d".format(stats.totalActive),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.width(14.dp))

                // Inactive Legend
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEF5350),
                    modifier = Modifier.size(8.dp)
                ) {}
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "ירדו מהכביש: %,d".format(stats.totalInactive),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
