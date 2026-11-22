package com.bluemarlin.drinkdiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.bluemarlin.drinkdiary.domain.model.SurfaceStyle
import io.github.bluemarlins.aura.foundation.glass.AuraGlass
import io.github.bluemarlins.aura.foundation.glass.AuraGlassDefaults
import io.github.bluemarlins.aura.foundation.layout.AuraWindowInfo
import io.github.bluemarlins.aura.foundation.layout.rememberAuraWindowInfo
import io.github.bluemarlins.aura.foundation.motion.AuraMotion
import io.github.bluemarlins.aura.foundation.motion.AuraMotionDefaults
import io.github.bluemarlins.aura.foundation.motion.AuraMotionProfile
import io.github.bluemarlins.aura.foundation.theme.AuraColorMode
import io.github.bluemarlins.aura.foundation.theme.AuraMaterial
import io.github.bluemarlins.aura.foundation.theme.AuraTheme
import io.github.bluemarlins.aura.foundation.theme.rememberAuraReduceTransparency

fun SurfaceStyle.toAuraMaterial(): AuraMaterial =
    when (this) {
        SurfaceStyle.Solid -> AuraMaterial.Solid
        SurfaceStyle.GlassMorphism -> AuraMaterial.Glass
        SurfaceStyle.LiquidGlass -> AuraMaterial.LiquidGlass
    }

@Composable
fun DrinkDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    material: AuraMaterial = AuraMaterial.LiquidGlass,
    motionProfile: AuraMotionProfile = AuraMotionProfile.Standard,
    reduceTransparency: Boolean = rememberAuraReduceTransparency(),
    motion: AuraMotion = AuraMotionDefaults.forProfile(motionProfile),
    glass: AuraGlass =
        AuraGlassDefaults.forColorMode(
            if (darkTheme) AuraColorMode.Dark else AuraColorMode.Light,
        ),
    windowInfo: AuraWindowInfo = rememberAuraWindowInfo(),
    content: @Composable () -> Unit,
) {
    AuraTheme(
        colorMode = if (darkTheme) AuraColorMode.Dark else AuraColorMode.Light,
        material = material,
        motionProfile = motionProfile,
        reduceTransparency = reduceTransparency,
        motion = motion,
        glass = glass,
        windowInfo = windowInfo,
        content = content,
    )
}
