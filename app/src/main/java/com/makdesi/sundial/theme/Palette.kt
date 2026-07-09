package com.makdesi.sundial.theme

import androidx.compose.ui.graphics.Color
import com.makdesi.sundial.domain.Mode

/**
 * Port of PALETTES in sundial-demo.html — the design contract. Values are verbatim.
 * `horizon` is the left-to-right gradient of the top band; `wash` bleeds down
 * `washHeight` (fraction of screen height) from the top.
 */
data class Palette(
    val bg: Color,
    val ink: Color,
    val faint: Color,
    val hair: Color,
    val overlay: Color,
    val horizon: List<Color>,
    val horizonStops: List<Float>,
    val wash: Color,
    val washHeight: Float,
    val isLight: Boolean,
    /** One functional accent, used only for "live" information (Instrument). */
    val accent: Color = Color.Unspecified,
)

val Dawn = Palette(
    bg = Color(0xFFF3EEE6),
    ink = Color(0xFF2A2620),
    faint = Color(0x702A2620),          // ink at .44
    hair = Color(0x212A2620),           // ink at .13
    overlay = Color(0xF0F3EEE6),        // bg at .94
    horizon = listOf(Color(0xFFF4B98E), Color(0xFFF2D3B0), Color(0xFFD9E2E8), Color(0xFFBFD2E0)),
    horizonStops = listOf(0f, .35f, .75f, 1f),
    wash = Color(0x42F4B98E),           // .26
    washHeight = .46f,
    isLight = true,
)

val Noon = Palette(
    bg = Color(0xFFF4F5F3),
    ink = Color(0xFF1E2124),
    faint = Color(0x6B1E2124),          // .42
    hair = Color(0x1F1E2124),           // .12
    overlay = Color(0xF0F4F5F3),
    horizon = listOf(Color(0xFFA8C8E8), Color(0xFFC2D9EE), Color(0xFFDCE9F4), Color(0xFFDCE9F4)),
    horizonStops = listOf(0f, .45f, 1f, 1f),
    wash = Color(0x1AA8C8E8),           // .10
    washHeight = .16f,
    isLight = true,
)

val Dusk = Palette(
    bg = Color(0xFF131216),
    ink = Color(0xFFD9D3C8),
    faint = Color(0x66D9D3C8),          // .40
    hair = Color(0x1FD9D3C8),           // .12
    overlay = Color(0xF0131216),
    horizon = listOf(Color(0xFF2B2140), Color(0xFF4A2E4E), Color(0xFF93502F), Color(0xFFC97B3C)),
    horizonStops = listOf(0f, .45f, .85f, 1f),
    wash = Color(0x21C97B3C),           // .13
    washHeight = .30f,
    isLight = false,
)

fun paletteFor(mode: Mode): Palette = when (mode) {
    Mode.MORNING -> Dawn
    Mode.DAY -> Noon
    Mode.EVENING -> Dusk
}

/**
 * The Instrument face (sundial-two-faces.html, edition B): one fixed light
 * palette, ruled lines, a single orange accent for live information. The
 * rhythm still follows the clock; only the skin holds still.
 */
val InstrumentPalette = Palette(
    bg = Color(0xFFF1F1EF),
    ink = Color(0xFF141517),
    faint = Color(0x80141517),          // .50
    hair = Color(0x24141517),           // .14
    overlay = Color(0xF0F1F1EF),
    horizon = listOf(Color(0xFF141517), Color(0xFF141517), Color(0xFF141517), Color(0xFF141517)),
    horizonStops = listOf(0f, .35f, .75f, 1f),
    wash = Color(0x00000000),
    washHeight = 0f,
    isLight = true,
    accent = Color(0xFFE8501E),
)
