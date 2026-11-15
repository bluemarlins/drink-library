package com.example.ds.component

import android.graphics.BlurMaskFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraTheme

/**
 * Optical materiality preset for Aura Spatial & Floating UI surfaces.
 */
enum class GlassStyle {
    /** Standard acrylic frost: 24~30dp backdrop blur, 1.3x saturation boost, adaptive surface tint, diagonal specular stroke. */
    FROSTED,

    /** Next-gen optical droplet lens: AGSL edge bevel refraction, chromatic RGB dispersion, inner Fresnel glow, and metaball physics. */
    LIQUID,
}

@Stable
class GlassBackdropState internal constructor(
    internal val graphicsLayer: GraphicsLayer,
) {
    internal var originInRoot by mutableStateOf(Offset.Zero)
    internal var hasRecorded by mutableStateOf(false)
    internal var contentDrawTick by mutableIntStateOf(0)

    /** Requests foreground [GlassSurface] backdrops to re-sample Layer 0 on content scroll or update. */
    fun invalidateBackdrop() {
        contentDrawTick++
    }
}

@Composable
fun rememberGlassBackdropState(): GlassBackdropState {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(graphicsLayer) { GlassBackdropState(graphicsLayer) }
}

val LocalGlassBackdropState = compositionLocalOf<GlassBackdropState?> { null }

/**
 * Records the Layer 0 live canvas into [state] so foreground [GlassSurface] panels
 * can sample, blur, saturate, and refract actual content passing behind them.
 */
fun Modifier.glassBackdropSource(state: GlassBackdropState): Modifier = this
    .onGloballyPositioned { coords ->
        val pos = coords.positionInRoot()
        if (state.originInRoot != pos) {
            state.originInRoot = pos
        }
    }
    .drawWithContent {
        if (size.width >= 1f && size.height >= 1f) {
            state.graphicsLayer.record {
                this@drawWithContent.drawContent()
            }
            if (!state.hasRecorded) {
                state.hasRecorded = true
            }
            drawLayer(state.graphicsLayer)
        } else {
            drawContent()
        }
    }

/**
 * Interruptible tactile spring compression (`0.92f` on Touch Down, rebounding with
 * `Spring(dampingRatio = 0.65f, stiffness = 450f)`).
 */
