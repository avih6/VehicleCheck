package com.avih6.vehiclecheck

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import com.google.firebase.perf.FirebasePerformance
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avih6.vehiclecheck.data.VehicleUtils
import com.avih6.vehiclecheck.ui.components.AdBanner
import com.avih6.vehiclecheck.ui.components.MultiplePlatesDialog
import com.avih6.vehiclecheck.ui.components.RatingDialog
import com.avih6.vehiclecheck.ui.components.ThemeSettingsDialog
import com.avih6.vehiclecheck.ui.components.handCursor
import com.avih6.vehiclecheck.ui.screens.HistoryScreen
import com.avih6.vehiclecheck.ui.screens.InfoScreen
import com.avih6.vehiclecheck.ui.screens.SearchScreen
import com.avih6.vehiclecheck.ui.theme.VehicleCheckTheme
import com.google.android.gms.ads.MobileAds
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        handleSearchIntent(intent)
        handleSendImageIntent(intent)

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
            MobileAds.initialize(this@MainActivity) {}
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColors by viewModel.dynamicColors.collectAsState()

            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            VehicleCheckTheme(darkTheme = isDark, dynamicColor = dynamicColors) {
                MainAppShell(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSearchIntent(intent)
        handleSendImageIntent(intent)
    }

    private fun handleSearchIntent(intent: Intent?) {
        val plate = intent?.getStringExtra("plate")
            ?: intent?.getStringExtra("search_plate")
            ?: intent?.data?.getQueryParameter("plate")
        if (!plate.isNullOrBlank()) {
            val clean = plate.filter { it.isDigit() }
            if (clean.length in 5..8) {
                viewModel.searchPlateDirect(clean)
            }
        }
    }

    private fun handleSendImageIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val type = intent.type ?: return
        if (!type.startsWith("image/")) return

        val imageUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } ?: intent.data

        if (imageUri != null) {
            processSharedImage(imageUri)
        }
    }

    private fun processSharedImage(uri: Uri) {
        viewModel.logEvent("share_to_app_received")
        val ocrTrace = FirebasePerformance.getInstance().newTrace("share_to_app_ocr_latency")
        ocrTrace.start()
        Toast.makeText(this, R.string.ocr_processing_image, Toast.LENGTH_SHORT).show()
        val textRecognizer = try {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        } catch (t: Throwable) {
            ocrTrace.putAttribute("status", "init_failed")
            ocrTrace.stop()
            viewModel.recordException(t)
            viewModel.logEvent("share_to_app_error", Bundle().apply {
                putString("error", t.message ?: "Init failed")
            })
            Log.e("MainActivity", "Failed to initialize TextRecognizer", t)
            Toast.makeText(this, R.string.ocr_init_failed, Toast.LENGTH_SHORT).show()
            return
        }

        val inputImage = try {
            InputImage.fromFilePath(this, uri)
        } catch (e: Exception) {
            ocrTrace.putAttribute("status", "load_image_failed")
            ocrTrace.stop()
            viewModel.recordException(e)
            viewModel.logEvent("share_to_app_error", Bundle().apply {
                putString("error", e.message ?: "Load image failed")
            })
            Log.e("MainActivity", "Failed to load InputImage from URI: $uri", e)
            Toast.makeText(this, R.string.ocr_no_plate_found, Toast.LENGTH_SHORT).show()
            return
        }

        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val detected = VehicleUtils.extractLicensePlateCandidates(visionText.text)
                ocrTrace.putAttribute("status", "success")
                ocrTrace.putAttribute("candidates_count", detected.size.toString())
                ocrTrace.stop()
                viewModel.logEvent("share_to_app_ocr_success", Bundle().apply {
                    putInt("candidates_count", detected.size)
                })

                if (detected.isEmpty()) {
                    viewModel.logEvent("share_to_app_no_plate_found")
                    Toast.makeText(this, R.string.ocr_no_plate_found, Toast.LENGTH_LONG).show()
                } else if (detected.size == 1) {
                    val plate = detected.first()
                    viewModel.logEvent("share_to_app_auto_search", Bundle().apply {
                        putString("plate_length", plate.length.toString())
                    })
                    Toast.makeText(
                        this,
                        getString(R.string.ocr_plate_detected_searching, VehicleUtils.formatPlate(plate)),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.setSelectedTab(0)
                    viewModel.searchPlateDirect(plate)
                } else {
                    viewModel.logEvent("share_to_app_multiple_plates_shown", Bundle().apply {
                        putInt("candidates_count", detected.size)
                    })
                    viewModel.setSelectedTab(0)
                    viewModel.setCandidatePlates(detected)
                }
            }
            .addOnFailureListener { e ->
                ocrTrace.putAttribute("status", "ocr_failed")
                ocrTrace.stop()
                viewModel.recordException(e)
                viewModel.logEvent("share_to_app_error", Bundle().apply {
                    putString("error", e.message ?: "OCR failed")
                })
                Log.e("MainActivity", "Text recognition error on shared image", e)
                Toast.makeText(this, R.string.ocr_no_plate_found, Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                try { textRecognizer.close() } catch (_: Throwable) {}
            }
    }

    override fun recreate() {
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(android.app.Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
            overrideActivityTransition(android.app.Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
        super.recreate()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(viewModel: MainViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    val candidatePlates by viewModel.candidatePlates.collectAsState()
    var showRatingDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    candidatePlates?.let { plates ->
        MultiplePlatesDialog(
            plates = plates,
            onPlateSelected = { plate ->
                viewModel.selectCandidatePlate(plate)
            },
            onDismiss = {
                viewModel.setCandidatePlates(null)
            }
        )
    }

    val privacyPolicyUrl = "https://avih6.github.io/vehicle-check/privacy-policy"
    val termsUrl = "https://avih6.github.io/vehicle-check/terms-of-service"

    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onRateSelected = { stars ->
                viewModel.logEvent("rate_stars_selected", Bundle().apply {
                    putInt("stars", stars)
                })
                if (stars >= 4) {
                    viewModel.logEvent("rate_playstore_opened")
                    openPlayStore(context)
                }
            },
            onFeedbackAccepted = {
                viewModel.logEvent("rate_feedback_accepted")
                sendEmail(context)
            },
            onCancelled = {
                viewModel.logEvent("rate_cancelled")
            }
        )
    }

    if (showSettingsDialog) {
        ThemeSettingsDialog(
            onDismiss = { showSettingsDialog = false },
            viewModel = viewModel
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                DrawerHeader()
                Spacer(Modifier.height(16.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.tab_services_drawer), fontWeight = FontWeight.Bold) },
                    selected = selectedTab == 6,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.logEvent("drawer_services_clicked")
                        viewModel.setSelectedTab(6)
                    },
                    icon = { Icon(Icons.Default.Storefront, null, tint = MaterialTheme.colorScheme.primary) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_settings)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.logEvent("drawer_settings_clicked")
                        showSettingsDialog = true
                    },
                    icon = { Icon(Icons.Default.Settings, null) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_disabled_permit), fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.logEvent("drawer_disabled_permit_clicked")
                        com.avih6.vehiclecheck.util.ExternalAppUtils.openDisabledPermitApp(context, source = "drawer")
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Accessible, null, tint = MaterialTheme.colorScheme.primary) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_share)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.logEvent("drawer_share_clicked")
                        shareApp(context)
                    },
                    icon = { Icon(Icons.Default.Share, null) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_rate)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.logEvent("drawer_rate_clicked")
                        showRatingDialog = true
                    },
                    icon = { Icon(Icons.Default.Star, null) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_contact)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.logEvent("drawer_contact_clicked")
                        sendEmail(context)
                    },
                    icon = { Icon(Icons.Default.Email, null) }
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_privacy)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.logEvent("drawer_privacy_clicked")
                        launchCustomTab(context, privacyPolicyUrl)
                    },
                    icon = { Icon(Icons.Default.PrivacyTip, null) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_terms)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.logEvent("drawer_terms_clicked")
                        launchCustomTab(context, termsUrl)
                    },
                    icon = { Icon(Icons.Default.Description, null) }
                )

                Text(
                    text = stringResource(R.string.version_format, BuildConfig.VERSION_NAME),
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                0 -> stringResource(R.string.search_title)
                                1 -> stringResource(R.string.history_title)
                                2 -> stringResource(R.string.title_statistics)
                                3 -> stringResource(R.string.title_recalls)
                                4 -> stringResource(R.string.title_dtc)
                                5 -> stringResource(R.string.title_gallery)
                                6 -> stringResource(R.string.tab_services_drawer)
                                else -> stringResource(R.string.search_title)
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        )
                    },
                    navigationIcon = {
                        com.avih6.vehiclecheck.ui.components.HoverTooltipIconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            tooltipText = stringResource(R.string.btn_menu)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.btn_menu))
                        }
                    },
                    actions = {
                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.btn_menu))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_disabled_permit), fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Accessible,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.logEvent("menu_disabled_permit_clicked")
                                    com.avih6.vehiclecheck.util.ExternalAppUtils.openDisabledPermitApp(context, source = "top_overflow_menu")
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_settings)) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.logEvent("menu_settings_clicked")
                                    showSettingsDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_share)) },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.logEvent("menu_share_clicked")
                                    shareApp(context)
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                Column {
                    AdBanner(
                        modifier = Modifier.fillMaxWidth(),
                        isScreenshotMode = viewModel.isScreenshotMode
                    )

                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.setSelectedTab(0) },
                            icon = {
                                Icon(
                                    if (selectedTab == 0) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = stringResource(R.string.tab_search)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_search), fontSize = 10.sp) },
                            modifier = Modifier.handCursor()
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.setSelectedTab(1) },
                            icon = {
                                Icon(
                                    if (selectedTab == 1) Icons.Filled.History else Icons.Outlined.History,
                                    contentDescription = stringResource(R.string.tab_history)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_history), fontSize = 10.sp) },
                            modifier = Modifier.handCursor()
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.setSelectedTab(2) },
                            icon = {
                                Icon(
                                    if (selectedTab == 2) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                    contentDescription = stringResource(R.string.tab_statistics_short)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_statistics_short), fontSize = 10.sp) },
                            modifier = Modifier.handCursor()
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { viewModel.setSelectedTab(3) },
                            icon = {
                                Icon(
                                    if (selectedTab == 3) Icons.Filled.Warning else Icons.Outlined.WarningAmber,
                                    contentDescription = stringResource(R.string.tab_recalls_short)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_recalls_short), fontSize = 10.sp) },
                            modifier = Modifier.handCursor()
                        )
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { viewModel.setSelectedTab(4) },
                            icon = {
                                Icon(
                                    if (selectedTab == 4) Icons.Filled.Build else Icons.Outlined.Build,
                                    contentDescription = stringResource(R.string.tab_dtc_short)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_dtc_short), fontSize = 10.sp) },
                            modifier = Modifier.handCursor()
                        )
                        NavigationBarItem(
                            selected = selectedTab == 5,
                            onClick = { viewModel.setSelectedTab(5) },
                            icon = {
                                Icon(
                                    if (selectedTab == 5) Icons.Filled.Collections else Icons.Outlined.Collections,
                                    contentDescription = stringResource(R.string.tab_gallery_short)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_gallery_short), fontSize = 10.sp) },
                            modifier = Modifier.handCursor()
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                androidx.activity.compose.BackHandler(enabled = selectedTab == 6) {
                    viewModel.setSelectedTab(0)
                }

                when (selectedTab) {
                    0 -> SearchScreen(
                        viewModel = viewModel,
                        onNavigateToServices = { viewModel.setSelectedTab(6) },
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> HistoryScreen(
                        viewModel = viewModel,
                        onSelectVehicle = { plate, isEngineering ->
                            viewModel.searchPlateDirect(plate, isEngineering)
                            viewModel.setSelectedTab(0)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> com.avih6.vehiclecheck.ui.screens.StatisticsScreen(
                        viewModel = viewModel,
                        onNavigateToGallery = { modelQuery ->
                            viewModel.onQueryChange(modelQuery)
                            viewModel.setSelectedTab(5)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    3 -> com.avih6.vehiclecheck.ui.screens.RecallsScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                    4 -> com.avih6.vehiclecheck.ui.screens.DtcScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                    5 -> {
                        val currentSearchState = viewModel.searchState.collectAsState().value
                        val initialQuery = if (currentSearchState is com.avih6.vehiclecheck.data.SearchState.Success) {
                            val v = currentSearchState.vehicle
                            val (makeEn, modelEn) = com.avih6.vehiclecheck.data.VehicleUtils.getEnglishMakeAndModel(v.make, v.model)
                            "$makeEn $modelEn"
                        } else "הכל"

                        com.avih6.vehiclecheck.ui.screens.GalleryScreen(
                            initialQuery = initialQuery,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    6 -> com.avih6.vehiclecheck.ui.screens.ServicesScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                shadowElevation = 4.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.app_name),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp
            )
        }
    }
}

private fun shareApp(context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_text))
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, null))
}

private fun openPlayStore(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: Exception) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
    }
}

private fun launchCustomTab(context: Context, url: String) {
    if (!url.startsWith("https://") && !url.startsWith("http://")) return
    try {
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        intent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        } catch (_: Exception) {}
    }
}

private fun sendEmail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("av6development@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.contact_subject))
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {}
}
