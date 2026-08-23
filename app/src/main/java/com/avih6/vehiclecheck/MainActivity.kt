package com.avih6.vehiclecheck

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.avih6.vehiclecheck.ui.components.AdBanner
import com.avih6.vehiclecheck.ui.components.RatingDialog
import com.avih6.vehiclecheck.ui.components.ThemeSettingsDialog
import com.avih6.vehiclecheck.ui.screens.HistoryScreen
import com.avih6.vehiclecheck.ui.screens.InfoScreen
import com.avih6.vehiclecheck.ui.screens.SearchScreen
import com.avih6.vehiclecheck.ui.theme.VehicleCheckTheme
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) {}

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(viewModel: MainViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    val privacyPolicyUrl = "https://avih6.github.io/vehicle-check/privacy-policy"
    val termsUrl = "https://avih6.github.io/vehicle-check/terms-of-service"

    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onRateSelected = { stars ->
                if (stars >= 4) {
                    openPlayStore(context)
                }
            },
            onFeedbackAccepted = {
                sendEmail(context)
            },
            onCancelled = {}
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
                    label = { Text(stringResource(R.string.menu_settings)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSettingsDialog = true
                    },
                    icon = { Icon(Icons.Default.Settings, null) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_share)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        shareApp(context)
                    },
                    icon = { Icon(Icons.Default.Share, null) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_rate)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showRatingDialog = true
                    },
                    icon = { Icon(Icons.Default.Star, null) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_contact)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
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
                        launchCustomTab(context, privacyPolicyUrl)
                    },
                    icon = { Icon(Icons.Default.PrivacyTip, null) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_terms)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
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
                                else -> stringResource(R.string.tab_info)
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_settings)) },
                                onClick = {
                                    showOptionsMenu = false
                                    showSettingsDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_share)) },
                                onClick = {
                                    showOptionsMenu = false
                                    shareApp(context)
                                },
                                leadingIcon = { Icon(Icons.Default.Share, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_rate)) },
                                onClick = {
                                    showOptionsMenu = false
                                    showRatingDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Star, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_contact)) },
                                onClick = {
                                    showOptionsMenu = false
                                    sendEmail(context)
                                },
                                leadingIcon = { Icon(Icons.Default.Email, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_privacy)) },
                                onClick = {
                                    showOptionsMenu = false
                                    launchCustomTab(context, privacyPolicyUrl)
                                },
                                leadingIcon = { Icon(Icons.Default.PrivacyTip, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_terms)) },
                                onClick = {
                                    showOptionsMenu = false
                                    launchCustomTab(context, termsUrl)
                                },
                                leadingIcon = { Icon(Icons.Default.Description, null) }
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
                    AdBanner(modifier = Modifier.fillMaxWidth().height(50.dp))

                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    if (selectedTab == 0) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = stringResource(R.string.tab_search)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_search)) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    if (selectedTab == 1) Icons.Filled.History else Icons.Outlined.History,
                                    contentDescription = stringResource(R.string.tab_history)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_history)) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    if (selectedTab == 2) Icons.Filled.Info else Icons.Outlined.Info,
                                    contentDescription = stringResource(R.string.tab_info)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_info)) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> SearchScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> HistoryScreen(
                        viewModel = viewModel,
                        onSelectVehicle = { plate ->
                            viewModel.searchPlateDirect(plate)
                            selectedTab = 0
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> InfoScreen(modifier = Modifier.fillMaxSize())
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
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
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
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
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
    try {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
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
