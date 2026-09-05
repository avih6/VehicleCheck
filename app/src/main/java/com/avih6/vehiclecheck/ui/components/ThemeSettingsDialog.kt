package com.avih6.vehiclecheck.ui.components

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avih6.vehiclecheck.MainViewModel
import com.avih6.vehiclecheck.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColors by viewModel.dynamicColors.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var dropdownExpanded by remember { mutableStateOf(false) }

    val options = listOf(
        Pair("system", stringResource(R.string.theme_system)),
        Pair("light", stringResource(R.string.theme_light)),
        Pair("dark", stringResource(R.string.theme_dark))
    )

    val currentTitle = options.firstOrNull { it.first == themeMode }?.second ?: stringResource(R.string.theme_system)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_title),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Theme Dropdown Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_theme_label),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentTitle,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (themeMode) {
                                        "light" -> Icons.Default.LightMode
                                        "dark" -> Icons.Default.DarkMode
                                        else -> Icons.Default.SettingsBrightness
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            options.forEach { (mode, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontWeight = if (themeMode == mode) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        viewModel.setThemeMode(mode)
                                        dropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (mode) {
                                                "light" -> Icons.Default.LightMode
                                                "dark" -> Icons.Default.DarkMode
                                                else -> Icons.Default.SettingsBrightness
                                            },
                                            contentDescription = null,
                                            tint = if (themeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Language Selector
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                val localeOptions = listOf(
                    Pair("", stringResource(R.string.lang_system)),
                    Pair("he", stringResource(R.string.lang_he)),
                    Pair("en", stringResource(R.string.lang_en))
                )

                val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
                val currentLocaleTag = if (currentLocales.isEmpty) "" else currentLocales[0]?.language ?: ""
                var langDropdownExpanded by remember { mutableStateOf(false) }
                val currentLangLabel = localeOptions.firstOrNull { it.first == currentLocaleTag }?.second
                    ?: stringResource(R.string.lang_system)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_language_label),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = langDropdownExpanded,
                        onExpandedChange = { langDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentLangLabel,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = langDropdownExpanded)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = langDropdownExpanded,
                            onDismissRequest = { langDropdownExpanded = false }
                        ) {
                            localeOptions.forEach { (tag, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            label,
                                            fontWeight = if (currentLocaleTag == tag) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        langDropdownExpanded = false
                                        val localeList = if (tag.isEmpty()) {
                                            androidx.core.os.LocaleListCompat.getEmptyLocaleList()
                                        } else {
                                            androidx.core.os.LocaleListCompat.forLanguageTags(tag)
                                        }
                                        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(localeList)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
                                            val appLocales = if (tag.isEmpty()) {
                                                android.os.LocaleList.getEmptyLocaleList()
                                            } else {
                                                android.os.LocaleList.forLanguageTags(tag)
                                            }
                                            localeManager?.applicationLocales = appLocales
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Material You Dynamic Colors (Android 12+ / API 31+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_dynamic_colors),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = stringResource(R.string.settings_dynamic_colors_desc),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = dynamicColors,
                            onCheckedChange = { viewModel.setDynamicColors(it) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}