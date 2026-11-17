package com.bluemarlin.drinkdiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.ds.foundation.glass.AuraGlass
import com.example.ds.foundation.glass.AuraGlassDefaults
import com.example.ds.foundation.layout.AuraWindowInfo
import com.example.ds.foundation.layout.rememberAuraWindowInfo
import com.example.ds.foundation.motion.AuraMotion
import com.example.ds.foundation.motion.AuraMotionDefaults
import com.example.ds.foundation.motion.AuraMotionProfile
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraMaterial
import com.example.ds.foundation.theme.AuraTheme
import com.example.ds.foundation.theme.rememberAuraReduceTransparency

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
