package com.avih6.vehiclecheck.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avih6.vehiclecheck.data.NetworkClient
import com.avih6.vehiclecheck.data.RecallDetailRecord
import com.avih6.vehiclecheck.data.VehicleUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RecallsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var allRecalls by remember { mutableStateOf<List<RecallDetailRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterYear by remember { mutableStateOf<String>("הכל") }
    var selectedFilterCategory by remember { mutableStateOf<String>("הכל") }

    val filterYears = remember { listOf("הכל", "2026", "2025", "2024", "2023") }
    val filterCategories = remember { listOf("הכל", "בלמים", "כריות אוויר", "היגוי", "דלק", "מנוע", "חשמל") }

    fun loadRecalls() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val resp = withContext(Dispatchers.IO) {
                    NetworkClient.apiService.getAllRecalls(limit = 120, sort = "_id desc")
                }
                allRecalls = resp.result?.records ?: emptyList()
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "לא ניתן לטעון את מאגר הריקולים כעת. בדוק את החיבור לרשת."
            }
        }
    }

    LaunchedEffect(Unit) {
        loadRecalls()
    }

    val filteredRecalls = remember(allRecalls, searchQuery, selectedFilterYear, selectedFilterCategory) {
        allRecalls.filter { item ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                (item.makeName?.lowercase()?.contains(q) == true) ||
                (item.model?.lowercase()?.contains(q) == true) ||
                (item.faultDescription?.lowercase()?.contains(q) == true) ||
                (item.faultType?.lowercase()?.contains(q) == true) ||
                (item.importerName?.lowercase()?.contains(q) == true) ||
                (item.recallId?.toString()?.contains(q) == true)
            }

            val matchesYear = if (selectedFilterYear == "הכל") true else {
                item.recallYear?.toString() == selectedFilterYear
            }

            val matchesCategory = if (selectedFilterCategory == "הכל") true else {
                item.faultType?.contains(selectedFilterCategory) == true ||
                item.faultDescription?.contains(selectedFilterCategory) == true
            }

            matchesQuery && matchesYear && matchesCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Government Source Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
            border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "מאגר קריאות חוזרות (ריקולים) רשמי",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    Text(
                        text = "מידע רשמי ומעודכן בזמן אמת ממשרד התחבורה ויבואני הרכב",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF37474F)
                    )
                }
            }
        }

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("חיפוש לפי יצרן, דגם, סוג תקלה או מספר ריקול") },
            placeholder = { Text("למשל: TOYOTA, FORD, כריות אוויר, בלמים...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(8.dp))

        // Year Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterYears) { yr ->
                FilterChip(
                    selected = selectedFilterYear == yr,
                    onClick = { selectedFilterYear = yr },
                    label = { Text(yr, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Category Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterCategories) { cat ->
                FilterChip(
                    selected = selectedFilterCategory == cat,
                    onClick = { selectedFilterCategory = cat },
                    label = { Text(cat, fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Content
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { loadRecalls() }) {
                        Text("נסה שוב")
                    }
                }
            }
        } else if (filteredRecalls.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "לא נמצאו קריאות חוזרות התואמות לחיפוש.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = "נמצאו ${filteredRecalls.size} ריקולים (מסודרים לפי החדשים ביותר):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRecalls, key = { it.id ?: it.recallId ?: it.hashCode().toLong() }) { item ->
                    RecallFeedCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun RecallFeedCard(item: RecallDetailRecord) {
    val context = LocalContext.current
    val brandLogoUrl = VehicleUtils.getBrandLogoUrl(item.makeName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Badge + Recall ID & Year
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFD32F2F),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "קריאת שירות #${item.recallId ?: ""}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                item.recallYear?.let { yr ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "שנת $yr",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Brand Logo + Make + Models
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(brandLogoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.makeName,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.makeName ?: "יצרן רכב",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "דגמים: ${item.model ?: "כל הדגמים"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Production Dates
            if (!item.buildStart.isNullOrBlank() || !item.buildEnd.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "תאריכי ייצור מושפעים: ${item.buildStart ?: ""} עד ${item.buildEnd ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Fault category pill
            item.faultType?.let { fType ->
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "מכלול: $fType",
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // Fault description
            Text(
                text = "תיאור התקלה:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = item.faultDescription ?: "לא צוין תיאור",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Repair Method
            item.repairMethod?.let { repair ->
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("אופן הטיפול: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(repair, style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                }
            }

            // Importer Contact Actions
            if (!item.telephone.isNullOrBlank() || !item.website.isNullOrBlank() || !item.importerName.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "יבואן: ${item.importerName ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    Row {
                        item.telephone?.let { phone ->
                            FilledTonalIconButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    try { context.startActivity(dialIntent) } catch (e: Exception) {}
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(Modifier.width(6.dp))

                        item.website?.let { site ->
                            val fullUrl = if (!site.startsWith("http://") && !site.startsWith("https://")) "https://$site" else site
                            FilledTonalIconButton(
                                onClick = {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                                    try { context.startActivity(webIntent) } catch (e: Exception) {}
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "Website", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}