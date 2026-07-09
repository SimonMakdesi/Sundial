package com.makdesi.sundial.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.makdesi.sundial.R
import com.makdesi.sundial.data.AppEntry
import com.makdesi.sundial.data.Appearance
import com.makdesi.sundial.data.ModeConfig
import com.makdesi.sundial.data.Side
import com.makdesi.sundial.data.ThemeChoice
import com.makdesi.sundial.domain.Mode
import com.makdesi.sundial.theme.Grotesk
import com.makdesi.sundial.theme.Palette
import com.makdesi.sundial.theme.Serif
import com.makdesi.sundial.theme.paletteFor

/** "Your day" (plan §3.6): mode cards, then system rows. Appearance arrives in M6. */
@Composable
fun SettingsScreen(
    palette: Palette,
    daySettings: Map<Mode, ModeConfig>,
    appearance: Appearance,
    installedApps: List<AppEntry>,
    isDefaultLauncher: Boolean,
    onEditMode: (Mode) -> Unit,
    onTheme: (ThemeChoice) -> Unit,
    onAlign: (Side) -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val labels = installedApps.associate { it.packageName to it.label.lowercase() }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .padding(horizontal = 32.dp)
                .padding(top = 44.dp, bottom = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    fontFamily = Serif,
                    fontWeight = FontWeight.Light,
                    fontSize = 30.sp,
                    color = palette.ink,
                )
                Text(
                    text = stringResource(R.string.settings_done),
                    fontFamily = Grotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.05.em,
                    color = palette.faint,
                    modifier = Modifier
                        .clickable { onDone() }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                )
            }

            Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 20.dp)) {
                SectionLabel(stringResource(R.string.settings_your_modes), palette)

                Mode.entries.forEach { mode ->
                    val config = daySettings[mode] ?: ModeConfig()
                    ModeCard(
                        palette = palette,
                        mode = mode,
                        config = config,
                        labels = labels,
                        onClick = { onEditMode(mode) },
                    )
                }

                SectionLabel(stringResource(R.string.settings_appearance), palette)

                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(
                        text = stringResource(R.string.settings_theme),
                        fontFamily = Grotesk,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = palette.ink,
                    )
                    Segmented(
                        palette = palette,
                        options = listOf(
                            ThemeChoice.SUN to stringResource(R.string.theme_sun),
                            ThemeChoice.DAWN to stringResource(R.string.theme_dawn),
                            ThemeChoice.NOON to stringResource(R.string.theme_noon),
                            ThemeChoice.DUSK to stringResource(R.string.theme_dusk),
                        ),
                        selected = appearance.theme,
                        onSelect = onTheme,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(palette.hair))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_alignment),
                        fontFamily = Grotesk,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = palette.ink,
                    )
                    Segmented(
                        palette = palette,
                        options = listOf(
                            Side.LEFT to stringResource(R.string.align_left),
                            Side.RIGHT to stringResource(R.string.align_right),
                        ),
                        selected = appearance.align,
                        onSelect = onAlign,
                    )
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(palette.hair))

                SectionLabel(stringResource(R.string.settings_system), palette)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_default_launcher),
                        fontFamily = Grotesk,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = palette.ink,
                    )
                    Text(
                        text = stringResource(
                            if (isDefaultLauncher) R.string.settings_default_yes
                            else R.string.settings_default_no
                        ),
                        fontFamily = Grotesk,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.05.em,
                        color = palette.faint,
                    )
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(palette.hair))
            }
        }
    }
}

@Composable
private fun <T> Segmented(
    palette: Palette,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .border(1.dp, palette.hair, RoundedCornerShape(999.dp))
            .padding(2.dp),
    ) {
        options.forEach { (value, label) ->
            val on = value == selected
            Text(
                text = label,
                fontFamily = Grotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.5.sp,
                letterSpacing = 0.03.em,
                color = if (on) palette.bg else palette.faint,
                modifier = Modifier
                    .background(
                        if (on) palette.ink else androidx.compose.ui.graphics.Color.Transparent,
                        RoundedCornerShape(999.dp),
                    )
                    .clickable { onSelect(value) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, palette: Palette) {
    Text(
        text = text.uppercase(),
        fontFamily = Grotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.12.em,
        color = palette.faint,
        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
    )
}

@Composable
private fun ModeCard(
    palette: Palette,
    mode: Mode,
    config: ModeConfig,
    labels: Map<String, String>,
    onClick: () -> Unit,
) {
    val band = paletteFor(mode)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .border(1.dp, palette.hair, RoundedCornerShape(18.dp))
            .clickable { onClick() },
    ) {
        // horizon band strip along the top edge
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colorStops = band.horizonStops
                            .zip(band.horizon) { stop, color -> stop to color }
                            .toTypedArray()
                    ),
                    RoundedCornerShape(2.dp),
                ),
        )
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = mode.label(),
                    fontFamily = Serif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 19.sp,
                    color = palette.ink,
                )
                Text(
                    text = mode.spanLabel(),
                    fontFamily = Grotesk,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.05.em,
                    color = palette.faint,
                )
            }
            val names = config.apps.mapNotNull { labels[it] }
            Text(
                text = if (names.isEmpty()) stringResource(R.string.mode_card_empty)
                else names.joinToString(" · "),
                fontFamily = Grotesk,
                fontSize = 12.5.sp,
                lineHeight = 20.sp,
                color = palette.faint,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (config.intention.isNotBlank()) {
                Text(
                    text = "“${config.intention}”",
                    fontFamily = Serif,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    color = palette.ink,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
