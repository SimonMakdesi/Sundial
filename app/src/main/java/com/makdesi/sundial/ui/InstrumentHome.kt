package com.makdesi.sundial.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.makdesi.sundial.R
import com.makdesi.sundial.data.AppEntry
import com.makdesi.sundial.data.Side
import com.makdesi.sundial.theme.Instrument
import com.makdesi.sundial.theme.Mono
import com.makdesi.sundial.theme.Palette
import java.util.Locale

/**
 * The Instrument face (sundial-two-faces.html, edition B): grotesk-only,
 * ruled lines, mono metadata, one accent used only for live information.
 * Same product, same gestures, same rhythm — a different identity.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstrumentHome(
    palette: Palette,
    home: HomeState,
    ritualFlags: Set<String>,
    align: Side,
    onOpen: (AppEntry) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    val right = align == Side.RIGHT
    val locale = Locale.getDefault()
    val gestures = rememberHomeGestures(onOpenSettings, onOpenSearch)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .then(gestures.rootModifier),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .graphicsLayer(
                    scaleX = gestures.holdScale,
                    scaleY = gestures.holdScale,
                    alpha = gestures.holdAlpha,
                ),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
            ) {
                InstrumentScale(palette, home.dayFraction)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 32.dp, end = 32.dp, bottom = 10.dp),
                    horizontalAlignment = if (right) Alignment.End else Alignment.Start,
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 6.dp),
                    ) {
                        Text(
                            text = home.time,
                            fontFamily = Instrument,
                            fontWeight = FontWeight.Bold,
                            fontSize = 68.sp,
                            letterSpacing = (-0.04).em,
                            lineHeight = 68.sp,
                            color = palette.ink,
                        )
                        home.meridiem?.let {
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = it.uppercase(locale),
                                fontFamily = Mono,
                                fontSize = 13.sp,
                                lineHeight = 22.sp,
                                color = palette.faint,
                            )
                        }
                    }

                    // mono dateline: date · temperature (when whispering) · mode
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        MonoMeta(home.dateShort.uppercase(locale), palette)
                        home.temperature?.let { MonoMeta(it.uppercase(locale), palette) }
                        MonoMeta(home.mode.label().uppercase(locale), palette)
                    }

                    if (home.intention.isNotBlank()) {
                        Column(Modifier.padding(top = 20.dp)) {
                            Box(Modifier.fillMaxWidth().height(1.5.dp).background(palette.ink))
                            Row(Modifier.padding(vertical = 12.dp)) {
                                Text(
                                    text = "—",
                                    fontFamily = Instrument,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.5.sp,
                                    color = palette.accent,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = home.intention.uppercase(locale),
                                    fontFamily = Instrument,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.5.sp,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.04.em,
                                    color = palette.ink,
                                )
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(palette.ink.copy(alpha = .2f)),
                            )
                        }
                    }

                    CompositionLocalProvider(
                        androidx.compose.foundation.LocalOverscrollConfiguration provides null
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 24.dp)
                                .nestedScroll(gestures.overscroll)
                                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                .drawWithContent {
                                    drawContent()
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            0f to Color.Transparent,
                                            14f / size.height to Color.Black,
                                            1f - 18f / size.height to Color.Black,
                                            1f to Color.Transparent,
                                        ),
                                        blendMode = BlendMode.DstIn,
                                    )
                                },
                        ) {
                            itemsIndexed(
                                home.modeApps,
                                key = { _, it -> it.packageName + "/" + it.activityClassName },
                            ) { index, app ->
                                InstrumentRow(
                                    palette = palette,
                                    index = index,
                                    app = app,
                                    count = home.counts[app.packageName]?.takeIf { it > 0 },
                                    flagged = app.packageName in ritualFlags,
                                    right = right,
                                    onOpen = { onOpen(app) },
                                    onOpenSettings = onOpenSettings,
                                )
                            }
                        }
                    }

                    // split mono footer: {n} asleep · {NextMode} → {time}
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        MonoMeta(
                            stringResource(R.string.instrument_foot_asleep, home.asleepCount)
                                .uppercase(locale),
                            palette,
                            small = true,
                        )
                        MonoMeta(
                            stringResource(
                                R.string.instrument_foot_next,
                                home.nextMode.label(),
                                home.nextModeAt,
                            ).uppercase(locale),
                            palette,
                            small = true,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .width(64.dp)
                            .height(3.dp)
                            .background(palette.ink)
                            .align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonoMeta(text: String, palette: Palette, small: Boolean = false) {
    Text(
        text = text,
        fontFamily = Mono,
        fontSize = if (small) 9.5.sp else 10.5.sp,
        letterSpacing = if (small) 0.1.em else 0.08.em,
        color = palette.faint,
    )
}

/** The instrument scale: 24 ticks, majors every third hour, the accent marker is now. */
@Composable
private fun InstrumentScale(palette: Palette, dayFraction: Float) {
    Box(Modifier.fillMaxWidth().height(26.dp)) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom,
        ) {
            repeat(24) { i ->
                val major = i % 3 == 2
                Box(
                    Modifier
                        .weight(1f)
                        .height(if (major) 12.dp else 6.dp),
                ) {
                    Box(
                        Modifier
                            .width(if (major) 1.5.dp else 1.dp)
                            .fillMaxHeight()
                            .background(
                                if (major) palette.ink
                                else palette.ink.copy(alpha = .35f)
                            ),
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).align(Alignment.BottomStart).background(palette.ink))
        androidx.compose.ui.layout.Layout(
            content = {
                Box(
                    Modifier
                        .size(7.dp)
                        .background(palette.accent, CircleShape)
                        .semantics { contentDescription = "" },
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) { measurables, constraints ->
            val marker = measurables.first().measure(
                androidx.compose.ui.unit.Constraints()
            )
            layout(constraints.maxWidth, constraints.maxHeight) {
                val x = (constraints.maxWidth * dayFraction).toInt() - marker.width / 2
                val y = constraints.maxHeight - marker.height / 2 - 1
                marker.place(x.coerceIn(0, constraints.maxWidth - marker.width), y)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstrumentRow(
    palette: Palette,
    index: Int,
    app: AppEntry,
    count: Int?,
    flagged: Boolean,
    right: Boolean,
    onOpen: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val locale = Locale.getDefault()
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (right) Arrangement.End else Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .combinedClickable(onClick = onOpen, onLongClick = onOpenSettings)
                .padding(vertical = 6.dp),
        ) {
            val idx = @Composable {
                Text(
                    text = String.format(locale, "%02d", index + 1),
                    fontFamily = Mono,
                    fontSize = 10.sp,
                    color = palette.ink.copy(alpha = .4f),
                )
            }
            val name = @Composable {
                Text(
                    text = app.label.lowercase(),
                    fontFamily = Instrument,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    letterSpacing = 0.01.em,
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val notes = @Composable {
                count?.let {
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = stringResource(R.string.instrument_count, it).uppercase(locale),
                        fontFamily = Mono,
                        fontSize = 10.sp,
                        letterSpacing = 0.05.em,
                        color = palette.faint,
                    )
                }
                if (flagged) {
                    Spacer(Modifier.width(14.dp))
                    Box(Modifier.size(5.dp).background(palette.accent, CircleShape))
                }
            }

            if (right) {
                notes()
                if (count != null || flagged) Spacer(Modifier.width(14.dp))
                name()
                Spacer(Modifier.width(14.dp))
                idx()
            } else {
                idx()
                Spacer(Modifier.width(14.dp))
                name()
                notes()
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(palette.hair))
    }
}
