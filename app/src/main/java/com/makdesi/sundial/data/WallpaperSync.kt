package com.makdesi.sundial.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.makdesi.sundial.domain.ModeEngine
import com.makdesi.sundial.theme.Palette
import com.makdesi.sundial.theme.instrumentPaletteFor
import com.makdesi.sundial.theme.paletteFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

/**
 * The lock screen follows the light too: it always wears the current face and
 * theme, so waking the phone flows seamlessly into Sundial. Following the sun,
 * it drifts at every mode boundary (the alarms we already have); a locked
 * theme holds it steady. Lock screen only — home lives behind Sundial anyway.
 */
object WallpaperSync {

    private val faceKey = stringPreferencesKey("face")
    private val themeKey = stringPreferencesKey("theme")
    private val lastSetKey = stringPreferencesKey("wallpaper_sig")

    suspend fun sync(context: Context) = withContext(Dispatchers.IO) {
        runCatching {
            val prefs = context.sundialDataStore.data.first()
            val face = prefs[faceKey]?.let { runCatching { Face.valueOf(it) }.getOrNull() }
                ?: Face.SIGNATURE
            val theme = prefs[themeKey]?.let { runCatching { ThemeChoice.valueOf(it) }.getOrNull() }
                ?: ThemeChoice.SUN

            val mode = when (theme) {
                ThemeChoice.SUN -> ModeEngine.modeAt(ZonedDateTime.now())
                ThemeChoice.DAWN -> com.makdesi.sundial.domain.Mode.MORNING
                ThemeChoice.NOON -> com.makdesi.sundial.domain.Mode.DAY
                ThemeChoice.DUSK -> com.makdesi.sundial.domain.Mode.EVENING
            }

            // Skip the (not-free) wallpaper write when nothing changed.
            val signature = "v7-${face.name}-${mode.name}"
            if (prefs[lastSetKey] == signature) return@withContext

            val palette = when (face) {
                Face.SIGNATURE -> paletteFor(mode)
                Face.INSTRUMENT -> instrumentPaletteFor(mode)
            }
            val metrics = context.resources.displayMetrics
            val bitmap = render(face, palette, metrics.widthPixels, metrics.heightPixels)

            WallpaperManager.getInstance(context)
                .setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            bitmap.recycle()

            context.sundialDataStore.edit { it[lastSetKey] = signature }
        } // fail silently — a wallpaper is never worth an error
    }

    private fun render(face: Face, palette: Palette, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val w = width.toFloat()
        val h = height.toFloat()

        canvas.drawColor(palette.bg.toArgb())

        if (face == Face.SIGNATURE) {
            // the horizon's wash bleeding down
            if (palette.washHeight > 0f) {
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h * palette.washHeight,
                    palette.wash.toArgb(), 0x00000000,
                    Shader.TileMode.CLAMP,
                )
                canvas.drawRect(0f, 0f, w, h * palette.washHeight, paint)
                paint.shader = null
            }
            // the horizon band, with a soft glow beneath (approximated, like the demo's blur)
            val bandShader = LinearGradient(
                0f, 0f, w, 0f,
                palette.horizon.map { it.toArgb() }.toIntArray(),
                palette.horizonStops.toFloatArray(),
                Shader.TileMode.CLAMP,
            )
            // The band runs from the very top edge (so corner clipping and
            // zoom-crop land band-on-band, invisibly) and the glow fades in
            // slices — light bleeding, never a header block.
            paint.shader = bandShader
            val bandTop = 0f
            val bandBottom = h * 0.05f
            val glowEnd = h * 0.17f
            val slices = 24
            val sliceHeight = (glowEnd - bandBottom) / slices
            for (i in 0 until slices) {
                val fade = 1f - i / slices.toFloat()
                paint.alpha = (70 * fade * fade).toInt()
                canvas.drawRect(
                    0f, bandBottom + i * sliceHeight,
                    w, bandBottom + (i + 1) * sliceHeight, paint,
                )
            }
            paint.alpha = 255
            canvas.drawRect(0f, bandTop, w, bandBottom, paint)
            paint.shader = null
        } else {
            // the instrument scale: ruled line + 24 ticks, majors every third
            val ink = palette.ink.toArgb()
            val faintInk = palette.ink.copy(alpha = .45f).toArgb()
            val baseline = h * 0.062f
            paint.color = ink
            paint.strokeWidth = h * 0.0022f
            canvas.drawLine(0f, baseline, w, baseline, paint)
            for (i in 0 until 24) {
                val major = i % 3 == 2
                val x = w * i / 24f
                paint.color = if (major) ink else faintInk
                paint.strokeWidth = if (major) h * 0.0022f else h * 0.0013f
                val tickHeight = if (major) h * 0.022f else h * 0.011f
                canvas.drawLine(x, baseline - tickHeight, x, baseline, paint)
            }
        }
        return bitmap
    }
}
