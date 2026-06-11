package com.example.dosagecalc.presentation.calculator.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dosagecalc.presentation.ui.theme.LocalDosageShapes
import com.example.dosagecalc.presentation.ui.theme.spacing
import java.util.Locale

@Composable
fun PatientInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    sliderValue: Float,
    onSliderChange: (Float) -> Unit,
    sliderRange: ClosedFloatingPointRange<Float>,
    suffix: String,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    imeAction: ImeAction = ImeAction.Next,
    activeColor: Color,
    inactiveColor: Color,
    errorMessage: String?,
    hintMessage: String?,
) {
    val haptic = LocalHapticFeedback.current
    var lastHapticValue by remember { mutableFloatStateOf(sliderValue) }
    var cardWidthPx by remember { mutableIntStateOf(1) }
    var isEditing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val sp = MaterialTheme.spacing
    val shapes = LocalDosageShapes.current

    val fraction =
        (
            (sliderValue - sliderRange.start) /
                (sliderRange.endInclusive - sliderRange.start)
        ).coerceIn(0f, 1f)

    val displayText =
        value.toFloatOrNull()?.let { v ->
            if (v == kotlin.math.floor(v.toDouble()).toFloat()) v.toLong().toString() else value
        } ?: if (value.isEmpty()) "-" else value

    LaunchedEffect(isEditing) {
        if (isEditing) focusRequester.requestFocus()
    }

    Card(
        shape = shapes.tile,
        colors = CardDefaults.cardColors(containerColor = inactiveColor.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .onSizeChanged { cardWidthPx = it.width.coerceAtLeast(1) }
                .pointerInput(sliderRange) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        if (!isEditing) {
                            val range = sliderRange.endInclusive - sliderRange.start
                            val delta = (dragAmount / cardWidthPx.toFloat()) * range
                            val newVal = (sliderValue + delta).coerceIn(sliderRange)
                            onSliderChange(newVal)
                            val formatted =
                                if (keyboardType == KeyboardType.Number) {
                                    newVal.toInt().toString()
                                } else {
                                    String.format(Locale.US, "%.1f", newVal)
                                }
                            onValueChange(formatted)
                            if (kotlin.math.abs(newVal - lastHapticValue) >= 1f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastHapticValue = newVal
                            }
                        }
                    }
                },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = sp.base, vertical = sp.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = activeColor,
                )
            }

            Spacer(modifier = Modifier.height(sp.sm))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (isEditing) {
                    BasicTextField(
                        value = value,
                        onValueChange = { input ->
                            onValueChange(input)
                            input.toFloatOrNull()?.let { parsed ->
                                if (parsed in sliderRange) {
                                    onSliderChange(parsed)
                                }
                            }
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = keyboardType,
                                imeAction = imeAction,
                            ),
                        keyboardActions = KeyboardActions(onAny = { isEditing = false }),
                        singleLine = true,
                        cursorBrush = SolidColor(activeColor),
                        textStyle =
                            MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                    )
                } else {
                    Text(
                        text = displayText,
                        style =
                            MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                            ),
                        color =
                            if (errorMessage != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { isEditing = true },
                    )
                }
            }

            Text(
                text = suffix,
                style = MaterialTheme.typography.labelMedium,
                color = activeColor,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(sp.md))

            Canvas(modifier = Modifier.fillMaxWidth().height(5.dp)) {
                val w = size.width
                val r = 2.5.dp.toPx()
                val thumbR = 5.dp.toPx()
                val thumbX = (thumbR + fraction * (w - thumbR * 2f)).coerceIn(thumbR, w - thumbR)

                drawRoundRect(
                    color = inactiveColor.copy(alpha = 0.35f),
                    cornerRadius = CornerRadius(r),
                )
                if (fraction > 0f) {
                    drawRoundRect(
                        color = activeColor.copy(alpha = 0.55f),
                        size = size.copy(width = thumbX),
                        cornerRadius = CornerRadius(r),
                    )
                }
                drawCircle(
                    color = activeColor,
                    radius = thumbR,
                    center = Offset(thumbX, size.height / 2f),
                )
            }
        }
    }

    val helperText = errorMessage ?: hintMessage
    if (helperText != null) {
        Text(
            text = helperText,
            color =
                if (errorMessage != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = sp.xs, top = 2.dp),
        )
    }
}
