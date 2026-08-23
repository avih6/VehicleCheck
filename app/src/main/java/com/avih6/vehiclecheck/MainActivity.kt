package com.avih6.vehiclecheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avih6.vehiclecheck.ui.components.AdBanner
import com.avih6.vehiclecheck.ui.screens.HistoryScreen
import com.avih6.vehiclecheck.ui.screens.InfoScreen
import com.avih6.vehiclecheck.ui.screens.SearchScreen
import com.avih6.vehiclecheck.ui.theme.VehicleCheckTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) {}

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            val isDark = isSystemInDarkTheme()

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

            VehicleCheckTheme(darkTheme = isDark) {
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = when (selectedTab) {
                                        0 -> stringResource(R.string.search_title)
                                        1 -> stringResource(R.string.history_title)
                                        else -> stringResource(R.string.tab_info)
                                    }
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> SearchScreen(viewModel = viewModel)
                            1 -> HistoryScreen(
                                viewModel = viewModel,
                                onSelectVehicle = { plate ->
                                    selectedTab = 0
                                    viewModel.searchPlateDirect(plate)
                                }
                            )
                            2 -> InfoScreen()
                        }
                    }
                }
            }
        }
    }
}