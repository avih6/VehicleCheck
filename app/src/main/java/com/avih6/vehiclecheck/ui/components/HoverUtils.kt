package com.avih6.vehiclecheck.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.border
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

fun Modifier.handCursor(): Modifier {
    return this.pointerHoverIcon(PointerIcon.Hand)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoverTooltipIconButton(
    onClick: () -> Unit,
    tooltipText: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState()
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier.pointerHoverIcon(PointerIcon.Hand),
            enabled = enabled
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoverTooltipBox(
    tooltipText: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState(),
        modifier = modifier
    ) {
        content()
    }
}

fun Modifier.tvFocusable(
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    borderWidth: androidx.compose.ui.unit.Dp = 2.dp,
    focusedBorderColor: androidx.compose.ui.graphics.Color? = null
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val focusColor = focusedBorderColor ?: MaterialTheme.colorScheme.primary
    
    this.onFocusChanged { state ->
        isFocused = state.isFocused
    }.then(
        if (isFocused) {
            Modifier.border(borderWidth, focusColor, shape)
        } else {
            Modifier
        }
    )
}