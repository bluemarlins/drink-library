package com.bluemarlin.drinkdiary.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Aura는 별도 Shape 토큰 객체를 노출하지 않아 로컬 Gap 컴포넌트(스낵바·진행 바)와 이미지 클립에만 사용한다.
// 타이포그래피와 반응형 간격은 모두 AuraTheme.typography / AuraTheme.layout을 사용한다.
object DDShape {
    val extraSmall = RoundedCornerShape(4.dp)
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
}
