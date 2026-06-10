@file:Suppress("MatchingDeclarationName")
package com.example.dosagecalc.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dosagecalc.presentation.ui.theme.LocalDosageShapes
import com.example.dosagecalc.presentation.ui.theme.LocalElevation

enum class CardTone { Primary, Secondary, Tertiary, Error }

@Composable
fun ExpressiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    asymmetric: Boolean = true,
    mirrored: Boolean = false,
    containerAlpha: Float = 1f,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shapes = LocalDosageShapes.current
    val elevation = LocalElevation.current
    val shape =
        when {
            !asymmetric -> shapes.card
            mirrored -> shapes.expressiveMirror
            else -> shapes.expressive
        }
    val colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = containerAlpha),
        )
    val elev = CardDefaults.cardElevation(defaultElevation = elevation.level0)
    if (onClick != null) {
        Card(
            modifier = modifier.clip(shape).clickable { onClick() },
            shape = shape,
            colors = colors,
            elevation = elev,
            content = content,
        )
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, elevation = elev, content = content)
    }
}

@Composable
fun ElevatedSurfaceCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = LocalDosageShapes.current.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        content = content,
    )
}

@Composable
fun OutlinedTintCard(
    modifier: Modifier = Modifier,
    tone: CardTone = CardTone.Primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val toneColor =
        when (tone) {
            CardTone.Primary -> cs.primary
            CardTone.Secondary -> cs.secondary
            CardTone.Tertiary -> cs.tertiary
            CardTone.Error -> cs.error
        }
    Card(
        modifier = modifier,
        shape = LocalDosageShapes.current.cardLarge,
        colors = CardDefaults.cardColors(containerColor = toneColor.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, toneColor.copy(alpha = 0.3f)),
        content = content,
    )
}
