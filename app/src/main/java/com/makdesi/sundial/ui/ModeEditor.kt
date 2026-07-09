package com.makdesi.sundial.ui

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.makdesi.sundial.R
import com.makdesi.sundial.data.AppEntry
import com.makdesi.sundial.data.ModeConfig
import com.makdesi.sundial.domain.Mode
import com.makdesi.sundial.theme.Grotesk
import com.makdesi.sundial.theme.Palette
import com.makdesi.sundial.theme.Serif

/**
 * The mode editor (plan §3.6): intention, then a searchable picker where the
 * circle check includes an app in this mode and ☉ flags the global ritual.
 * The soft cap warns above 8 apps — it never blocks.
 */
@Composable
fun ModeEditor(
    palette: Palette,
    mode: Mode,
    config: ModeConfig,
    apps: List<AppEntry>,
    ritualFlags: Set<String>,
    onToggleApp: (String) -> Unit,
    onToggleRitual: (String) -> Unit,
    onIntention: (String) -> Unit,
    onBack: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    // Local echo of the intention so typing is immediate; DataStore follows.
    var intention by rememberSaveable(mode) { mutableStateOf(config.intention) }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding(),
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
                    text = mode.label(),
                    fontFamily = Serif,
                    fontWeight = FontWeight.Light,
                    fontSize = 30.sp,
                    color = palette.ink,
                )
                Text(
                    text = stringResource(R.string.editor_back),
                    fontFamily = Grotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.05.em,
                    color = palette.faint,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                )
            }

            Text(
                text = mode.spanLabel(),
                fontFamily = Grotesk,
                fontSize = 13.sp,
                color = palette.faint,
                modifier = Modifier.padding(top = 8.dp),
            )

            BasicTextField(
                value = intention,
                onValueChange = {
                    intention = it.take(60)
                    onIntention(intention)
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = Serif,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    fontSize = 15.sp,
                    color = palette.ink,
                ),
                cursorBrush = SolidColor(palette.ink),
                decorationBox = { inner ->
                    Column {
                        Box(Modifier.padding(vertical = 10.dp)) {
                            if (intention.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.editor_intention_hint),
                                    fontFamily = Serif,
                                    fontWeight = FontWeight.Light,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 15.sp,
                                    color = palette.faint.copy(alpha = palette.faint.alpha * .6f),
                                )
                            }
                            inner()
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(palette.hair))
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            )

            Text(
                text = stringResource(R.string.editor_legend),
                fontFamily = Grotesk,
                fontSize = 11.sp,
                lineHeight = 17.sp,
                color = palette.faint,
                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
            )

            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = Grotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = palette.ink,
                ),
                cursorBrush = SolidColor(palette.ink),
                decorationBox = { inner ->
                    Column {
                        Box(Modifier.padding(vertical = 10.dp)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.editor_search_hint),
                                    fontFamily = Grotesk,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = palette.faint.copy(alpha = palette.faint.alpha * .6f),
                                )
                            }
                            inner()
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(palette.hair))
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )

            if (config.apps.size > 8) {
                Text(
                    text = stringResource(R.string.editor_fullness, config.apps.size),
                    fontFamily = Serif,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.5.sp,
                    color = palette.faint,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            val results = apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
            LazyColumn(Modifier.padding(top = 4.dp)) {
                items(results, key = { it.packageName + "/" + it.activityClassName }) { app ->
                    val included = app.packageName in config.apps
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        ) {
                            // circle check: include in this mode
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .then(
                                        if (included) Modifier.background(palette.ink, CircleShape)
                                        else Modifier.border(1.5.dp, palette.faint, CircleShape)
                                    )
                                    .clickable { onToggleApp(app.packageName) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (included) {
                                    Text(
                                        text = "✓",
                                        fontSize = 11.sp,
                                        color = palette.bg,
                                    )
                                }
                            }
                            Text(
                                text = app.label.lowercase(),
                                fontFamily = Grotesk,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = if (included) palette.ink
                                else palette.ink.copy(alpha = .38f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToggleApp(app.packageName) }
                                    .padding(horizontal = 12.dp),
                            )
                            // ☉: the global breath-ritual flag
                            Text(
                                text = "☉",
                                fontFamily = Serif,
                                fontSize = 15.sp,
                                color = if (app.packageName in ritualFlags) palette.ink
                                else palette.faint.copy(alpha = palette.faint.alpha * .35f),
                                modifier = Modifier
                                    .clickable { onToggleRitual(app.packageName) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(palette.hair))
                    }
                }
            }
        }
    }
}
