package com.makdesi.sundial.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.makdesi.sundial.SundialViewModel
import com.makdesi.sundial.theme.Grotesk
import com.makdesi.sundial.theme.Motion
import com.makdesi.sundial.theme.paletteFor
import com.makdesi.sundial.theme.reducedMotion
import kotlinx.coroutines.delay

private enum class Layer { HOME, SETTINGS }

@Composable
fun SundialRoot(viewModel: SundialViewModel) {
    val home by viewModel.home.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val ritualFlags by viewModel.ritualFlags.collectAsState()
    val pendingRitual by viewModel.pendingRitual.collectAsState()
    val palette = animatedPalette(paletteFor(home.mode))

    var layer by remember { mutableStateOf(Layer.HOME) }
    var searchOpen by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }
    var toastKey by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.toasts.collect {
            toast = it
            toastKey++
        }
    }
    LaunchedEffect(toastKey) {
        if (toast != null) {
            delay(2200)
            toast = null
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = palette.isLight
    }

    val reduced = reducedMotion(LocalContext.current)
    val layerMs = if (reduced) Motion.NEAR_INSTANT_MS else Motion.LAYER_MS

    Box(Modifier.fillMaxSize().background(palette.bg)) {
        AnimatedContent(
            targetState = layer,
            transitionSpec = {
                (fadeIn(tween(layerMs)) + scaleIn(tween(layerMs), initialScale = 0.98f))
                    .togetherWith(fadeOut(tween(layerMs)) + scaleOut(tween(layerMs), targetScale = 0.98f))
            },
            label = "layer",
        ) { current ->
            when (current) {
                Layer.HOME -> HomeScreen(
                    palette = palette,
                    home = home,
                    apps = apps,
                    ritualFlags = ritualFlags,
                    onOpen = viewModel::open,
                    onOpenSettings = { layer = Layer.SETTINGS },
                    onOpenSearch = { searchOpen = true },
                )
                Layer.SETTINGS -> SettingsScreen(
                    palette = palette,
                    onDone = { layer = Layer.HOME },
                )
            }
        }

        // Back from settings returns home; Back on home does nothing (it's the home screen).
        BackHandler(enabled = layer == Layer.SETTINGS) { layer = Layer.HOME }

        if (searchOpen) {
            SearchSheet(
                palette = palette,
                apps = apps,
                ritualFlags = ritualFlags,
                onOpen = {
                    searchOpen = false
                    viewModel.open(it)
                },
                onToggleRitual = viewModel::toggleRitualFlag,
                onDismiss = { searchOpen = false },
            )
        }

        AnimatedVisibility(
            visible = pendingRitual != null,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(300)),
        ) {
            RitualOverlay(
                palette = palette,
                onOpenForTen = viewModel::ritualOpenForTen,
                onNotNow = viewModel::ritualNotNow,
            )
        }
        BackHandler(enabled = pendingRitual != null) { viewModel.ritualNotNow() }

        // in-app toast, in the app's own voice
        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
            exit = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
        ) {
            toast?.let {
                Text(
                    text = it,
                    fontFamily = Grotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.03.em,
                    color = palette.bg,
                    modifier = Modifier
                        .background(palette.ink, RoundedCornerShape(999.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                )
            }
        }
    }
}
