package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AspectRatioType {
    SHORT,   // e.g. 16:9 (1080x1920) or older devices
    REGULAR, // e.g. 18:9 to 19.5:9 (1080x2160, 390x844)
    TALL     // e.g. 20:9 or taller (1080x2400, 1440x3200, 412x915)
}

object ResponsiveLayout {
    @Composable
    fun screenWidthDp(): Dp = LocalConfiguration.current.screenWidthDp.dp

    @Composable
    fun screenHeightDp(): Dp = LocalConfiguration.current.screenHeightDp.dp

    @Composable
    fun getAspectRatioType(): AspectRatioType {
        val width = LocalConfiguration.current.screenWidthDp.toFloat()
        val height = LocalConfiguration.current.screenHeightDp.toFloat()
        val ratio = if (width > 0) height / width else 1.77f

        return when {
            ratio < 1.85f -> AspectRatioType.SHORT
            ratio in 1.85f..2.15f -> AspectRatioType.REGULAR
            else -> AspectRatioType.TALL
        }
    }

    @Composable
    fun horizontalPadding(): Dp {
        val width = LocalConfiguration.current.screenWidthDp
        return when {
            width < 360 -> 12.dp
            width in 360..412 -> 16.dp
            else -> 20.dp
        }
    }

    @Composable
    fun responsiveMapHeight(): Dp {
        val height = LocalConfiguration.current.screenHeightDp
        return (height * 0.36f).coerceIn(260f, 360f).dp
    }

    @Composable
    fun isSmallWidth(): Boolean = LocalConfiguration.current.screenWidthDp < 360
}
