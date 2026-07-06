package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glass.*
import com.example.ui.theme.MyApplicationTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFFF3EDF7) // Soft Lavender backdrop matching Clean Minimalism
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF3EDF7))
                            .drawBehind {
                                // Render dynamic organic mesh blobs for glass refraction contrast
                                val canvasWidth = this.size.width
                                val canvasHeight = this.size.height
                                drawCircle(
                                    color = Color(0xFFD0BCFF).copy(alpha = 0.45f),
                                    radius = canvasWidth * 0.6f,
                                    center = Offset(canvasWidth * (-0.1f), canvasHeight * 0.05f)
                                )
                                drawCircle(
                                    color = Color(0xFFB69DF8).copy(alpha = 0.4f),
                                    radius = canvasWidth * 0.7f,
                                    center = Offset(canvasWidth * 1.1f, canvasHeight * 0.4f)
                                )
                                drawCircle(
                                    color = Color(0xFF7C4DFF).copy(alpha = 0.15f),
                                    radius = canvasWidth * 0.5f,
                                    center = Offset(canvasWidth * 0.8f, canvasHeight * 0.15f)
                                )
                            }
                    ) {
                        GlassmorphicPlaygroundScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

enum class SandboxBackground {
    VIBRANT_FLUID,
    SUNSET_RADIANCY,
    NEO_CYBER,
    DEEP_SPACE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassmorphicPlaygroundScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Active Glass Parameters State
    var activePreset by remember { mutableStateOf(GlassPresetType.IOS) }
    var blurRadius by remember { mutableFloatStateOf(24f) }
    var tintColor by remember { mutableStateOf(Color.White) }
    var tintAlpha by remember { mutableFloatStateOf(0.12f) }
    var borderWidth by remember { mutableFloatStateOf(0.5f) }
    var borderAlpha by remember { mutableFloatStateOf(0.25f) }
    var noiseAlpha by remember { mutableFloatStateOf(0.02f) }
    var cornerRadius by remember { mutableFloatStateOf(24f) }
    var shadowElevation by remember { mutableFloatStateOf(10f) }
    var specularAlpha by remember { mutableFloatStateOf(0.15f) }
    
    // New Advanced Parameters: Refraction Offset & Height, Dispersion
    var refractionOffset by remember { mutableFloatStateOf(0f) }
    var refractionHeight by remember { mutableFloatStateOf(0f) }
    var dispersion by remember { mutableFloatStateOf(0f) }

    // Bottom Sheet Control
    var showFormulaSheet by remember { mutableStateOf(false) }

    // Widgets Testbench Controls
    var testbenchOverlayEnabled by remember { mutableStateOf(true) }
    var testbenchCoverage by remember { mutableFloatStateOf(0.6f) }

    // Synchronize sliders when switching presets
    LaunchedEffect(activePreset) {
        val targetParams = when (activePreset) {
            GlassPresetType.IOS -> GlassPresets.iOS
            GlassPresetType.HYPER_OS -> GlassPresets.hyperOS
            GlassPresetType.COLOR_OS -> GlassPresets.colorOS
            GlassPresetType.CUSTOM -> null
        }
        if (targetParams != null) {
            blurRadius = targetParams.blurRadiusDp
            tintColor = targetParams.tintColor
            tintAlpha = targetParams.tintAlpha
            borderWidth = targetParams.borderWidthDp
            borderAlpha = targetParams.borderAlpha
            noiseAlpha = targetParams.noiseAlpha
            cornerRadius = targetParams.cornerRadiusDp
            shadowElevation = targetParams.shadowElevationDp
            specularAlpha = targetParams.specularAlpha
            refractionOffset = targetParams.refractionOffset
            refractionHeight = targetParams.refractionHeight
            dispersion = targetParams.dispersion
        }
    }

    // Helper function to handle slider edits (auto-switches preset to Custom)
    val onParameterEdit: () -> Unit = {
        if (activePreset != GlassPresetType.CUSTOM) {
            activePreset = GlassPresetType.CUSTOM
        }
    }

    // Interactive Drag Sandbox states
    var bgChoice by remember { mutableStateOf(SandboxBackground.VIBRANT_FLUID) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    // Showcase tab
    var activeShowroomTab by remember { mutableIntStateOf(0) }

    // Packed parameters for child components
    val currentParams = remember(
        activePreset, blurRadius, tintColor, tintAlpha, borderWidth,
        borderAlpha, noiseAlpha, cornerRadius, shadowElevation, specularAlpha,
        refractionOffset, refractionHeight, dispersion
    ) {
        GlassParameters(
            presetType = activePreset,
            blurRadiusDp = blurRadius,
            tintColor = tintColor,
            tintAlpha = tintAlpha,
            borderWidthDp = borderWidth,
            borderAlpha = borderAlpha,
            noiseAlpha = noiseAlpha,
            cornerRadiusDp = cornerRadius,
            shadowElevationDp = shadowElevation,
            specularAlpha = specularAlpha,
            refractionOffset = refractionOffset,
            refractionHeight = refractionHeight,
            dispersion = dispersion
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // 1. BRAND HERO HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .border(2.5.dp, Color(0xFF6750A4), CircleShape)
                    )
                }
                Column {
                    Text(
                        text = "GlassEngine",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF1D1B20),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        )
                    )
                    Text(
                        text = "V2.4 PRO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF6750A4),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            fontSize = 9.sp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Frosted Refraction Lab",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF1D1B20),
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Design, preview, and export high-performance system-level frosted glass components.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF49454F),
                    lineHeight = 20.sp
                )
            )
        }

        // 2. INTERACTIVE SANDBOX ARENA (Fixed Height)
        Text(
            text = "INTERACTIVE SANDBOX",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF49454F),
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .testTag("sandbox_arena")
        ) {
            // Apply background choice inside sandbox
            val bgPainter = painterResource(id = R.drawable.img_glass_background_1783329071386)

            @Composable
            fun SandboxContainer(content: @Composable BoxScope.() -> Unit) {
                when (bgChoice) {
                    SandboxBackground.VIBRANT_FLUID -> {
                        GlassBackground(painter = bgPainter, content = content)
                    }
                    SandboxBackground.SUNSET_RADIANCY -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF5F6D),
                                            Color(0xFFFFC371),
                                            Color(0xFF2F0743)
                                        ),
                                        radius = 800f
                                    )
                                ),
                            content = content
                        )
                    }
                    SandboxBackground.NEO_CYBER -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0F2027),
                                            Color(0xFF203A43),
                                            Color(0xFF2C5364)
                                        )
                                    )
                                ),
                            content = content
                        )
                    }
                    SandboxBackground.DEEP_SPACE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF1F1C2C),
                                            Color(0xFF928DAB),
                                            Color(0xFF0F172A)
                                        ),
                                        radius = 600f
                                    )
                                ),
                            content = content
                        )
                    }
                }
            }

            SandboxContainer {
                // Background Selector Overlay (Top Left)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SandboxBgPill(
                        label = "Fluid",
                        selected = bgChoice == SandboxBackground.VIBRANT_FLUID,
                        onClick = { bgChoice = SandboxBackground.VIBRANT_FLUID }
                    )
                    SandboxBgPill(
                        label = "Sunset",
                        selected = bgChoice == SandboxBackground.SUNSET_RADIANCY,
                        onClick = { bgChoice = SandboxBackground.SUNSET_RADIANCY }
                    )
                    SandboxBgPill(
                        label = "Cyber",
                        selected = bgChoice == SandboxBackground.NEO_CYBER,
                        onClick = { bgChoice = SandboxBackground.NEO_CYBER }
                    )
                    SandboxBgPill(
                        label = "Space",
                        selected = bgChoice == SandboxBackground.DEEP_SPACE,
                        onClick = { bgChoice = SandboxBackground.DEEP_SPACE }
                    )
                }

                // Drag indicator / coordinates overlay (Bottom Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Offset: X:${dragOffsetX.toInt()} Y:${dragOffsetY.toInt()} • Drag Lens to Test Refraction",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.sp,
                            color = Color(0xFF00FFC4),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // THE INTERACTIVE DRAGGABLE GLASS CARD
                FrostedGlassCard(
                    modifier = Modifier
                        .size(160.dp, 100.dp)
                        .offset { IntOffset(dragOffsetX.roundToInt(), dragOffsetY.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                dragOffsetX = (dragOffsetX + dragAmount.x).coerceIn(-100f, 300f)
                                dragOffsetY = (dragOffsetY + dragAmount.y).coerceIn(-40f, 400f)
                            }
                        }
                        .testTag("draggable_glass_lens"),
                    blurRadius = currentParams.blurRadiusDp.dp,
                    tintColor = currentParams.tintColor,
                    tintAlpha = currentParams.tintAlpha,
                    borderWidth = currentParams.borderWidthDp.dp,
                    borderAlpha = currentParams.borderAlpha,
                    noiseAlpha = currentParams.noiseAlpha,
                    cornerRadius = currentParams.cornerRadiusDp.dp,
                    shadowElevation = currentParams.shadowElevationDp.dp,
                    specularAlpha = currentParams.specularAlpha,
                    refractionOffset = currentParams.refractionOffset.dp,
                    refractionHeight = currentParams.refractionHeight.dp,
                    dispersion = currentParams.dispersion.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Glass Lens",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.DragIndicator,
                                contentDescription = "Drag",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = when (activePreset) {
                                GlassPresetType.IOS -> "iOS Satin Effect"
                                GlassPresetType.HYPER_OS -> "HyperOS High-Gloss"
                                GlassPresetType.COLOR_OS -> "ColorOS Soft Acrylic"
                                GlassPresetType.CUSTOM -> "Custom Formula"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. PRESET SYSTEM SELECTOR
        Text(
            text = "SELECT OPERATING SYSTEM PRESET",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF49454F),
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetButton(
                title = "iOS",
                subtitle = "Satin Glass",
                active = activePreset == GlassPresetType.IOS,
                onClick = { activePreset = GlassPresetType.IOS },
                modifier = Modifier.weight(1f)
            )
            PresetButton(
                title = "HyperOS",
                subtitle = "Tech Glossy",
                active = activePreset == GlassPresetType.HYPER_OS,
                onClick = { activePreset = GlassPresetType.HYPER_OS },
                modifier = Modifier.weight(1f)
            )
            PresetButton(
                title = "ColorOS",
                subtitle = "Warm Acrylic",
                active = activePreset == GlassPresetType.COLOR_OS,
                onClick = { activePreset = GlassPresetType.COLOR_OS },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset philosophy bio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.5f))
                .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            val (title, desc) = when (activePreset) {
                GlassPresetType.IOS -> Pair(
                    "iOS Satin Glassmorphism",
                    "A pure, ultra-clean aesthetic based on Apple's premium satin finish. Uses a high-intensity blur (24dp) combined with light white tints, ultra-thin border highlights, and a micro-fine frosting texture to maximize legibility and visual luxury."
                )
                GlassPresetType.HYPER_OS -> Pair(
                    "Xiaomi HyperOS Glass",
                    "A technological, glossy, high-contrast look engineered for modern performance dashboards. Features sharp double-bevel border highlights, increased noise grain texture, and vibrant diagonal specular reflections that react powerfully to movement."
                )
                GlassPresetType.COLOR_OS -> Pair(
                    "Oppo ColorOS Acrylic",
                    "An organic, matte, silky acrylic feel emphasizing depth and environmental warmth. Leverages a ultra-deep blur (30dp) with lower tint opacity to blend seamlessly with surrounding elements, backed by a soft ambient cast shadow."
                )
                GlassPresetType.CUSTOM -> Pair(
                    "Custom Refraction Formula",
                    "You are now manually blending your own glass compound! Fine-tune the density, tint, refraction limits, and light catching parameters below to create a unique visual asset."
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF49454F),
                        lineHeight = 16.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. BOTTOM SHEET CONFIGURATOR TRIGGER
        Button(
            onClick = { showFormulaSheet = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6750A4)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
                .testTag("open_formula_sheet_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = "Edit Formula",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EDIT GLASS FORMULA SHEET",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        // 4B. RECOMPACTED BOTTOM SHEET
        if (showFormulaSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFormulaSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                containerColor = Color(0xFFF7F2FA),
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = "Formula Editor",
                                tint = Color(0xFF6750A4),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Formula Editor",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20)
                                )
                            )
                        }
                        IconButton(
                            onClick = { showFormulaSheet = false },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFF49454F)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE7E0EC))

                    // Group 1: Core Physics
                    Text(
                        text = "CORE PHYSICS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp
                        )
                    )

                    GlassSlider(
                        label = "Blur Intensity",
                        value = blurRadius,
                        valueRange = 0f..40f,
                        displayValue = "${blurRadius.toInt()} dp",
                        icon = Icons.Rounded.BlurLinear,
                        onValueChange = { blurRadius = it; onParameterEdit() }
                    )

                    GlassSlider(
                        label = "Tint Opacity",
                        value = tintAlpha,
                        valueRange = 0f..0.8f,
                        displayValue = "${(tintAlpha * 100).toInt()}%",
                        icon = Icons.Rounded.Opacity,
                        onValueChange = { tintAlpha = it; onParameterEdit() }
                    )

                    // Color Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tint Color Accent",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val colors = listOf(
                                Color.White,
                                Color(0xFF0F172A),
                                Color(0xFFFFD54F),
                                Color(0xFF2196F3),
                                Color(0xFFFF2E93),
                                Color(0xFF00E676)
                            )
                            colors.forEach { color ->
                                ColorCircle(
                                    color = color,
                                    isSelected = tintColor == color,
                                    onClick = { tintColor = color; onParameterEdit() }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE7E0EC))

                    // Group 2: Advanced Refraction & Dispersion
                    Text(
                        text = "REFRACTION & CHROMATIC DISPERSION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp
                        )
                    )

                    GlassSlider(
                        label = "Refraction X-Offset",
                        value = refractionOffset,
                        valueRange = -30f..30f,
                        displayValue = String.format("%.1f dp", refractionOffset),
                        icon = Icons.Rounded.Compare,
                        onValueChange = { refractionOffset = it; onParameterEdit() }
                    )

                    GlassSlider(
                        label = "Refraction Y-Height",
                        value = refractionHeight,
                        valueRange = -30f..30f,
                        displayValue = String.format("%.1f dp", refractionHeight),
                        icon = Icons.Rounded.UnfoldMore,
                        onValueChange = { refractionHeight = it; onParameterEdit() }
                    )

                    GlassSlider(
                        label = "Chromatic Dispersion",
                        value = dispersion,
                        valueRange = 0f..20f,
                        displayValue = String.format("%.1f dp", dispersion),
                        icon = Icons.Rounded.Gradient,
                        onValueChange = { dispersion = it; onParameterEdit() }
                    )

                    HorizontalDivider(color = Color(0xFFE7E0EC))

                    // Group 3: Glass Edges & Specular
                    Text(
                        text = "SURFACE & SHADOWS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp
                        )
                    )

                    GlassSlider(
                        label = "Border Width",
                        value = borderWidth,
                        valueRange = 0.2f..3.0f,
                        displayValue = String.format("%.1f dp", borderWidth),
                        icon = Icons.Rounded.BorderStyle,
                        onValueChange = { borderWidth = it; onParameterEdit() }
                    )

                    GlassSlider(
                        label = "Border Opacity",
                        value = borderAlpha,
                        valueRange = 0f..0.9f,
                        displayValue = "${(borderAlpha * 100).toInt()}%",
                        icon = Icons.Rounded.BorderOuter,
                        onValueChange = { borderAlpha = it; onParameterEdit() }
                    )

                    GlassSlider(
                        label = "Specular Shine",
                        value = specularAlpha,
                        valueRange = 0f..0.5f,
                        displayValue = "${(specularAlpha * 100).toInt()}%",
                        icon = Icons.Rounded.LightMode,
                        onValueChange = { specularAlpha = it; onParameterEdit() }
                    )

                    GlassSlider(
                        label = "Noise Texture",
                        value = noiseAlpha,
                        valueRange = 0f..0.15f,
                        displayValue = "${(noiseAlpha * 100).toInt()}%",
                        icon = Icons.Rounded.Grain,
                        onValueChange = { noiseAlpha = it; onParameterEdit() }
                    )

                    GlassSlider(
                        label = "Corner Radius",
                        value = cornerRadius,
                        valueRange = 0f..40f,
                        displayValue = "${cornerRadius.toInt()} dp",
                        icon = Icons.Rounded.RoundedCorner,
                        onValueChange = { cornerRadius = it; onParameterEdit() }
                    )

                    GlassSlider(
                        label = "Cast Shadow",
                        value = shadowElevation,
                        valueRange = 0f..24f,
                        displayValue = "${shadowElevation.toInt()} dp",
                        icon = Icons.Rounded.SettingsBrightness,
                        onValueChange = { shadowElevation = it; onParameterEdit() }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4C. BLUR OVER WIDGETS TESTBENCH
        Text(
            text = "BLUR ON WIDGETS TEST (OVERLAY SPEC)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF49454F),
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFEADDFF).copy(alpha = 0.25f))
                .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(24.dp))
                .testTag("widgets_testbench_container")
        ) {
            // Background Widgets: These sit fully underneath
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Native Widgets Underneath",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4)
                        )
                    )
                    
                    // Controller to enable/disable physical overlay
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (testbenchOverlayEnabled) "Glass Sheet: ON" else "Glass Sheet: OFF",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                        Switch(
                            checked = testbenchOverlayEnabled,
                            onCheckedChange = { testbenchOverlayEnabled = it },
                            modifier = Modifier.scale(0.8f).testTag("widgets_overlay_switch")
                        )
                    }
                }

                // Blur intensity/coverage slider for widgets test
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sheet Span:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                    Slider(
                        value = testbenchCoverage,
                        onValueChange = { testbenchCoverage = it },
                        valueRange = 0.1f..1.0f,
                        modifier = Modifier.weight(1f).testTag("testbench_coverage_slider")
                    )
                    Text(
                        text = "${(testbenchCoverage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    )
                }

                // Native Android UI controls that should be blurred *physically on top*
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .padding(14.dp)
                        .then(
                            // Physically blur the layout itself when the glass overlay is on!
                            if (testbenchOverlayEnabled) Modifier.blur((blurRadius * testbenchCoverage).dp)
                            else Modifier
                        )
                ) {
                    Text(
                        text = "This native body text is placed inside the widgets layer. Notice how letters melt under high frosting settings!",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF1D1B20)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    var bgCounter by remember { mutableIntStateOf(0) }
                    Button(
                        onClick = { bgCounter++ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text("Active Under-Glass Clicker: $bgCounter", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    var bgInputText by remember { mutableStateOf("Type here to test readability...") }
                    OutlinedTextField(
                        value = bgInputText,
                        onValueChange = { bgInputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Foreground Physical Glass Sheet Overlay (rendered on top!)
            if (testbenchOverlayEnabled) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(bottom = 4.dp)
                ) {
                    // Custom Glass Overlay Sheet that covers the designated percentage span of the widgets
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(testbenchCoverage)
                            .align(Alignment.TopCenter)
                            .testTag("widgets_overlay_lens"),
                        blurRadius = blurRadius.dp,
                        tintColor = tintColor,
                        tintAlpha = tintAlpha,
                        borderWidth = borderWidth.dp,
                        borderAlpha = borderAlpha,
                        noiseAlpha = noiseAlpha,
                        cornerRadius = 24.dp, // Matches parent container rounding
                        shadowElevation = 0.dp, // Flat flush overlay
                        specularAlpha = specularAlpha,
                        refractionOffset = refractionOffset.dp,
                        refractionHeight = refractionHeight.dp,
                        dispersion = dispersion.dp
                    ) {
                        // Empty inside! This represents a pure physical sheet of glass lying directly ON TOP of the widgets!
                        // This proves the blur is drawn over the widgets, not inside the widgets' background!
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. LIVE EXPORT CODE BOX
        Text(
            text = "LIVE EXPORT CODE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF49454F),
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1D1B20))
                .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(16.dp))
        ) {
            val generatedCode = currentParams.toKotlinCode()

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6750A4))
                        )
                        Text(
                            text = "ComposeKotlinExporter.kt",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.5f),
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(generatedCode))
                            Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = generatedCode,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. SHOWROOM GALLERY (COMPONENTS APPLIED)
        Text(
            text = "LIVE SHOWROOM GALLERY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF49454F),
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
        )

        // Showroom selector pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShowroomTabPill(
                title = "Music",
                active = activeShowroomTab == 0,
                onClick = { activeShowroomTab = 0 },
                modifier = Modifier.weight(1f)
            )
            ShowroomTabPill(
                title = "Smart Home",
                active = activeShowroomTab == 1,
                onClick = { activeShowroomTab = 1 },
                modifier = Modifier.weight(1.1f)
            )
            ShowroomTabPill(
                title = "Profile",
                active = activeShowroomTab == 2,
                onClick = { activeShowroomTab = 2 },
                modifier = Modifier.weight(1f)
            )
            ShowroomTabPill(
                title = "Alert",
                active = activeShowroomTab == 3,
                onClick = { activeShowroomTab = 3 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Render selected showroom component inside a background holder so the glass pops out!
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6750A4), // M3 Primary Deep Purple
                            Color(0xFFD0BCFF), // M3 Light Purple
                            Color(0xFFEADDFF)  // Pale Purple
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(16.dp)
                .testTag("showroom_container")
        ) {
            // Set up a local GlassBackground state for coordinates sync so the showroom card blurs the background correctly!
            val showroomBgPainter = painterResource(id = R.drawable.img_glass_background_1783329071386)
            GlassBackground(
                painter = showroomBgPainter,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                // Dim the image slightly for maximum component readability
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )

                Box(modifier = Modifier.padding(8.dp)) {
                    when (activeShowroomTab) {
                        0 -> GlassMusicPlayer(params = currentParams)
                        1 -> GlassSmartHome(params = currentParams)
                        2 -> GlassProfileCard(params = currentParams)
                        3 -> GlassAlertNotification(params = currentParams)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun SandboxBgPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (selected) Color(0xFF6750A4)
                else Color.White.copy(alpha = 0.4f)
            )
            .border(
                1.dp,
                if (selected) Color(0xFF6750A4)
                else Color(0xFFE7E0EC),
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Color(0xFF49454F)
            )
        )
    }
}

