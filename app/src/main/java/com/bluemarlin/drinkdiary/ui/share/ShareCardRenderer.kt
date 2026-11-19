package com.bluemarlin.drinkdiary.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.TasteType
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.profile.TasteTypeCopy
import com.example.ds.foundation.color.AuraColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ShareCardContent(
    val scope: TypeScope,
    val type: TasteType,
    val recordCount: Int,
    val preferences: List<TraitPreference>,
)

object ShareCardRenderer {
    private const val WIDTH = 1080
    private const val HEIGHT = 1920

    fun render(
        content: ShareCardContent,
        colors: AuraColors,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(colors.background.primary.toArgb())

        val cardBg =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.surface.raised.toArgb()
            }
        val cardBorder =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.border.subtle.toArgb()
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
        val outerRect = RectF(72f, 96f, WIDTH - 72f, HEIGHT - 96f)
        canvas.drawRoundRect(outerRect, 48f, 48f, cardBg)
        canvas.drawRoundRect(outerRect, 48f, 48f, cardBorder)

        val secondaryText =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.content.secondary.toArgb()
                textSize = 36f
            }
        val primaryText =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.content.primary.toArgb()
                textSize = 44f
            }
        val scopeLabel =
            when (content.scope) {
                TypeScope.Wine -> "와인 취향 카드"
                TypeScope.Whiskey -> "위스키 취향 카드"
                TypeScope.Combined -> "와인 & 위스키 통합 취향 카드"
            }

        canvas.drawText("음주일기 · $scopeLabel", 140f, 210f, secondaryText)

        val codePaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.brand.primary.toArgb()
                textSize = 152f
                typeface = Typeface.DEFAULT_BOLD
            }
        canvas.drawText(content.type.code, 140f, 390f, codePaint)

        val titlePaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.content.primary.toArgb()
                textSize = 56f
                typeface = Typeface.DEFAULT_BOLD
            }
        canvas.drawText(TasteTypeCopy.shortName(content.type), 140f, 475f, titlePaint)

        val sentencePaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.content.primary.toArgb()
                textSize = 46f
            }
        val sentenceLayout =
            StaticLayout.Builder
                .obtain(
                    TasteTypeCopy.sentence(content.type),
                    0,
                    TasteTypeCopy.sentence(content.type).length,
                    sentencePaint,
                    WIDTH - 280,
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(12f, 1.0f)
                .build()
        canvas.save()
        canvas.translate(140f, 540f)
        sentenceLayout.draw(canvas)
        canvas.restore()

        val trackPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.surface.sunken.toArgb()
            }
        val midTickPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.border.strong.toArgb()
            }
        val brandDotPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.brand.primary.toArgb()
            }
        val neutralDotPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.content.secondary.toArgb()
            }
        val smallLabelPaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.content.secondary.toArgb()
                textSize = 32f
            }

        val startY = 820f
        val rowHeight = 148f
        val barLeft = 140f
        val barRight = WIDTH - 140f
        val barWidth = barRight - barLeft

        content.preferences.take(6).forEachIndexed { index, pref ->
            val y = startY + index * rowHeight
            canvas.drawText(DrinkLabels.trait(pref.trait), barLeft, y, primaryText)

            val statusLabel =
                when (pref.preference) {
                    TastePreference.High, TastePreference.Low ->
                        DrinkLabels.preference(pref.trait, pref.preference)
                    TastePreference.Neutral -> "가리지 않음"
                    null -> "표본 수집 중"
                }
            val statusWidth = secondaryText.measureText(statusLabel)
            canvas.drawText(statusLabel, barRight - statusWidth, y, secondaryText)

            val trackTop = y + 26f
            canvas.drawRoundRect(RectF(barLeft, trackTop, barRight, trackTop + 14f), 7f, 7f, trackPaint)
            val midX = barLeft + barWidth * 0.5f
            canvas.drawRect(RectF(midX - 2f, trackTop - 5f, midX + 2f, trackTop + 19f), midTickPaint)

            val fraction: Float? =
                when (pref.preference) {
                    TastePreference.High -> (0.5f + pref.strength.coerceIn(0.35, 1.0).toFloat() * 0.44f)
                    TastePreference.Low -> (0.5f - pref.strength.coerceIn(0.35, 1.0).toFloat() * 0.44f)
                    TastePreference.Neutral -> 0.5f
                    null -> null
                }
            if (fraction != null) {
                val dotX = barLeft + barWidth * fraction
                val dotPaint = if (pref.preference == TastePreference.Neutral) neutralDotPaint else brandDotPaint
                canvas.drawCircle(dotX, trackTop + 7f, 16f, dotPaint)
            }

            val poles = DrinkLabels.poles(pref.trait)
            canvas.drawText(poles.first, barLeft, trackTop + 56f, smallLabelPaint)
            val rightW = smallLabelPaint.measureText(poles.second)
            canvas.drawText(poles.second, barRight - rightW, trackTop + 56f, smallLabelPaint)
        }

        val footer = "일기 ${content.recordCount}잔 바탕 · Drink Diary"
        canvas.drawText(footer, 140f, HEIGHT - 165f, secondaryText)

        return bitmap
    }

    suspend fun shareBitmap(
        context: Context,
        bitmap: Bitmap,
    ) {
        val uri =
            withContext(Dispatchers.IO) {
                val dir = File(context.cacheDir, "share_cards").apply { mkdirs() }
                val file = File(dir, "taste_card.png")
                file.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }

        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(Intent.createChooser(shareIntent, "취향 카드 공유하기"))
    }
}
