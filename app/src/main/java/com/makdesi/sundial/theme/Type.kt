package com.makdesi.sundial.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.makdesi.sundial.R

/**
 * Rule one of the design contract: two typefaces only.
 * Fraunces (warm serif, weight 300, italic for intentions) speaks for the app;
 * Manrope (plain grotesk) does everything functional.
 * Both ship as variable fonts; weights are pinned via font-variation settings.
 */

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun fraunces(weight: Int, italic: Boolean) = Font(
    resId = if (italic) R.font.fraunces_italic_var else R.font.fraunces_var,
    weight = FontWeight(weight),
    style = if (italic) FontStyle.Italic else FontStyle.Normal,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        FontVariation.Setting("opsz", 60f),
    ),
)

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun manrope(weight: Int) = Font(
    resId = R.font.manrope_var,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Serif = FontFamily(
    fraunces(300, italic = false),
    fraunces(400, italic = false),
    fraunces(300, italic = true),
)

val Grotesk = FontFamily(
    manrope(400),
    manrope(500),
    manrope(600),
    manrope(700),
)

/*
 * The Instrument face (sundial-two-faces.html, edition B) speaks two other
 * voices: Space Grotesk for everything, IBM Plex Mono for metadata.
 */

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun spaceGrotesk(weight: Int) = Font(
    resId = R.font.space_grotesk_var,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Instrument = FontFamily(
    spaceGrotesk(400),
    spaceGrotesk(500),
    spaceGrotesk(700),
)

val Mono = FontFamily(
    Font(R.font.plex_mono_regular, FontWeight.Normal),
    Font(R.font.plex_mono_medium, FontWeight.Medium),
)