fun Modifier.fluidPressScale(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.92f,
): Modifier = composed {
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val isPressed by rememberAuraTactilePressed(
        interactionSource = interactionSource,
        minPressDurationMillis = motion.minPressDurationMillis,
    )
    val scale by animateFloatAsState(
        targetValue = if (!reducedMotion && enabled && isPressed) pressedScale else 1f,
        animationSpec = when {
            reducedMotion -> snap()
            isPressed -> motion.pressInSpring()
            else -> spring(dampingRatio = 0.65f, stiffness = 450f)
        },
        label = "FluidPressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

private const val LIQUID_GLASS_AGSL = """
uniform shader content;
uniform float2 uSize;
uniform float uCornerRadius;
uniform float uBevelPx;
uniform float uDispersionPx;
uniform float uRefractionPx;

float sdRoundBox(float2 p, float2 b, float r) {
    float2 q = abs(p) - b + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

half4 main(float2 fragCoord) {
    float2 halfSize = max(uSize * 0.5, float2(1.0));
    float r = min(uCornerRadius, min(halfSize.x, halfSize.y));
    float2 p = fragCoord - halfSize;

    float dist = sdRoundBox(p, halfSize, r);
    float inset = max(-dist, 0.0);

    float2 eps = float2(1.0, 0.0);
    float gx = sdRoundBox(p + eps.xy, halfSize, r) - sdRoundBox(p - eps.xy, halfSize, r);
    float gy = sdRoundBox(p + eps.yx, halfSize, r) - sdRoundBox(p - eps.yx, halfSize, r);
    float2 grad = float2(gx, gy);
    float gradLen = max(length(grad), 0.0001);
    float2 normal = grad / gradLen;

    // Preserve clear/soft Safe Zone at center; ramp convex lens warping cleanly in outer bevel (8dp)
    float bevelT = clamp(1.0 - (inset / max(uBevelPx, 1.0)), 0.0, 1.0);
    float curvature = bevelT * bevelT * (3.0 - 2.0 * bevelT);

    float2 refracted = fragCoord - normal * (curvature * uRefractionPx);
    float disp = (0.06 + curvature * 1.12) * uDispersionPx;
    float2 dispOffset = normal * disp;

    float2 cR = clamp(refracted + dispOffset, float2(0.0), uSize - float2(1.0));
    float2 cG = clamp(refracted, float2(0.0), uSize - float2(1.0));
    float2 cB = clamp(refracted - dispOffset, float2(0.0), uSize - float2(1.0));

    half rCh = content.eval(cR).r;
    half gCh = content.eval(cG).g;
    half bCh = content.eval(cB).b;
    half aCh = content.eval(cG).a;

    half3 rgb = half3(rCh, gCh, bCh);
    // Subtle Fresnel rim illumination near curved bevel perimeter
    half fresnel = half(pow(bevelT, 2.1) * 0.14);
    rgb = clamp(rgb + half3(fresnel), half3(0.0), half3(1.0));
    return half4(rgb, aCh);
}
"""

/**
 * Reusable Dual-Materiality Floating Glass Container ([GlassStyle.FROSTED] & [GlassStyle.LIQUID]).
 *
 * Samples the live Layer 0 [LocalGlassBackdropState] directly behind its screen bounds so real
 * scrolling cards, photography gradients, and typography blur or optically refract beneath without
 * affecting foreground chrome crispness.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    glassStyle: GlassStyle = GlassStyle.LIQUID,
    shape: Shape = RoundedCornerShape(50),
    cornerRadiusHint: Dp = 28.dp,
    backdropState: GlassBackdropState? = LocalGlassBackdropState.current,
    dispersionBoost: Float = 1f,
    surfaceAlphaMultiplier: Float = 1f,
    showShadow: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = AuraTheme.colors
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val density = LocalDensity.current
    val isDark = colors.isDark

    var surfacePositionInRoot by remember { mutableStateOf(Offset.Zero) }
    var surfaceSizePx by remember { mutableStateOf(Size.Zero) }

    val colorSpec = remember(reducedMotion, motion.stateChangeDurationMillis) {
        if (reducedMotion) snap<Color>() else tween(durationMillis = motion.stateChangeDurationMillis)
    }

    // Mode A (Frosted): Light White 72% / Dark #1E1E22 68%
    // Mode B (Liquid): Lower base opacity in crystal lens with safe-zone protection
    val darkFrostedTint = Color(red = 30f / 255f, green = 30f / 255f, blue = 34f / 255f, alpha = 0.68f)
    val darkLiquidTint = Color(red = 30f / 255f, green = 30f / 255f, blue = 34f / 255f, alpha = 0.54f)

    val rawTint = when (glassStyle) {
        GlassStyle.FROSTED -> if (isDark) darkFrostedTint else Color.White.copy(alpha = 0.72f)
        GlassStyle.LIQUID -> if (isDark) darkLiquidTint else Color.White.copy(alpha = 0.54f)
    }
    val targetTint = rawTint.copy(alpha = (rawTint.alpha * surfaceAlphaMultiplier.coerceIn(0f, 1f)))
    val animatedTint by animateColorAsState(
        targetValue = targetTint,
        animationSpec = colorSpec,
        label = "GlassSurfaceTint",
    )

    val blurPx = remember(density) { with(density) { 26.dp.toPx().coerceAtLeast(1f) } }
    val softLiquidBlurPx = remember(density) { with(density) { 10.dp.toPx().coerceAtLeast(1f) } }
    val bevelPx = remember(density) { with(density) { 8.dp.toPx().coerceAtLeast(1f) } }
    val cornerPx = remember(density, cornerRadiusHint, surfaceSizePx) {
        val hintPx = with(density) { cornerRadiusHint.toPx() }
        val maxHalf = minOf(surfaceSizePx.width, surfaceSizePx.height) * 0.5f
        if (maxHalf > 0f) minOf(hintPx, maxHalf) else hintPx
    }
    val dispersionPx = remember(density, dispersionBoost) {
        with(density) { (1.15f * dispersionBoost.coerceIn(0.6f, 2.4f)).dp.toPx() }
    }
    val refractionPx = remember(density) { with(density) { 5.5.dp.toPx() } }

    val cachedRuntimeShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching { RuntimeShader(LIQUID_GLASS_AGSL) }.getOrNull()
        } else {
            null
        }
    }

    val backdropRenderEffect: RenderEffect? = remember(
        glassStyle,
        blurPx,
        softLiquidBlurPx,
        surfaceSizePx,
        cornerPx,
        bevelPx,
        dispersionPx,
        refractionPx,
        cachedRuntimeShader,
        surfaceAlphaMultiplier,
    ) {
        if (surfaceAlphaMultiplier <= 0.02f) {
            null
        } else {
            when (glassStyle) {
                GlassStyle.FROSTED -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        GlassRenderEffectCompat.createFrostedEffect(blurPx)
                    } else {
                        null
                    }
                }
                GlassStyle.LIQUID -> {
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            cachedRuntimeShader != null &&
                            surfaceSizePx.width >= 2f &&
                            surfaceSizePx.height >= 2f -> {
                            GlassRenderEffectCompat.createLiquidShaderEffect(
                                shader = cachedRuntimeShader,
                                widthPx = surfaceSizePx.width,
                                heightPx = surfaceSizePx.height,
                                cornerRadiusPx = cornerPx,
                                bevelPx = bevelPx,
                                dispersionPx = dispersionPx,
                                refractionPx = refractionPx,
                                softBlurPx = softLiquidBlurPx,
                            ) ?: GlassRenderEffectCompat.createFrostedEffect(softLiquidBlurPx)
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                            GlassRenderEffectCompat.createFrostedEffect(softLiquidBlurPx)
                        }
                        else -> null
                    }
                }
            }
        }
    }

    val strokeStartAlpha = if (isDark) 0.34f else 0.55f
    val strokeEndAlpha = if (isDark) 0.06f else 0.08f
    val strokeEndOffset = Offset(
        x = surfaceSizePx.width.coerceAtLeast(1f),
        y = surfaceSizePx.height.coerceAtLeast(1f),
    )
    val strokeBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = strokeStartAlpha * surfaceAlphaMultiplier),
            Color.White.copy(alpha = strokeEndAlpha * surfaceAlphaMultiplier),
        ),
        start = Offset.Zero,
        end = strokeEndOffset,
    )

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val newPos = coords.positionInRoot()
                val newSize = Size(coords.size.width.toFloat(), coords.size.height.toFloat())
                if (surfacePositionInRoot != newPos) {
                    surfacePositionInRoot = newPos
                }
                if (surfaceSizePx != newSize) {
                    surfaceSizePx = newSize
                }
            }
            .dualGlassShadow(
                shape = shape,
                cornerRadiusHint = cornerRadiusHint,
                alphaMultiplier = surfaceAlphaMultiplier,
                enabled = showShadow,
            )
            .clip(shape),
    ) {
        // Real Pass-Through Backdrop Layer (isolated renderEffect so content text/icons stay razor-sharp)
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    clip = true
                    this.shape = shape
                    renderEffect = backdropRenderEffect
                },
        ) {
            if (backdropState != null && backdropState.hasRecorded && surfaceAlphaMultiplier > 0.02f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Observe backdrop tick in draw scope so scrolled Layer 0 updates foreground glass
                    @Suppress("UNUSED_EXPRESSION")
                    backdropState.contentDrawTick
                    if (backdropState.graphicsLayer.size.width > 0 && backdropState.graphicsLayer.size.height > 0) {
                        val relOffset = surfacePositionInRoot - backdropState.originInRoot
                        translate(left = -relOffset.x, top = -relOffset.y) {
                            drawLayer(backdropState.graphicsLayer)
                        }
                    }
                }
            }
        }

        // Optical tint & Canvas Fresnel / Specular / Fallback Chromatic Rim layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(animatedTint, shape),
        ) {
            if (glassStyle == GlassStyle.LIQUID && surfaceAlphaMultiplier > 0.05f) {
                LiquidOpticalOverlayCanvas(
                    cornerRadiusHint = cornerRadiusHint,
                    dispersionBoost = dispersionBoost,
                    intensity = surfaceAlphaMultiplier,
                    isDark = isDark,
                )
            }
        }

        // Diagonal Linear Specular Stroke (1dp)
        if (surfaceAlphaMultiplier > 0.05f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(width = 1.dp, brush = strokeBrush, shape = shape),
            )
        }

        // Crisp Foreground Content
        content()
    }
}

@Composable
private fun LiquidOpticalOverlayCanvas(
    cornerRadiusHint: Dp,
    dispersionBoost: Float,
    intensity: Float,
    isDark: Boolean,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (size.width <= 2f || size.height <= 2f) return@Canvas
        val radiusPx = minOf(cornerRadiusHint.toPx(), minOf(size.width, size.height) * 0.5f)
        val innerGlowPx = 6.dp.toPx()

        // Inner Volume Fresnel Glow (White 35%, 6dp feathered inset rings)
        val steps = 4
        for (i in 0 until steps) {
            val t = i.toFloat() / steps.toFloat()
            val inset = innerGlowPx * t * 0.65f
            val alpha = (0.35f * (1f - t) * (1f - t)) * intensity * (if (isDark) 0.55f else 1f)
            if (size.width - inset * 2f > 2f && size.height - inset * 2f > 2f) {
                drawRoundRect(
                    color = Color.White.copy(alpha = alpha.coerceIn(0f, 0.45f)),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2f, size.height - inset * 2f),
                    cornerRadius = CornerRadius((radiusPx - inset).coerceAtLeast(1f)),
                    style = Stroke(width = 1.8.dp.toPx()),
                )
            }
        }

        // Top-left water droplet convex specular catchlight
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.28f * intensity),
                    Color.Transparent,
                ),
                startY = 0f,
                endY = size.height * 0.48f,
            ),
            topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
            size = Size((size.width - 2.dp.toPx()).coerceAtLeast(0f), size.height * 0.48f),
            cornerRadius = CornerRadius(radiusPx),
        )

        // Subtle Chromatic Dispersion Prism Rim (enhances API 33 AGSL & carries API < 33 fallback)
        val dispAlpha = (0.16f * dispersionBoost.coerceIn(0.8f, 2f) * intensity).coerceAtMost(0.35f)
        val cyanRim = Color(red = 0.35f, green = 0.90f, blue = 1f, alpha = dispAlpha)
        val magentaRim = Color(red = 1f, green = 0.40f, blue = 0.80f, alpha = dispAlpha)
        val shift = (0.9.dp.toPx() * dispersionBoost).coerceAtMost(2.5.dp.toPx())

        drawRoundRect(
            color = cyanRim,
            topLeft = Offset(shift, shift * 0.5f),
            size = Size((size.width - shift * 2f).coerceAtLeast(1f), (size.height - shift).coerceAtLeast(1f)),
            cornerRadius = CornerRadius((radiusPx - shift).coerceAtLeast(1f)),
            style = Stroke(width = 0.9.dp.toPx()),
        )
        drawRoundRect(
            color = magentaRim,
            topLeft = Offset(shift * 0.4f, shift),
            size = Size((size.width - shift).coerceAtLeast(1f), (size.height - shift * 2f).coerceAtLeast(1f)),
            cornerRadius = CornerRadius((radiusPx - shift).coerceAtLeast(1f)),
            style = Stroke(width = 0.9.dp.toPx()),
        )
    }
}

private fun Modifier.dualGlassShadow(
    shape: Shape,
    cornerRadiusHint: Dp,
    alphaMultiplier: Float,
    enabled: Boolean,
): Modifier = if (!enabled || alphaMultiplier <= 0.02f) {
    this
} else {
    composed {
        val ambientPaint = remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG) }
        val keyPaint = remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG) }
        val genericShadowPath = remember { Path() }

        drawBehind {
            if (size.width <= 1f || size.height <= 1f) return@drawBehind
            val outline = shape.createOutline(size, layoutDirection, this)
            val cornerPx = minOf(cornerRadiusHint.toPx(), minOf(size.width, size.height) * 0.5f)
            val clampedAlpha = alphaMultiplier.coerceIn(0f, 1f)

            val ambientBlurPx = 24.dp.toPx().coerceAtLeast(1f)
            val ambientOffsetYPx = 10.dp.toPx()
            val ambientColor = Color.Black.copy(alpha = 0.15f * clampedAlpha)

            val keyBlurPx = 6.dp.toPx().coerceAtLeast(1f)
            val keyOffsetYPx = 3.dp.toPx()
            val keyColor = Color.Black.copy(alpha = 0.10f * clampedAlpha)

            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                drawBlurredShadowLayer(
                    nativeCanvas = nativeCanvas,
                    paint = ambientPaint,
                    outline = outline,
                    reusablePath = genericShadowPath,
                    cornerPx = cornerPx,
                    offsetY = ambientOffsetYPx,
                    blurPx = ambientBlurPx,
                    color = ambientColor,
                )
                drawBlurredShadowLayer(
                    nativeCanvas = nativeCanvas,
                    paint = keyPaint,
                    outline = outline,
                    reusablePath = genericShadowPath,
                    cornerPx = cornerPx,
                    offsetY = keyOffsetYPx,
                    blurPx = keyBlurPx,
                    color = keyColor,
                )
            }
        }
    }
}

private fun drawBlurredShadowLayer(
    nativeCanvas: android.graphics.Canvas,
    paint: android.graphics.Paint,
    outline: Outline,
    reusablePath: Path,
    cornerPx: Float,
    offsetY: Float,
    blurPx: Float,
    color: Color,
) {
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = color.toArgb()
    paint.maskFilter = runCatching {
        BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
    }.getOrNull()

    nativeCanvas.save()
    nativeCanvas.translate(0f, offsetY)
    when (outline) {
        is Outline.Rectangle -> {
            nativeCanvas.drawRect(
                outline.rect.left,
                outline.rect.top,
                outline.rect.right,
                outline.rect.bottom,
                paint,
            )
        }
        is Outline.Rounded -> {
            val rr = outline.roundRect
            val rx = rr.topLeftCornerRadius.x.takeIf { it > 0f } ?: cornerPx
            val ry = rr.topLeftCornerRadius.y.takeIf { it > 0f } ?: cornerPx
            nativeCanvas.drawRoundRect(rr.left, rr.top, rr.right, rr.bottom, rx, ry, paint)
        }
        is Outline.Generic -> {
            reusablePath.reset()
            reusablePath.addPath(outline.path)
            nativeCanvas.drawPath(reusablePath.asAndroidPath(), paint)
        }
    }
    nativeCanvas.restore()
}

internal fun calculateMetaballWaistRatio(mergeProgress: Float): Float {
    val clamped = mergeProgress.coerceIn(0f, 1f)
    return if (clamped <= 0f) 0f else (0.08f + 0.78f * clamped).coerceIn(0.08f, 0.86f)
}

internal fun calculateScrollProgress(
    firstVisibleIndex: Int,
    firstVisibleOffsetPx: Int,
    thresholdPx: Float,
): Float {
    if (firstVisibleIndex > 0) return 1f
    if (firstVisibleOffsetPx <= 0 || thresholdPx <= 0f) return 0f
    return (firstVisibleOffsetPx.toFloat() / thresholdPx).coerceIn(0f, 1f)
}

/**
 * Viscous Gooey / Metaball liquid neck bridge connecting two adjacent pills during separation or merge.
 *
 * Combines an API 31+ Gaussian blur + Alpha Threshold (`18 * alpha - 7.5`) `RenderEffect` over
 * overlapping droplet anchors with a concave hourglass meniscus and specular rim highlight.
 */
@Composable
fun LiquidMetaballBridge(
    gapWidthDp: Dp,
    heightDp: Dp,
    glassStyle: GlassStyle,
    mergeProgress: Float,
    modifier: Modifier = Modifier,
) {
    val isDark = AuraTheme.colors.isDark
    val density = LocalDensity.current
    val effectiveMerge = mergeProgress.coerceIn(0f, 1f)
    if (effectiveMerge <= 0.02f || gapWidthDp <= 0.5.dp) return

    val fillColor = when (glassStyle) {
        GlassStyle.FROSTED -> if (isDark) {
            Color(red = 30f / 255f, green = 30f / 255f, blue = 34f / 255f, alpha = 0.68f)
        } else {
            Color.White.copy(alpha = 0.72f)
        }
        GlassStyle.LIQUID -> if (isDark) {
            Color(red = 34f / 255f, green = 36f / 255f, blue = 42f / 255f, alpha = 0.62f)
        } else {
            Color.White.copy(alpha = 0.68f)
        }
    }

    val metaballBlurPx = remember(density) { with(density) { 4.5.dp.toPx().coerceAtLeast(1f) } }
    val metaballThresholdEffect = remember(metaballBlurPx, glassStyle) {
        if (glassStyle == GlassStyle.LIQUID && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GlassRenderEffectCompat.createMetaballAlphaThresholdEffect(metaballBlurPx)
        } else {
            null
        }
    }

    val bridgePath = remember { Path() }
    val rimTopPath = remember { Path() }
    val rimBottomPath = remember { Path() }

    Box(modifier = modifier) {
        // Metaball body: blurred blobs thresholded into sharp liquid silhouette on API 31+, clean Bezier fallback below
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (metaballThresholdEffect != null) {
                        renderEffect = metaballThresholdEffect
                        alpha = fillColor.alpha * effectiveMerge
                    }
                },
        ) {
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f) return@Canvas

            val waistRatio = calculateMetaballWaistRatio(effectiveMerge)
            val halfGapY = (h * (1f - waistRatio) * 0.5f).coerceIn(0f, h * 0.45f)
            val flareY = h * 0.06f

            bridgePath.reset()
            bridgePath.moveTo(-2f, flareY)
            bridgePath.cubicTo(
                x1 = w * 0.35f,
                y1 = halfGapY,
                x2 = w * 0.65f,
                y2 = halfGapY,
                x3 = w + 2f,
                y3 = flareY,
            )
            bridgePath.lineTo(w + 2f, h - flareY)
            bridgePath.cubicTo(
                x1 = w * 0.65f,
                y1 = h - halfGapY,
                x2 = w * 0.35f,
                y2 = h - halfGapY,
                x3 = -2f,
                y3 = h - flareY,
            )
            bridgePath.close()

            val bodyColor = if (metaballThresholdEffect != null) {
                fillColor.copy(alpha = 1f)
            } else {
                fillColor.copy(alpha = fillColor.alpha * effectiveMerge)
            }
            drawPath(path = bridgePath, color = bodyColor)

            if (metaballThresholdEffect != null) {
                val anchorRadius = h * 0.38f * effectiveMerge
                drawCircle(
                    color = bodyColor,
                    radius = anchorRadius,
                    center = Offset(0f, h * 0.5f),
                )
                drawCircle(
                    color = bodyColor,
                    radius = anchorRadius,
                    center = Offset(w, h * 0.5f),
                )
            }
        }

        // Crisp specular meniscus rim highlights above the metaball surface
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f) return@Canvas

            val waistRatio = calculateMetaballWaistRatio(effectiveMerge)
            val halfGapY = (h * (1f - waistRatio) * 0.5f).coerceIn(0f, h * 0.45f)
            val flareY = h * 0.06f

            rimTopPath.reset()
            rimTopPath.moveTo(0f, flareY)
            rimTopPath.cubicTo(w * 0.35f, halfGapY, w * 0.65f, halfGapY, w, flareY)

            rimBottomPath.reset()
            rimBottomPath.moveTo(0f, h - flareY)
            rimBottomPath.cubicTo(w * 0.35f, h - halfGapY, w * 0.65f, h - halfGapY, w, h - flareY)

            val specularAlpha = (if (glassStyle == GlassStyle.LIQUID) 0.52f else 0.32f) * effectiveMerge
            drawPath(
                path = rimTopPath,
                color = Color.White.copy(alpha = specularAlpha),
                style = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round),
            )
            drawPath(
                path = rimBottomPath,
                color = Color.White.copy(alpha = specularAlpha * 0.5f),
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

private object GlassRenderEffectCompat {
    @RequiresApi(Build.VERSION_CODES.S)
    fun createFrostedEffect(blurRadiusPx: Float): RenderEffect? = runCatching {
        val satMatrix = ColorMatrix().apply { setSaturation(1.3f) }
        val satFilter = AndroidRenderEffect.createColorFilterEffect(ColorMatrixColorFilter(satMatrix))
        val blur = AndroidRenderEffect.createBlurEffect(
            blurRadiusPx.coerceAtLeast(1f),
            blurRadiusPx.coerceAtLeast(1f),
            Shader.TileMode.CLAMP,
        )
        AndroidRenderEffect.createChainEffect(blur, satFilter).asComposeRenderEffect()
    }.getOrNull()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createLiquidShaderEffect(
        shader: RuntimeShader,
        widthPx: Float,
        heightPx: Float,
        cornerRadiusPx: Float,
        bevelPx: Float,
        dispersionPx: Float,
        refractionPx: Float,
        softBlurPx: Float,
    ): RenderEffect? = runCatching {
        shader.setFloatUniform("uSize", widthPx, heightPx)
        shader.setFloatUniform("uCornerRadius", cornerRadiusPx)
        shader.setFloatUniform("uBevelPx", bevelPx)
        shader.setFloatUniform("uDispersionPx", dispersionPx)
        shader.setFloatUniform("uRefractionPx", refractionPx)

        val satMatrix = ColorMatrix().apply { setSaturation(1.25f) }
        val satFilter = AndroidRenderEffect.createColorFilterEffect(ColorMatrixColorFilter(satMatrix))
        val blur = AndroidRenderEffect.createBlurEffect(
            softBlurPx.coerceAtLeast(1f),
            softBlurPx.coerceAtLeast(1f),
            Shader.TileMode.CLAMP,
        )
        val blurWithSat = AndroidRenderEffect.createChainEffect(blur, satFilter)
        val agslEffect = AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
        AndroidRenderEffect.createChainEffect(agslEffect, blurWithSat).asComposeRenderEffect()
    }.getOrNull()

    @RequiresApi(Build.VERSION_CODES.S)
    fun createMetaballAlphaThresholdEffect(blurRadiusPx: Float): RenderEffect? = runCatching {
        val blur = AndroidRenderEffect.createBlurEffect(
            blurRadiusPx.coerceAtLeast(1f),
            blurRadiusPx.coerceAtLeast(1f),
            Shader.TileMode.CLAMP,
        )
        val thresholdMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 18f, -7.5f * 255f,
            ),
        )
        val alphaThreshold = AndroidRenderEffect.createColorFilterEffect(ColorMatrixColorFilter(thresholdMatrix))
        AndroidRenderEffect.createChainEffect(alphaThreshold, blur).asComposeRenderEffect()
    }.getOrNull()
}

@Preview(name = "Glass Surface Dual Presets", showBackground = true)
@Composable
private fun GlassSurfacePreview() {
    AuraTheme(colorMode = AuraColorMode.Light) {
        val backdrop = rememberGlassBackdropState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(AuraTheme.colors.background.primary),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .glassBackdropSource(backdrop)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                AuraTheme.colors.brand.primary,
                                AuraTheme.colors.status.warning.solid,
                            ),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassSurface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        glassStyle = GlassStyle.FROSTED,
                        backdropState = backdrop,
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            AuraText(text = "FROSTED", style = AuraTheme.typography.labelLarge)
                        }
                    }
                    GlassSurface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        glassStyle = GlassStyle.LIQUID,
                        backdropState = backdrop,
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            AuraText(text = "LIQUID", style = AuraTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}
