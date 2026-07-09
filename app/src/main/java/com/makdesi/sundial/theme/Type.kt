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
