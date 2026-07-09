package com.makdesi.sundial.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.makdesi.sundial.R
import com.makdesi.sundial.data.AppEntry
import com.makdesi.sundial.domain.Mode
import com.makdesi.sundial.theme.Grotesk
import com.makdesi.sundial.theme.Motion
import com.makdesi.sundial.theme.Palette
import com.makdesi.sundial.theme.Serif
import com.makdesi.sundial.theme.reducedMotion

data class HomeState(
    val mode: Mode,
    val time: String,
    val meridiem: String?,
    val dateline: String,
    val nextMode: Mode,
    val nextModeAt: String,
)

@Composable
fun Mode.label(): String = stringResource(
    when (this) {
        Mode.MORNING -> R.string.mode_morning
        Mode.DAY -> R.string.mode_day
        Mode.EVENING -> R.string.mode_evening
    }
)

/** Crossfades every palette channel at the contract's timings (bg/ink 1.1s, horizon/wash 1.4s). */
@Composable
fun animatedPalette(target: Palette): Palette {
    val reduced = reducedMotion(LocalContext.current)
    val fast = if (reduced) Motion.NEAR_INSTANT_MS else Motion.PALETTE_MS
    val slow = if (reduced) Motion.NEAR_INSTANT_MS else Motion.HORIZON_MS

    @Composable
    fun color(c: Color, ms: Int): Color {
        val v by animateColorAsState(c, tween(ms), label = "palette")
        return v
    }

    val washH by animateFloatAsState(target.washHeight, tween(slow), label = "washH")
    return Palette(
        bg = color(target.bg, fast),
        ink = color(target.ink, fast),
        faint = color(target.faint, fast),
        hair = color(target.hair, fast),
        overlay = color(target.overlay, fast),
        horizon = target.horizon.map { color(it, slow) },
        horizonStops = target.horizonStops,
        wash = color(target.wash, slow),
        washHeight = washH,
        isLight = target.isLight,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    palette: Palette,
    home: HomeState,
    apps: List<AppEntry>,
    onOpen: (AppEntry) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    // During a hold the content recedes slightly as tactile feedback (plan §3.3).
    var holding by remember { mutableStateOf(false) }
    val holdScale by animateFloatAsState(if (holding) .97f else 1f, tween(500), label = "holdS")
    val holdAlpha by animateFloatAsState(if (holding) .75f else 1f, tween(500), label = "holdA")

    // Swipe up past the list's end also opens search — the sheet is reachable from anywhere.
    val overscroll = remember {
        object : NestedScrollConnection {
            var accumulated = 0f
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y < 0f) {
                    accumulated += available.y
                    if (accumulated < -120f) {
                        accumulated = 0f
                        onOpenSearch()
                    }
                } else if (available.y > 0f) {
                    accumulated = 0f
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        holding = true
                        tryAwaitRelease()
                        holding = false
                    },
                    onLongPress = {
                        holding = false
                        onOpenSettings()
                    },
                )
            }
            .pointerInput(Unit) {
                var dragged = 0f
                detectVerticalDragGestures(
                    onDragStart = { dragged = 0f },
                    onVerticalDrag = { _, delta ->
                        dragged += delta
                        if (dragged < -48.dp.toPx()) {
                            dragged = 0f
                            onOpenSearch()
                        }
                    },
                )
            },
    ) {
        // wash: the horizon's bleed down the screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(palette.washHeight)
                .background(Brush.verticalGradient(listOf(palette.wash, Color.Transparent))),
        )
        // horizon band: the one loudly beautiful place
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(
                    Brush.horizontalGradient(
                        colorStops = palette.horizonStops
                            .zip(palette.horizon) { stop, color -> stop to color }
                            .toTypedArray()
                    )
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .graphicsLayer(scaleX = holdScale, scaleY = holdScale, alpha = holdAlpha),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 480.dp)
                    .padding(start = 32.dp, end = 32.dp, top = 44.dp, bottom = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = home.time,
                        fontFamily = Serif,
                        fontWeight = FontWeight.Light,
                        fontSize = 72.sp,
                        letterSpacing = (-0.02).em,
                        lineHeight = 72.sp,
                        color = palette.ink,
                    )
                    home.meridiem?.let {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = it,
                            fontFamily = Serif,
                            fontWeight = FontWeight.Light,
                            fontStyle = FontStyle.Italic,
                            fontSize = 22.sp,
                            lineHeight = 30.sp,
                            color = palette.faint,
                        )
                    }
                }
                Text(
                    text = home.dateline,
                    fontFamily = Grotesk,
                    fontSize = 12.5.sp,
                    letterSpacing = 0.04.em,
                    color = palette.faint,
                    modifier = Modifier.padding(top = 10.dp),
                )

                // app list with soft fade masks top and bottom, no scrollbar
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 22.dp)
                        .nestedScroll(overscroll)
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
                    items(apps, key = { it.packageName + "/" + it.activityClassName }) { app ->
                        Text(
                            text = app.label.lowercase(),
                            fontFamily = Grotesk,
                            fontWeight = FontWeight.Medium,
                            fontSize = 19.sp,
                            letterSpacing = 0.01.em,
                            color = palette.ink,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .combinedClickable(
                                    onClick = { onOpen(app) },
                                    onLongClick = onOpenSettings,
                                )
                                .padding(vertical = 9.dp),
                        )
                    }
                }

                Text(
                    text = stringResource(
                        R.string.footer_rhythm,
                        0, // apps asleep — real once per-mode lists arrive (M5)
                        home.nextMode.label(),
                        home.nextModeAt,
                    ),
                    fontFamily = Grotesk,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.05.em,
                    color = palette.faint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .width(72.dp)
                        .height(4.dp)
                        .background(palette.hair, RoundedCornerShape(999.dp))
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}
