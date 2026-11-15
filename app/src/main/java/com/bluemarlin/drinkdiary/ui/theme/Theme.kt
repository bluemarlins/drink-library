package com.bluemarlin.drinkdiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraTheme

@Composable
fun DrinkDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    AuraTheme(
        colorMode = if (darkTheme) AuraColorMode.Dark else AuraColorMode.Light,
        content = content,
    )
}
