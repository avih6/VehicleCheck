package com.avih6.vehiclecheck.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.avih6.vehiclecheck.R

@Composable
fun LicensePlateInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onVoiceClick: () -> Unit,
    onCameraClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() }.take(8)
            onValueChange(digits)
        },
        modifier = modifier.fillMaxWidth(),
        label = { Text("מספר רכב (5 עד 8 ספרות)") },
        placeholder = { Text("00-000-00") },
        leadingIcon = {
            Icon(Icons.Default.DirectionsCar, contentDescription = null)
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            Icons.Default.Clear, 
                            contentDescription = "נקה",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                IconButton(onClick = onVoiceClick) {
                    Icon(
                        Icons.Default.Mic, 
                        contentDescription = "חיפוש קולי"
                    )
                }
                IconButton(onClick = onCameraClick) {
                    Icon(
                        Icons.Default.CameraAlt, 
                        contentDescription = "סריקת מצלמה"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        visualTransformation = LicensePlateTransformation(),
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

class LicensePlateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val out = StringBuilder()

        val offsetMapping = when (digits.length) {
            5 -> {
                for (i in digits.indices) {
                    out.append(digits[i])
                    if (i == 1) out.append("-")
                }
                object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int {
                        if (offset <= 2) return offset
                        return offset + 1
                    }
                    override fun transformedToOriginal(offset: Int): Int {
                        if (offset <= 2) return offset
                        return offset - 1
                    }
                }
            }
            6 -> {
                for (i in digits.indices) {
                    out.append(digits[i])
                    if (i == 2) out.append("-")
                }
                object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int {
                        if (offset <= 3) return offset
                        return offset + 1
                    }
                    override fun transformedToOriginal(offset: Int): Int {
                        if (offset <= 3) return offset
                        return offset - 1
                    }
                }
            }
            7 -> {
                for (i in digits.indices) {
                    out.append(digits[i])
                    if (i == 1 || i == 4) out.append("-")
                }
                object : OffsetMapping {
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
                object : OffsetMapping {
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
                OffsetMapping.Identity
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}