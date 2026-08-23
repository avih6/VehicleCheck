package com.avih6.vehiclecheck.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.avih6.vehiclecheck.MainViewModel
import com.avih6.vehiclecheck.R
import com.avih6.vehiclecheck.data.SearchState
import com.avih6.vehiclecheck.data.VehicleUtils
import com.avih6.vehiclecheck.ui.components.CameraScannerDialog
import com.avih6.vehiclecheck.ui.components.NativeAdView
import com.avih6.vehiclecheck.ui.components.ResultCard
import java.util.Locale

@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val query by viewModel.query.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val nativeAd by viewModel.nativeAd.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var showCameraScanner by remember { mutableStateOf(false) }

    // Speech Recognizer Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.searchPlateDirect(spokenText)
            }
        }
    }

    if (showCameraScanner) {
        CameraScannerDialog(
            onDismiss = { showCameraScanner = false },
            onResult = { plate ->
                showCameraScanner = false
                viewModel.searchPlateDirect(plate)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Government Source Info Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "מאגר כלי רכב רשמי - משרד התחבורה",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "בדיקת תוקף טסט, מפרט טכני, רמת בטיחות ובעלות",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Title
        Text(
            text = stringResource(R.string.search_placeholder),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // License Plate Input Field
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            placeholder = { Text("לדוגמה: 12345678") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    viewModel.search()
                }
            ),
            visualTransformation = LicensePlateTransformation(),
            singleLine = true,
            leadingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                    IconButton(onClick = { showCameraScanner = true }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Camera OCR", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "אמור את מספר הרכב")
                        }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {}
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )

        Spacer(Modifier.height(14.dp))

        // Search Button
        Button(
            onClick = {
                keyboardController?.hide()
                viewModel.search()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.search_btn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        // Search State Presentation
        AnimatedContent(
            targetState = searchState,
            label = "SearchStateTransition"
        ) { state ->
            when (state) {
                is SearchState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SearchState.Success -> {
                    LaunchedEffect(state.vehicle) {
                        viewModel.loadNativeAd(context)
                    }
                    val isFav = favorites.any { it.licensePlate == query }
                    Column {
                        nativeAd?.let { ad ->
                            NativeAdView(nativeAd = ad)
                            Spacer(Modifier.height(12.dp))
                        }

                        ResultCard(
                            vehicle = state.vehicle,
                            formattedPlate = state.formattedPlate,
                            testStatus = state.testStatus,
                            hasDisabledPermit = state.hasDisabledPermit,
                            isFavorite = isFav,
                            onToggleFavorite = { viewModel.toggleFavoriteCurrentResult(query, isFav) }
                        )
                    }
                }
                is SearchState.NotFound -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "רכב לא נמצא במאגר",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "מספר הרכב ${VehicleUtils.formatPlate(state.plate)} לא אותר במאגר כלי הרכב הפעילים של משרד התחבורה. ייתכן שמדובר ברכב שהורד מהכביש או רכב זר.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is SearchState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(12.dp))
                            Text(text = state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                is SearchState.Idle -> {
                    // Quick Tips Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "מה ניתן לבדוק?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            BulletPoint("תוקף טסט שנתי וכמה ימים נותרו")
                            BulletPoint("שנת ייצור ומועד עלייה מדויק לכביש")
                            BulletPoint("סוג בעלות (פרטי / חברה / ליסינג)")
                            BulletPoint("ציון בטיחות רשמי וקבוצת זיהום")
                            BulletPoint("דגם מנוע, מידות צמיגים ומספר שלדה")
                            BulletPoint("בדיקת זכאות לתו נכה פעיל")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

class LicensePlateTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val digits = text.text
        val out = StringBuilder()

        val offsetMapping = when (digits.length) {
            7 -> {
                for (i in digits.indices) {
                    out.append(digits[i])
                    if (i == 1 || i == 4) out.append("-")
                }
                object : androidx.compose.ui.text.input.OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int {
                        if (offset <= 2) return offset
                        if (offset <= 5) return offset + 1
                        return offset + 2
                    }
                    override fun transformedToOriginal(offset: Int): Int {
                        if (offset <= 2) return offset
                        if (offset <= 6) return offset - 1
                        return offset - 2
                    }
                }
            }
            8 -> {
                for (i in digits.indices) {
                    out.append(digits[i])
                    if (i == 2 || i == 4) out.append("-")
                }
                object : androidx.compose.ui.text.input.OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int {
                        if (offset <= 3) return offset
                        if (offset <= 5) return offset + 1
                        return offset + 2
                    }
                    override fun transformedToOriginal(offset: Int): Int {
                        if (offset <= 3) return offset
                        if (offset <= 6) return offset - 1
                        return offset - 2
                    }
                }
            }
            else -> {
                out.append(digits)
                androidx.compose.ui.text.input.OffsetMapping.Identity
            }
        }

        return androidx.compose.ui.text.input.TransformedText(
            androidx.compose.ui.text.AnnotatedString(out.toString()),
            offsetMapping
        )
    }
}