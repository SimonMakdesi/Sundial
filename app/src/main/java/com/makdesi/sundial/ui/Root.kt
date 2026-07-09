package com.makdesi.sundial.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.makdesi.sundial.SundialViewModel
import com.makdesi.sundial.theme.Motion
import com.makdesi.sundial.theme.paletteFor
import com.makdesi.sundial.theme.reducedMotion

private enum class Layer { HOME, SETTINGS }

@Composable
fun SundialRoot(viewModel: SundialViewModel) {
    val home by viewModel.home.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val palette = animatedPalette(paletteFor(home.mode))

    var layer by remember { mutableStateOf(Layer.HOME) }
    var searchOpen by remember { mutableStateOf(false) }

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
                onOpen = {
                    searchOpen = false
                    viewModel.open(it)
                },
                onDismiss = { searchOpen = false },
            )
        }
    }
}
