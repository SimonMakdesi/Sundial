package com.makdesi.sundial.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * How a face speaks, everywhere in the app.
 *  - ceremonial: moments in the app's own voice (titles, intentions, rituals)
 *  - functional: everything operational (rows, buttons, fields)
 *  - meta: whispered metadata (statuses, spans, legends, tags)
 * Signature's ceremonial italics collapse to upright in Instrument, whose
 * headline register is bold instead of light.
 */
data class FaceVoice(
    val ceremonial: FontFamily,
    val functional: FontFamily,
    val meta: FontFamily,
    val italic: FontStyle,
    val titleWeight: FontWeight,
)

val SignatureVoice = FaceVoice(
    ceremonial = Serif,
    functional = Grotesk,
    meta = Grotesk,
    italic = FontStyle.Italic,
    titleWeight = FontWeight.Light,
)

val InstrumentVoice = FaceVoice(
    ceremonial = Instrument,
    functional = Instrument,
    meta = Mono,
    italic = FontStyle.Normal,
    titleWeight = FontWeight.Bold,
)

val LocalVoice = compositionLocalOf { SignatureVoice }
