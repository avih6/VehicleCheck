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
import androidx.compose.foundation.shape.CircleShape
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
import com.avih6.vehiclecheck.ui.components.HoverTooltipIconButton
import com.avih6.vehiclecheck.ui.components.handCursor
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
    var selectedFilterYear by remember { mutableStateOf("הכל") }
    var selectedFilterCategory by remember { mutableStateOf("הכל") }

    val filterYears = remember { 
        listOf("הכל", "2026", "2025", "2024", "2023", "2022", "2021", "2020", "2019", "2018", "2017", "2016", "2015", "2010-2014", "לפני 2010") 
    }
    val filterCategories = remember { 
        listOf("הכל", "בלמים", "כריות אוויר", "היגוי", "דלק", "מנוע", "חשמל", "מתלים", "חגורות", "תוכנה") 
    }

    fun loadRecalls() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val resp = withContext(Dispatchers.IO) {
                    NetworkClient.apiService.getAllRecalls(limit = 5000, sort = "_id desc")
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

            val yr = item.recallYear ?: 0
            val matchesYear = when (selectedFilterYear) {
                "הכל" -> true
                "2010-2014" -> yr in 2010..2014
                "לפני 2010" -> yr in 1..2009
                else -> item.recallYear?.toString() == selectedFilterYear
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
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Compact Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("חיפוש יצרן, דגם, תקלה או מספר ריקול") },
            placeholder = { Text("למשל: FORD, TOYOTA, בלמים, כריות אוויר...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    HoverTooltipIconButton(
                        onClick = { searchQuery = "" },
                        tooltipText = "נקה חיפוש"
                    ) {
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

        // Combined Filter Chips (Years & Categories)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(filterYears) { yr ->
                FilterChip(
                    selected = selectedFilterYear == yr,
                    onClick = { selectedFilterYear = yr },
                    label = { Text(if (yr == "הכל") "כל השנים" else yr, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.handCursor()
                )
            }
            item {
                VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))
            }
            items(filterCategories.drop(1)) { cat ->
                FilterChip(
                    selected = selectedFilterCategory == cat,
                    onClick = { selectedFilterCategory = if (selectedFilterCategory == cat) "הכל" else cat },
                    label = { Text(cat, fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.handCursor()
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Content
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("טוען קריאות שירות רשמיות...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { loadRecalls() }, modifier = Modifier.handCursor()) {
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
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "לא נמצאו קריאות שירות התואמות לחיפוש.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (filteredRecalls.size == allRecalls.size) "סה\"כ ${allRecalls.size} קריאות חוזרות (כל המאגר)" else "מוצגות ${filteredRecalls.size} מתוך ${allRecalls.size} קריאות",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "מקור: משרד התחבורה",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.5.dp, Color(0xFFEF5350).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Top Badges (Recall ID + Year)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFD32F2F),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "קריאת שירות #${item.recallId ?: ""}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }

                item.recallYear?.let { yr ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "שנת $yr",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Brand Section: Large Emblem + Make + Big Models text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(54.dp).padding(2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(6.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(brandLogoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.makeName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.makeName ?: "יצרן רכב",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "דגמים: ${item.model ?: "כל הדגמים"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Production Dates Range
            if (!item.buildStart.isNullOrBlank() || !item.buildEnd.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "תאריכי ייצור מושפעים: ${item.buildStart ?: ""} עד ${item.buildEnd ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Fault category pill
            item.faultType?.let { fType ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "מכלול: $fType",
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            // Fault Description (Large, clear, comfortable line height)
            Text(
                text = "תיאור התקלה:",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.faultDescription ?: "לא צוין תיאור לתקלה זו.",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Repair Method Container
            item.repairMethod?.let { repair ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("אופן הטיפול הנדרש:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1B5E20))
                            Text(repair, fontSize = 13.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Importer Contact Bar
            if (!item.telephone.isNullOrBlank() || !item.website.isNullOrBlank() || !item.importerName.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "יבואן רשמי:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.importerName ?: "שירות היבואן",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            item.telephone?.let { phone ->
                                HoverTooltipIconButton(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        try { context.startActivity(dialIntent) } catch (e: Exception) {}
                                    },
                                    tooltipText = "חייג ליבואן ($phone)",
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Phone,
                                                contentDescription = "Call",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            item.website?.let { site ->
                                val fullUrl = if (!site.startsWith("http://") && !site.startsWith("https://")) "https://$site" else site
                                HoverTooltipIconButton(
                                    onClick = {
                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                                        try { context.startActivity(webIntent) } catch (e: Exception) {}
                                    },
                                    tooltipText = "פתח אתר יבואן",
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Language,
                                                contentDescription = "Website",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(18.dp)
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
    }
}