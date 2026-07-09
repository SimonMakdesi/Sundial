package com.makdesi.sundial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.makdesi.sundial.R
import com.makdesi.sundial.data.Face
import com.makdesi.sundial.data.ThemeChoice
import com.makdesi.sundial.theme.Instrument
import com.makdesi.sundial.theme.LocalVoice
import com.makdesi.sundial.theme.Mono
import com.makdesi.sundial.theme.Palette
import com.makdesi.sundial.theme.instrumentPaletteFor
import com.makdesi.sundial.theme.paletteFor
import java.util.Locale

/**
 * The theme gallery: swipe through live miniatures of your own home —
 * current apps, current face — wearing each theme. Tap one to live in it.
 */
@Composable
fun ThemePicker(
    palette: Palette,
    face: Face,
    current: ThemeChoice,
    home: HomeState,
    onPick: (ThemeChoice) -> Unit,
    onBack: () -> Unit,
) {
    val choices = ThemeChoice.entries
    val pagerState = rememberPagerState(
        initialPage = choices.indexOf(current).coerceAtLeast(0),
        pageCount = { choices.size },
    )

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(top = 44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(R.string.settings_theme),
                fontFamily = LocalVoice.current.ceremonial,
                fontWeight = LocalVoice.current.titleWeight,
                fontSize = 30.sp,
                color = palette.ink,
            )
            Text(
                text = stringResource(R.string.editor_back),
                fontFamily = LocalVoice.current.functional,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 0.05.em,
                color = palette.faint,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        HorizontalPager(
            state = pagerState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 72.dp),
            pageSpacing = 20.dp,
            modifier = Modifier.weight(1f).widthIn(max = 480.dp),
        ) { page ->
            val choice = choices[page]
            val previewMode = when (choice) {
                ThemeChoice.SUN -> home.mode
                ThemeChoice.DAWN -> com.makdesi.sundial.domain.Mode.MORNING
                ThemeChoice.NOON -> com.makdesi.sundial.domain.Mode.DAY
                ThemeChoice.DUSK -> com.makdesi.sundial.domain.Mode.EVENING
            }
            val previewPalette =
                if (face == Face.INSTRUMENT) instrumentPaletteFor(previewMode)
                else paletteFor(previewMode)
            val selected = choice == current

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) palette.ink else palette.hair,
                            shape = RoundedCornerShape(28.dp),
                        )
                        .padding(if (selected) 4.dp else 3.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onPick(choice) },
                ) {
                    HomeMiniature(previewPalette, face, home)
                }
                Text(
                    text = stringResource(
                        when (choice) {
                            ThemeChoice.SUN -> R.string.theme_sun
                            ThemeChoice.DAWN -> R.string.theme_dawn
                            ThemeChoice.NOON -> R.string.theme_noon
                            ThemeChoice.DUSK -> R.string.theme_dusk
                        }
                    ) + if (selected) " ✓" else "",
                    fontFamily = LocalVoice.current.functional,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.03.em,
                    color = if (selected) palette.ink else palette.faint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 14.dp, bottom = 24.dp),
                )
            }
        }
    }
}

