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

    var dropdownExpanded by remember { mutableStateOf(false) }

    val options = listOf(
        Pair("system", "לפי הגדרות המערכת"),
        Pair("light", "מצב בהיר"),
        Pair("dark", "מצב כהה")
    )

    val currentTitle = options.firstOrNull { it.first == themeMode }?.second ?: "לפי הגדרות המערכת"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "הגדרות תצוגה",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Theme Dropdown Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ערכת נושא:",
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
                                text = "צבעים דינמיים (Material You)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "מתאים את צבעי האפליקציה אוטומטית לרקע המכשיר שלך",
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
                Text("סגור", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}