package com.example.glass

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A beautiful, functional Glassmorphic Music Player card.
 * Displays a custom animated album art, track details, progress slider, and playback controls.
 */
@Composable
fun GlassMusicPlayer(
    params: GlassParameters,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0.42f) }

    // Pulsing animation for the album art when playing
    val infiniteTransition = rememberInfiniteTransition(label = "albumPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("showroom_music_player"),
        blurRadius = params.blurRadiusDp.dp,
        tintColor = params.tintColor,
        tintAlpha = params.tintAlpha,
        borderWidth = params.borderWidthDp.dp,
        borderAlpha = params.borderAlpha,
        noiseAlpha = params.noiseAlpha,
        cornerRadius = params.cornerRadiusDp.dp,
        shadowElevation = params.shadowElevationDp.dp,
        specularAlpha = params.specularAlpha,
        refractionOffset = params.refractionOffset.dp,
        refractionHeight = params.refractionHeight.dp,
        dispersion = params.dispersion.dp
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = "Music icon",
                    tint = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                )
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Custom Styled Album Art
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF416C),
                                Color(0xFF8A2387),
                                Color(0xFFE94057),
                                Color(0xFFF27121)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl Disk look
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Song Info
            Text(
                text = "Starlight Echoes",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Solaris Flux • Retrograde",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Frosted Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                        thumbColor = Color.White
                    ),
                    modifier = Modifier.testTag("music_progress_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "1:42",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.5f))
                    )
                    Text(
                        text = "3:58",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Controls with subtle hover-ready layouts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Primary Play circular button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable { isPlaying = !isPlaying }
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        .testTag("music_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * A sleek Smart Home dashboard panel with a dynamic temperature circle indicator
 * and frosted interactive switch components.
 */
@Composable
fun GlassSmartHome(
    params: GlassParameters,
    modifier: Modifier = Modifier
) {
    var acTemp by remember { mutableFloatStateOf(21.5f) }
    var lightsOn by remember { mutableStateOf(true) }
    var purifierOn by remember { mutableStateOf(false) }
    var securityArmed by remember { mutableStateOf(true) }

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("showroom_smart_home"),
        blurRadius = params.blurRadiusDp.dp,
        tintColor = params.tintColor,
        tintAlpha = params.tintAlpha,
        borderWidth = params.borderWidthDp.dp,
        borderAlpha = params.borderAlpha,
        noiseAlpha = params.noiseAlpha,
        cornerRadius = params.cornerRadiusDp.dp,
        shadowElevation = params.shadowElevationDp.dp,
        specularAlpha = params.specularAlpha,
        refractionOffset = params.refractionOffset.dp,
        refractionHeight = params.refractionHeight.dp,
        dispersion = params.dispersion.dp
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVING ROOM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "Climate & System",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "Home Icon",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Display: Large Dial and Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Dynamic Temp Indicator
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF00C6FF),
                                    Color(0x0000C6FF),
                                    Color(0xFFFF5F6D),
                                    Color(0xFF00C6FF)
                                )
                            )
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1f°", acTemp),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 28.sp
                            )
                        )
                        Text(
                            text = "AIR CON",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                // Quick Controls
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (acTemp > 16f) acTemp -= 0.5f },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Rounded.Remove, "Cool", tint = Color.White)
                        }

                        IconButton(
                            onClick = { if (acTemp < 30f) acTemp += 0.5f },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Rounded.Add, "Warm", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Status: Optimal",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF00FFCC),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Frosted Interactive Badges / Switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Light Button
                GlassSwitch(
                    icon = Icons.Rounded.Lightbulb,
                    label = "Lights",
                    isActive = lightsOn,
                    activeColor = Color(0xFFFFD54F),
                    onClick = { lightsOn = !lightsOn },
                    modifier = Modifier.weight(1f)
                )

                // Purifier Button
                GlassSwitch(
                    icon = Icons.Rounded.Air,
                    label = "Purifier",
                    isActive = purifierOn,
                    activeColor = Color(0xFF81C784),
                    onClick = { purifierOn = !purifierOn },
                    modifier = Modifier.weight(1f)
                )

                // Security Button
                GlassSwitch(
                    icon = if (securityArmed) Icons.Rounded.Shield else Icons.Rounded.ShieldMoon,
                    label = "Security",
                    isActive = securityArmed,
                    activeColor = Color(0xFF64B5F6),
                    onClick = { securityArmed = !securityArmed },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * A beautiful custom Toggle Switch card with glass body and active glowing color.
 */
@Composable
fun GlassSwitch(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) activeColor.copy(alpha = 0.25f)
                else Color.White.copy(alpha = 0.08f)
            )
            .border(
                1.dp,
                if (isActive) activeColor.copy(alpha = 0.45f)
                else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * An interactive Creator Profile card showcasing the glass highlight edges.
 */
@Composable
fun GlassProfileCard(
    params: GlassParameters,
    modifier: Modifier = Modifier
) {
    var followed by remember { mutableStateOf(false) }

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("showroom_profile_card"),
        blurRadius = params.blurRadiusDp.dp,
        tintColor = params.tintColor,
        tintAlpha = params.tintAlpha,
        borderWidth = params.borderWidthDp.dp,
        borderAlpha = params.borderAlpha,
        noiseAlpha = params.noiseAlpha,
        cornerRadius = params.cornerRadiusDp.dp,
        shadowElevation = params.shadowElevationDp.dp,
        specularAlpha = params.specularAlpha,
        refractionOffset = params.refractionOffset.dp,
        refractionHeight = params.refractionHeight.dp,
        dispersion = params.dispersion.dp
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Layout with custom Avatar and Badge
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                // Main Avatar circle with glow outline
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF00FF87),
                                    Color(0xFF60EFFF),
                                    Color(0xFF0061FF)
                                )
                            )
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Avatar Placeholder",
                        tint = Color.White,
                        modifier = Modifier.size(45.dp)
                    )
                }
                // Online Indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676))
                        .border(3.dp, Color.Black.copy(alpha = 0.4f), CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Info
            Text(
                text = "Elena Vance",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "@elenadesign",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Glass Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlassBadge(text = "Visual Pro")
                GlassBadge(text = "Xiaomi Fan")
                GlassBadge(text = "Android UI")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Follow Action button
            Button(
                onClick = { followed = !followed },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (followed) Color.White.copy(alpha = 0.2f) else Color.White,
                    contentColor = if (followed) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_follow_button")
            ) {
                Text(
                    text = if (followed) "✓ FOLLOWING" else "FOLLOW CREATOR",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

/**
 * A beautiful, light glass tag/badge component.
 */
@Composable
fun GlassBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp
            )
        )
    }
}

/**
 * An alert/notification hub component.
 */
@Composable
fun GlassAlertNotification(
    params: GlassParameters,
    modifier: Modifier = Modifier,
    onAction: () -> Unit = {}
) {
    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("showroom_alert"),
        blurRadius = params.blurRadiusDp.dp,
        tintColor = params.tintColor,
        tintAlpha = params.tintAlpha,
        borderWidth = params.borderWidthDp.dp,
        borderAlpha = params.borderAlpha,
        noiseAlpha = params.noiseAlpha,
        cornerRadius = params.cornerRadiusDp.dp,
        shadowElevation = params.shadowElevationDp.dp,
        specularAlpha = params.specularAlpha,
        refractionOffset = params.refractionOffset.dp,
        refractionHeight = params.refractionHeight.dp,
        dispersion = params.dispersion.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFFFF3B30).copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "Alert icon",
                        tint = Color(0xFFFF453A),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "System Alert",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "High thermal warning emitted from emulator background engine.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISMISS",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable { onAction() }
                )
                Text(
                    text = "COOL DOWN",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E676),
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier
                        .clickable { onAction() }
                )
            }
        }
    }
}
