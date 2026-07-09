package com.makdesi.sundial.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makdesi.sundial.R
import com.makdesi.sundial.theme.Grotesk
import com.makdesi.sundial.theme.LocalVoice
import com.makdesi.sundial.theme.Motion
import com.makdesi.sundial.theme.Palette
import com.makdesi.sundial.theme.Serif
import kotlinx.coroutines.delay

/**
 * The breath ritual (plan §3.5): a full-screen pause in the palette's
 * translucent tone. One breath, then two quiet choices.
 */
@Composable
fun RitualOverlay(
    palette: Palette,
    onOpenForTen: () -> Unit,
    onNotNow: () -> Unit,
) {
    val reduced = com.makdesi.sundial.theme.reducedMotion(LocalContext.current)
    var choicesShown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(if (reduced) 600L else Motion.RITUAL_CHOICES_DELAY_MS)
        choicesShown = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.overlay)
            .clickable(enabled = false) {}, // swallow touches to the layers beneath
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            Box(Modifier.height(130.dp), contentAlignment = Alignment.Center) {
                if (reduced) {
                    // reduced motion: a still circle at mid-opacity
                    Box(
                        Modifier
                            .size(88.dp)
                            .graphicsLayer(alpha = .65f)
                            .border(1.5.dp, palette.ink, CircleShape),
                    )
                } else {
                    val breath = rememberInfiniteTransition(label = "breath")
                    val scale by breath.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.32f,
                        animationSpec = infiniteRepeatable(
                            tween(Motion.BREATH_MS / 2, easing = EaseInOut),
                            RepeatMode.Reverse,
                        ),
                        label = "scale",
                    )
                    val alpha by breath.animateFloat(
                        initialValue = .45f,
                        targetValue = .9f,
                        animationSpec = infiniteRepeatable(
                            tween(Motion.BREATH_MS / 2, easing = EaseInOut),
                            RepeatMode.Reverse,
                        ),
                        label = "alpha",
                    )
                    Box(
                        Modifier
                            .size(88.dp)
                            .scale(scale)
                            .graphicsLayer(alpha = alpha)
                            .border(1.5.dp, palette.ink, CircleShape),
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(
                        if (choicesShown) R.string.ritual_still else R.string.ritual_line
                    ),
                    fontFamily = LocalVoice.current.ceremonial,
                    fontWeight = LocalVoice.current.titleWeight,
                    fontSize = 22.sp,
                    color = palette.ink,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                if (choicesShown) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 26.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.ritual_open_ten),
                            fontFamily = LocalVoice.current.functional,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = palette.ink,
                            modifier = Modifier
                                .border(1.dp, palette.hair, RoundedCornerShape(999.dp))
                                .clickable { onOpenForTen() }
                                .padding(horizontal = 30.dp, vertical = 13.dp),
                        )
                        Text(
                            text = stringResource(R.string.ritual_not_now),
                            fontFamily = LocalVoice.current.functional,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = palette.bg,
                            modifier = Modifier
                                .background(palette.ink, RoundedCornerShape(999.dp))
                                .clickable { onNotNow() }
                                .padding(horizontal = 30.dp, vertical = 13.dp),
                        )
                    }
                }
            }
        }
    }
}