/** A quiet, non-interactive miniature of the home screen in a given palette. */
@Composable
private fun HomeMiniature(p: Palette, face: Face, home: HomeState) {
    val locale = Locale.getDefault()
    Box(Modifier.fillMaxSize().background(p.bg)) {
        if (face == Face.INSTRUMENT) {
            // mini instrument scale
            Box(Modifier.fillMaxWidth().height(14.dp)) {
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom) {
                    repeat(24) { i ->
                        val major = i % 3 == 2
                        Box(Modifier.weight(1f).height(if (major) 7.dp else 4.dp)) {
                            Box(
                                Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(if (major) p.ink else p.ink.copy(alpha = .35f)),
                            )
                        }
                    }
                }
                Box(
                    Modifier.fillMaxWidth().height(1.dp)
                        .align(Alignment.BottomStart).background(p.ink),
                )
                Box(
                    Modifier
                        .align(
                            androidx.compose.ui.BiasAlignment(
                                horizontalBias = home.dayFraction * 2f - 1f,
                                verticalBias = 1f,
                            )
                        )
                        .size(4.dp)
                        .background(p.accent, CircleShape),
                )
            }
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(p.washHeight)
                    .background(Brush.verticalGradient(listOf(p.wash, androidx.compose.ui.graphics.Color.Transparent))),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colorStops = p.horizonStops.zip(p.horizon) { s, c -> s to c }
                                .toTypedArray()
                        )
                    ),
            )
        }

        Column(Modifier.padding(horizontal = 18.dp, vertical = 22.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = home.time,
                    fontFamily = if (face == Face.INSTRUMENT) Instrument
                    else LocalVoice.current.ceremonial,
                    fontWeight = if (face == Face.INSTRUMENT) FontWeight.Bold
                    else FontWeight.Light,
                    fontSize = 34.sp,
                    lineHeight = 34.sp,
                    letterSpacing = if (face == Face.INSTRUMENT) (-0.04).em else (-0.02).em,
                    color = p.ink,
                )
                home.meridiem?.let {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (face == Face.INSTRUMENT) it.uppercase(locale) else it,
                        fontFamily = if (face == Face.INSTRUMENT) Mono
                        else LocalVoice.current.ceremonial,
                        fontStyle = LocalVoice.current.italic,
                        fontSize = 10.sp,
                        color = p.faint,
                    )
                }
            }
            Text(
                text = if (face == Face.INSTRUMENT) home.dateShort.uppercase(locale)
                else home.dateline,
                fontFamily = if (face == Face.INSTRUMENT) Mono else LocalVoice.current.functional,
                fontSize = 7.5.sp,
                letterSpacing = 0.05.em,
                color = p.faint,
                modifier = Modifier.padding(top = 5.dp),
            )
            if (home.intention.isNotBlank()) {
                Column(Modifier.padding(top = 10.dp)) {
                    Box(
                        Modifier.fillMaxWidth()
                            .height(if (face == Face.INSTRUMENT) 1.dp else 0.5.dp)
                            .background(if (face == Face.INSTRUMENT) p.ink else p.hair),
                    )
                    Text(
                        text = if (face == Face.INSTRUMENT) home.intention.uppercase(locale)
                        else home.intention,
                        fontFamily = if (face == Face.INSTRUMENT) Instrument
                        else LocalVoice.current.ceremonial,
                        fontStyle = LocalVoice.current.italic,
                        fontWeight = if (face == Face.INSTRUMENT) FontWeight.Medium
                        else FontWeight.Light,
                        fontSize = 8.sp,
                        lineHeight = 12.sp,
                        color = p.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 7.dp),
                    )
                    Box(Modifier.fillMaxWidth().height(0.5.dp).background(p.hair))
                }
            }
            Column(Modifier.padding(top = 12.dp).weight(1f)) {
                home.modeApps.take(6).forEachIndexed { i, app ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 5.dp),
                    ) {
                        if (face == Face.INSTRUMENT) {
                            Text(
                                text = String.format(locale, "%02d", i + 1),
                                fontFamily = Mono,
                                fontSize = 6.5.sp,
                                color = p.ink.copy(alpha = .4f),
                            )
                            Spacer(Modifier.width(7.dp))
                        }
                        Text(
                            text = app.label.lowercase(),
                            fontFamily = if (face == Face.INSTRUMENT) Instrument
                            else LocalVoice.current.functional,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.5.sp,
                            color = p.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (face == Face.INSTRUMENT) {
                        Box(Modifier.fillMaxWidth().height(0.5.dp).background(p.hair))
                    }
                }
            }
            Text(
                text = stringResource(
                    R.string.footer_rhythm,
                    home.asleepCount,
                    home.nextMode.label(),
                    home.nextModeAt,
                ).let { if (face == Face.INSTRUMENT) it.uppercase(locale) else it },
                fontFamily = if (face == Face.INSTRUMENT) Mono else LocalVoice.current.functional,
                fontSize = 6.5.sp,
                letterSpacing = 0.05.em,
                color = p.faint,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .width(36.dp)
                    .height(2.dp)
                    .background(
                        if (face == Face.INSTRUMENT) p.ink else p.hair,
                        RoundedCornerShape(999.dp),
                    )
                    .align(Alignment.CenterHorizontally),
            )
        }
    }
}
