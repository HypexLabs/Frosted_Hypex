package com.example.glass

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * State holding the background painter and size/position metrics to coordinate
 * pixel-aligned refraction blurs inside nested FrostedGlassCards.
 */
class GlassBackgroundState {
    var painter: Painter? by mutableStateOf(null)
    var size: IntSize by mutableStateOf(IntSize.Zero)
    var positionInWindow: Offset by mutableStateOf(Offset.Zero)
}

val LocalGlassBackgroundState = compositionLocalOf { GlassBackgroundState() }

/**
 * Main container that establishes a context for glass refraction.
 * All nested [FrostedGlassCard]s will read from this container to align their blurs.
 */
@Composable
fun GlassBackground(
    painter: Painter,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { GlassBackgroundState() }
    state.painter = painter

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                state.size = coordinates.size
                state.positionInWindow = coordinates.positionInWindow()
            }
            .drawBehind {
                // Draw the actual crisp background image
                with(painter) {
                    draw(size = Size(size.width, size.height))
                }
            }
    ) {
        CompositionLocalProvider(LocalGlassBackgroundState provides state) {
            content()
        }
    }
}

/**
 * A highly customizable Frosted Glass Card that renders a realistic glassmorphic effect.
 * It uses a layered structure: a blurred mirrored background, an overlay tint,
 * a stable sand-blasted noise overlay, and high-precision glossy specular edges.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 16.dp,
    tintColor: Color = Color.White,
    tintAlpha: Float = 0.15f,
    borderWidth: Dp = 1.dp,
    borderAlpha: Float = 0.3f,
    noiseAlpha: Float = 0.04f,
    cornerRadius: Dp = 16.dp,
    shadowElevation: Dp = 8.dp,
    specularAlpha: Float = 0.20f,
    refractionOffset: Dp = 0.dp,
    refractionHeight: Dp = 0.dp,
    dispersion: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundState = LocalGlassBackgroundState.current
    var cardOffset by remember { mutableStateOf(Offset.Zero) }
    var bgSize by remember { mutableStateOf(IntSize.Zero) }

    val shape = RoundedCornerShape(cornerRadius)

    // Build.VERSION_CODES.S is Android 12 (API 31).
    // On devices with API >= 31, we can use real-time hardware blur.
    val supportsHardwareBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val bgPos = backgroundState.positionInWindow
                val cardPos = coordinates.positionInWindow()
                cardOffset = cardPos - bgPos
                bgSize = backgroundState.size
            }
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
    ) {
        // LAYER 1: The Blurred Glass Body
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .drawBehind {
                    // Draw a solid tint base to ensure there's a backdrop
                    drawRect(color = tintColor.copy(alpha = if (supportsHardwareBlur) tintAlpha else tintAlpha + 0.15f))

                    // If background is available, draw the aligned slice
                    val painter = backgroundState.painter
                    if (painter != null && bgSize.width > 0 && bgSize.height > 0) {
                        val rx = refractionOffset.toPx()
                        val ry = refractionHeight.toPx()
                        val disp = dispersion.toPx()

                        if (disp > 0f) {
                            // Dispersion Offset 1 (Red/Magenta chromatic shift)
                            translate(left = -cardOffset.x + rx - disp, top = -cardOffset.y + ry) {
                                with(painter) {
                                    draw(
                                        size = Size(bgSize.width.toFloat(), bgSize.height.toFloat()),
                                        colorFilter = ColorFilter.tint(Color(0xFFFF3366).copy(alpha = 0.35f), BlendMode.Screen)
                                    )
                                }
                            }
                            // Dispersion Offset 2 (Cyan/Blue chromatic shift)
                            translate(left = -cardOffset.x + rx + disp, top = -cardOffset.y + ry) {
                                with(painter) {
                                    draw(
                                        size = Size(bgSize.width.toFloat(), bgSize.height.toFloat()),
                                        colorFilter = ColorFilter.tint(Color(0xFF33CCFF).copy(alpha = 0.35f), BlendMode.Screen)
                                    )
                                }
                            }
                            // Main aligned core background slice
                            translate(left = -cardOffset.x + rx, top = -cardOffset.y + ry) {
                                with(painter) {
                                    draw(
                                        size = Size(bgSize.width.toFloat(), bgSize.height.toFloat()),
                                        alpha = 0.7f
                                    )
                                }
                            }
                        } else {
                            // Standard single draw with refraction translation
                            translate(left = -cardOffset.x + rx, top = -cardOffset.y + ry) {
                                with(painter) {
                                    draw(size = Size(bgSize.width.toFloat(), bgSize.height.toFloat()))
                                }
                            }
                        }
                    }
                }
                // Apply hardware blur exclusively to the background layer on supported OS versions
                .then(
                    if (supportsHardwareBlur && blurRadius > 0.dp) {
                        Modifier.blur(blurRadius)
                    } else {
                        Modifier
                    }
                )
        )

        // LAYER 2: Glass Surface Highlights, Frosting Noise, and Custom Edge Shimmers
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .drawBehind {
                    // Draw a subtle translucent surface tint to fuse the colors together
                    drawRect(color = tintColor.copy(alpha = tintAlpha * 0.4f))

                    // Draw the satin specular highlight reflecting light diagonally
                    if (specularAlpha > 0f) {
                        val specularBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = specularAlpha),
                                Color.White.copy(alpha = specularAlpha * 0.4f),
                                Color.Transparent,
                                Color.White.copy(alpha = specularAlpha * 0.1f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                        drawRect(brush = specularBrush)
                    }

                    // Render custom sand-blasted noise/grain to mimic frosting
                    if (noiseAlpha > 0f) {
                        drawFrostedNoise(noiseAlpha)
                    }
                }
                .border(
                    width = borderWidth,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = borderAlpha),
                            Color.White.copy(alpha = borderAlpha * 0.3f),
                            Color.Transparent,
                            Color.White.copy(alpha = borderAlpha * 0.15f),
                            Color.White.copy(alpha = borderAlpha * 0.55f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(100f, 300f) // Soft diagonal angle for edge refraction
                    ),
                    shape = shape
                )
        )

        // LAYER 3: Sharp child content (NOT blurred, kept perfectly readable)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .padding(borderWidth) // Avoid overlapping the fine glass edge
        ) {
            content()
        }
    }
}

/**
 * Draws a static, premium noise/grain texture on the canvas.
 * Uses a stable seed to prevent any frame-by-frame flicker or performance drag.
 */
private fun DrawScope.drawFrostedNoise(alpha: Float) {
    val random = java.util.Random(1337) // Stable seed to avoid noise flickering
    val step = 3 // Checkered spatial resolution
    val w = size.width.toInt()
    val h = size.height.toInt()

    // Draw localized micro-particles to simulate high-end frosting
    val particleCount = (w * h / 120).coerceIn(200, 3000)
    for (i in 0 until particleCount) {
        val rx = random.nextFloat() * size.width
        val ry = random.nextFloat() * size.height
        val sizeVal = random.nextFloat() * 1.5f + 0.5f
        val particleAlpha = random.nextFloat() * alpha * 0.6f
        
        drawRect(
            color = Color.White.copy(alpha = particleAlpha),
            topLeft = Offset(rx, ry),
            size = Size(sizeVal, sizeVal)
        )
    }
}
