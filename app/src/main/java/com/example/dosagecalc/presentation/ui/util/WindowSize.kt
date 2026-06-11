package com.example.dosagecalc.presentation.ui.util

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalWindowSizeClass =
    compositionLocalOf<WindowSizeClass> {
        error("WindowSizeClass not provided - wrap root content with CompositionLocalProvider")
    }

@Composable
fun isCompactHeight(): Boolean = LocalWindowSizeClass.current.heightSizeClass == WindowHeightSizeClass.Compact

@Composable
fun isMediumOrExpandedWidth(): Boolean = LocalWindowSizeClass.current.widthSizeClass != WindowWidthSizeClass.Compact

@Composable
fun isExpandedWidth(): Boolean = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded

// no-op on phones; caps width for tablets so content doesn't stretch edge-to-edge
fun Modifier.responsiveContentWidth(maxWidth: Dp = 640.dp): Modifier = this.widthIn(max = maxWidth)
