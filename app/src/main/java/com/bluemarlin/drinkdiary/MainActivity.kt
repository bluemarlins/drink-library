package com.bluemarlin.drinkdiary

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bluemarlin.drinkdiary.domain.model.AppearanceSettings
import com.bluemarlin.drinkdiary.domain.model.ColorModePreference
import com.bluemarlin.drinkdiary.ui.navigation.DrinkDiaryApp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiaryTheme
import com.bluemarlin.drinkdiary.ui.theme.toAuraMaterial

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val appearanceState = (application as DrinkDiaryApplication).appContainer.appearance
        // 저장된 설정을 읽기 전에 기본 테마로 그리면 다크 설정 사용자는 흰 화면이 번쩍이고,
        // 아무것도 안 그리면 빈 화면이 보인다. 설정이 올 때까지 시스템 스플래시를 유지한다.
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (appearanceState.value == null) return false
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            },
        )
        setContent {
            val appearance: AppearanceSettings? by appearanceState.collectAsState()
            val settings = appearance
            if (settings != null) {
                val darkTheme =
                    when (settings.colorMode) {
                        ColorModePreference.System -> isSystemInDarkTheme()
                        ColorModePreference.Light -> false
                        ColorModePreference.Dark -> true
                    }
                // 시스템과 다른 모드를 고르면 상태 표시줄 아이콘 색도 앱 모드를 따라야 읽힌다.
                LaunchedEffect(darkTheme) {
                    val style =
                        if (darkTheme) {
                            SystemBarStyle.dark(Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                        }
                    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                }
                DrinkDiaryTheme(
                    darkTheme = darkTheme,
                    material = settings.surfaceStyle.toAuraMaterial(),
                ) {
                    DrinkDiaryApp()
                }
            }
        }
    }
}
