package com.avih6.vehiclecheck.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avih6.vehiclecheck.MainViewModel
import com.avih6.vehiclecheck.R
import com.avih6.vehiclecheck.data.VehicleHistoryEntity
import com.avih6.vehiclecheck.data.VehicleUtils
import com.avih6.vehiclecheck.ui.theme.*
import com.avih6.vehiclecheck.ui.components.tvFocusable

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onSelectVehicle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val history by viewModel.searchHistory.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    var showOnlyFavorites by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val displayedList = if (showOnlyFavorites) favorites else history

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.history_clear)) },
            text = { Text(stringResource(R.string.history_clear_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    viewModel.clearAllHistory()
                }) {
                    Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showOnlyFavorites,
                    onClick = { showOnlyFavorites = false },
                    label = { Text("הכל (${history.size})") }
                )
                FilterChip(
                    selected = showOnlyFavorites,
                    onClick = { showOnlyFavorites = true },
                    label = { Text("מועדפים (${favorites.size})") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            if (history.isNotEmpty() && !showOnlyFavorites) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        Icons.Outlined.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (displayedList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (showOnlyFavorites) Icons.Outlined.StarBorder else Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (showOnlyFavorites) "אין רכבים שמורים במועדפים" else stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedList, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        onClick = { onSelectVehicle(item.licensePlate) },
                        onToggleFavorite = { viewModel.toggleFavorite(item.id, item.isFavorite) },
                        onDelete = { viewModel.deleteHistoryEntry(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: VehicleHistoryEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .tvFocusable(shape = RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator Dot
            val isNotFound = item.make == "לא אותר במאגר"
            val statusColor = if (isNotFound) {
                Color(0xFFFF9800)
            } else if (item.isTestValid) {
                if (item.daysUntilTest <= 30) TestExpiringSoonAmber else TestValidGreen
            } else {
                TestExpiredRed
            }

            Surface(
                modifier = Modifier.size(10.dp),
                shape = CircleShape,
                color = statusColor
            ) {}

            Spacer(Modifier.width(12.dp))

            // Plate & Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = VehicleUtils.formatPlate(item.licensePlate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        item.year?.let { y ->
                            Text(
                                text = " • $y",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isNotFound) {
                        val classification = VehicleUtils.resolveQuickClassification(item.make, item.model, fuel = item.fuelType)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = classification,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                val desc = listOfNotNull(item.make, item.model, item.fuelType).filter { it.isNotBlank() }.joinToString(" • ")
                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Test Status summary / Retry status
                val testText = when {
                    isNotFound -> "לא אותר במאגר • לחץ לבדיקה חוזרת 🔄"
                    item.isTestValid -> {
                        val diff = VehicleUtils.calculateDateDifferenceHebrew(item.testExpiryDate)
                        if (diff != null) "טסט בתוקף • $diff" else "טסט בתוקף (עוד ${item.daysUntilTest} ימים)"
                    }
                    item.daysUntilTest < 0 -> {
                        val diff = VehicleUtils.calculateDateDifferenceHebrew(item.testExpiryDate)
                        if (diff != null) "טסט לא בתוקף • $diff" else "טסט לא בתוקף (איחור)"
                    }
                    else -> "רכב לא פעיל / טסט לא בתוקף"
                }
                Text(
                    text = testText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Favorite button
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (item.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}