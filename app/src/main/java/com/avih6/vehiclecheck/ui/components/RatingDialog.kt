package com.avih6.vehiclecheck.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.avih6.vehiclecheck.R

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onRateSelected: (Int) -> Unit,
    onFeedbackAccepted: () -> Unit,
    onCancelled: (String) -> Unit
) {
    var selectedStars by remember { mutableStateOf(0) }
    var showFeedbackStep by remember { mutableStateOf(false) }

    if (!showFeedbackStep) {
        AlertDialog(
            onDismissRequest = { 
                onCancelled("star_picker")
                onDismiss() 
            },
            title = { Text(stringResource(R.string.rate_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.rate_message))
                    Spacer(Modifier.height(16.dp))
                    Row {
                        (1..5).forEach { index ->
                            Icon(
                                imageVector = if (selectedStars >= index) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (selectedStars >= index) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { selectedStars = index }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedStars > 0) {
                            onRateSelected(selectedStars)
                            if (selectedStars <= 3) {
                                showFeedbackStep = true
                            } else {
                                onDismiss()
                            }
                        }
                    },
                    enabled = selectedStars > 0
                ) {
                    Text(stringResource(R.string.btn_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    onCancelled("star_picker_button")
                    onDismiss() 
                }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = { 
                onCancelled("feedback_prompt")
                onDismiss() 
            },
            title = { Text(stringResource(R.string.feedback_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.feedback_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onFeedbackAccepted()
                    onDismiss()
                }) {
                    Text(stringResource(R.string.btn_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onCancelled("feedback_declined")
                    onDismiss()
                }) {
                    Text(stringResource(R.string.btn_no))
                }
            }
        )
    }
}