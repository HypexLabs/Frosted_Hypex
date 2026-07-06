package com.example.glass

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class GlassPresetType {
    IOS,
    HYPER_OS,
    COLOR_OS,
    CUSTOM
}

data class GlassParameters(
    val presetType: GlassPresetType = GlassPresetType.CUSTOM,
    val blurRadiusDp: Float = 16f,
    val tintColor: Color = Color.White,
    val tintAlpha: Float = 0.15f,
    val borderWidthDp: Float = 1f,
    val borderAlpha: Float = 0.3f,
    val noiseAlpha: Float = 0.04f,
    val cornerRadiusDp: Float = 16f,
    val shadowElevationDp: Float = 8f,
    val specularAlpha: Float = 0.20f,
    val refractionOffset: Float = 0f,
    val refractionHeight: Float = 0f,
    val dispersion: Float = 0f
) {
    fun toKotlinCode(): String {
        val colorHex = "Color(0x${toArgbHex(tintColor)})"
        return """
        FrostedGlassCard(
            modifier = Modifier.padding(16.dp),
            blurRadius = ${blurRadiusDp.toInt()}.dp,
            tintColor = $colorHex,
            tintAlpha = ${String.format("%.2ff", tintAlpha)},
            borderWidth = ${String.format("%.1f.dp", borderWidthDp)},
            borderAlpha = ${String.format("%.2ff", borderAlpha)},
            noiseAlpha = ${String.format("%.2ff", noiseAlpha)},
            cornerRadius = ${cornerRadiusDp.toInt()}.dp,
            shadowElevation = ${shadowElevationDp.toInt()}.dp,
            specularAlpha = ${String.format("%.2ff", specularAlpha)},
            refractionOffset = ${refractionOffset.toInt()}.dp,
            refractionHeight = ${refractionHeight.toInt()}.dp,
            dispersion = ${dispersion.toInt()}.dp
        ) {
            // Add your content here
        }
        """.trimIndent()
    }
}

private fun toArgbHex(color: Color): String {
    val argb = color.toArgb()
    return String.format("%08X", argb)
}

object GlassPresets {
    val iOS = GlassParameters(
        presetType = GlassPresetType.IOS,
        blurRadiusDp = 24f,
        tintColor = Color.White,
        tintAlpha = 0.12f,
        borderWidthDp = 0.5f,
        borderAlpha = 0.25f,
        noiseAlpha = 0.02f,
        cornerRadiusDp = 24f,
        shadowElevationDp = 10f,
        specularAlpha = 0.15f,
        refractionOffset = 0f,
        refractionHeight = 0f,
        dispersion = 0f
    )

    val hyperOS = GlassParameters(
        presetType = GlassPresetType.HYPER_OS,
        blurRadiusDp = 16f,
        tintColor = Color(0xFFE4EDF7),
        tintAlpha = 0.16f,
        borderWidthDp = 1.0f,
        borderAlpha = 0.40f,
        noiseAlpha = 0.05f,
        cornerRadiusDp = 16f,
        shadowElevationDp = 14f,
        specularAlpha = 0.30f,
        refractionOffset = 0f,
        refractionHeight = 0f,
        dispersion = 0f
    )

    val colorOS = GlassParameters(
        presetType = GlassPresetType.COLOR_OS,
        blurRadiusDp = 30f,
        tintColor = Color(0xFFF2F4F7),
        tintAlpha = 0.09f,
        borderWidthDp = 1.5f,
        borderAlpha = 0.18f,
        noiseAlpha = 0.01f,
        cornerRadiusDp = 28f,
        shadowElevationDp = 6f,
        specularAlpha = 0.10f,
        refractionOffset = 0f,
        refractionHeight = 0f,
        dispersion = 0f
    )
}
