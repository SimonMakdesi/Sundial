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

private enum class Layer { HOME, SETTINGS, EDIT }

@Composable
fun SundialRoot(viewModel: SundialViewModel) {
    val home by viewModel.home.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val daySettings by viewModel.daySettings.collectAsState()
    val appearance by viewModel.appearance.collectAsState()
    val ritualFlags by viewModel.ritualFlags.collectAsState()
    val pendingRitual by viewModel.pendingRitual.collectAsState()

    // A fixed theme locks the palette; the rhythm (apps, intention, footer)
    // keeps following the clock regardless (plan §3.6).
    val paletteMode = when (appearance.theme) {
        com.makdesi.sundial.data.ThemeChoice.SUN -> home.mode
        com.makdesi.sundial.data.ThemeChoice.DAWN -> com.makdesi.sundial.domain.Mode.MORNING
        com.makdesi.sundial.data.ThemeChoice.NOON -> com.makdesi.sundial.domain.Mode.DAY
        com.makdesi.sundial.data.ThemeChoice.DUSK -> com.makdesi.sundial.domain.Mode.EVENING
    }
    val palette = animatedPalette(paletteFor(paletteMode))

    var layer by remember { mutableStateOf(Layer.HOME) }
    var editMode by remember { mutableStateOf(com.makdesi.sundial.domain.Mode.MORNING) }
    var searchOpen by remember { mutableStateOf(false) }

    val weatherState by viewModel.weather.state.collectAsState()

    val context = LocalContext.current
    val whispersOn = remember(layer) {
        com.makdesi.sundial.data.NotificationWhisperService.isEnabled(context)
    }
    val isDefaultLauncher = remember(layer) {
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
            .addCategory(android.content.Intent.CATEGORY_HOME)
        context.packageManager
            .resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName == context.packageName
    }
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
                    ritualFlags = ritualFlags,
                    align = appearance.align,
                    onOpen = viewModel::open,
                    onOpenSettings = { layer = Layer.SETTINGS },
                    onOpenSearch = { searchOpen = true },
                )
                Layer.SETTINGS -> SettingsScreen(
                    palette = palette,
                    daySettings = daySettings,
                    appearance = appearance,
                    weather = weatherState,
                    whispersOn = whispersOn,
                    installedApps = apps,
                    isDefaultLauncher = isDefaultLauncher,
                    onEditMode = {
                        editMode = it
                        layer = Layer.EDIT
                    },
                    onTheme = viewModel::setTheme,
                    onAlign = viewModel::setAlign,
                    onEnableWeather = viewModel::enableWeather,
                    onDisableWeather = viewModel::disableWeather,
                    onSearchCities = { viewModel.weather.searchCities(it) },
                    onSetCity = { viewModel.weather.setCity(it) },
                    onDone = { layer = Layer.HOME },
                )
                Layer.EDIT -> ModeEditor(
                    palette = palette,
                    mode = editMode,
                    config = daySettings[editMode] ?: com.makdesi.sundial.data.ModeConfig(),
                    apps = apps,
                    ritualFlags = ritualFlags,
                    onToggleApp = { viewModel.toggleModeApp(editMode, it) },
                    onToggleRitual = viewModel::toggleRitualFlag,
                    onIntention = { viewModel.setIntention(editMode, it) },
                    onBack = { layer = Layer.SETTINGS },
                )
            }
        }

        // Back walks the layers home; Back on home does nothing (it's the home screen).
        BackHandler(enabled = layer != Layer.HOME) {
            layer = if (layer == Layer.EDIT) Layer.SETTINGS else Layer.HOME
        }

        if (searchOpen) {
            SearchSheet(
                palette = palette,
                apps = apps,
                ritualFlags = ritualFlags,
                awakePackages = home.awakePackages,
                align = appearance.align,
                onOpen = {
                    searchOpen = false
                    viewModel.open(it)
                },
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