@Composable
fun PresetButton(
    title: String,
    subtitle: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBorderColor by animateColorAsState(
        targetValue = if (active) Color(0xFFD0BCFF) else Color(0xFFE7E0EC),
        label = "borderColor"
    )
    val activeBgColor by animateColorAsState(
        targetValue = if (active) Color(0xFFE8DEF8) else Color.White.copy(alpha = 0.5f),
        label = "bgColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(activeBgColor)
            .border(1.dp, activeBorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag("preset_btn_${title.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (active) Color(0xFF21005D) else Color(0xFF1D1B20)
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = if (active) Color(0xFF6750A4) else Color(0xFF49454F).copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
fun ShowroomTabPill(
    title: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(
                if (active) Color(0xFF6750A4)
                else Color.White.copy(alpha = 0.5f)
            )
            .border(
                1.dp,
                if (active) Color(0xFF6750A4)
                else Color(0xFFE7E0EC),
                CircleShape
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (active) Color.White else Color(0xFF49454F),
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun GlassSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    icon: ImageVector,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF1D1B20),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF6750A4)
                )
            )
        }
        Slider(
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF6750A4),
                activeTrackColor = Color(0xFF6750A4),
                inactiveTrackColor = Color(0xFFE7E0EC)
            ),
            modifier = Modifier.testTag("slider_${label.replace(" ", "_").lowercase()}")
        )
    }
}

@Composable
fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFF6750A4) else Color(0xFFE7E0EC),
                shape = CircleShape
            )
            .clickable { onClick() }
            .testTag("color_circle_${color.toArgbHex()}")
    )
}

private fun Color.toArgbHex(): String {
    val a = (this.alpha * 255).toInt().coerceIn(0, 255)
    val r = (this.red * 255).toInt().coerceIn(0, 255)
    val g = (this.green * 255).toInt().coerceIn(0, 255)
    val b = (this.blue * 255).toInt().coerceIn(0, 255)
    return "%02X%02X%02X%02X".format(a, r, g, b)
}
