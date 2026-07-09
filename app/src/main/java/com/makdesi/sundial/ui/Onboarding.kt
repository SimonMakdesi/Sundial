package com.makdesi.sundial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makdesi.sundial.R
import com.makdesi.sundial.data.AppEntry
import com.makdesi.sundial.theme.Grotesk
import com.makdesi.sundial.theme.Palette
import com.makdesi.sundial.theme.Serif

private enum class Step { WELCOME, PICK, WHISPERS, HOME }

/**
 * First-run onboarding (plan §3.9): under a minute, in the product's voice.
 * Welcome → pick apps (seeds all three modes) → offer whispers → make it home.
 */
@Composable
fun Onboarding(
    palette: Palette,
    apps: List<AppEntry>,
    suggested: List<String>,
    onOpenWhispersSettings: () -> Unit,
    onOpenHomeSettings: () -> Unit,
    onComplete: (List<String>) -> Unit,
) {
    var step by remember { mutableStateOf(Step.WELCOME) }
    val selected = remember(suggested) { suggested.toMutableStateList() }

    Box(
        modifier = Modifier.fillMaxSize().background(palette.bg).safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (step) {
                Step.WELCOME -> {
                    Text(
                        text = "☉",
                        fontFamily = Serif,
                        fontSize = 26.sp,
                        color = palette.faint,
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = Serif,
                        fontWeight = FontWeight.Light,
                        fontSize = 44.sp,
                        color = palette.ink,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                    Text(
                        text = stringResource(R.string.onboarding_tagline),
                        fontFamily = Serif,
                        fontWeight = FontWeight.Light,
                        fontStyle = FontStyle.Italic,
                        fontSize = 17.sp,
                        color = palette.faint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        text = stringResource(R.string.onboarding_welcome_line),
                        fontFamily = Grotesk,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = palette.faint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 26.dp),
                    )
                    Spacer(Modifier.height(40.dp))
                    SolidButton(palette, stringResource(R.string.onboarding_begin)) {
                        step = Step.PICK
                    }
                }

                Step.PICK -> {
                    Text(
                        text = stringResource(R.string.onboarding_pick_title),
                        fontFamily = Serif,
                        fontWeight = FontWeight.Light,
                        fontSize = 30.sp,
                        color = palette.ink,
                    )
                    Text(
                        text = stringResource(R.string.onboarding_pick_hint),
                        fontFamily = Grotesk,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = palette.faint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 10.dp, bottom = 16.dp),
                    )
                    LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 420.dp)) {
                        items(apps, key = { it.packageName + "/" + it.activityClassName }) { app ->
                            val included = app.packageName in selected
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (included) selected.remove(app.packageName)
                                        else selected.add(app.packageName)
                                    }
                                    .padding(vertical = 10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .then(
                                            if (included) Modifier.background(palette.ink, CircleShape)
                                            else Modifier.border(1.5.dp, palette.faint, CircleShape)
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (included) Text("✓", fontSize = 11.sp, color = palette.bg)
                                }
                                Text(
                                    text = app.label.lowercase(),
                                    fontFamily = Grotesk,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = if (included) palette.ink
                                    else palette.ink.copy(alpha = .38f),
                                    modifier = Modifier.padding(start = 12.dp),
                                )
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(palette.hair))
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    SolidButton(palette, stringResource(R.string.onboarding_continue)) {
                        step = Step.WHISPERS
                    }
                }

                Step.WHISPERS -> {
                    Text(
                        text = stringResource(R.string.onboarding_whispers_title),
                        fontFamily = Serif,
                        fontWeight = FontWeight.Light,
                        fontSize = 30.sp,
                        color = palette.ink,
                    )
                    Text(
                        text = stringResource(R.string.onboarding_whispers_body),
                        fontFamily = Grotesk,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = palette.faint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    Spacer(Modifier.height(40.dp))
                    GhostButton(palette, stringResource(R.string.onboarding_whispers_allow)) {
                        onOpenWhispersSettings()
                        step = Step.HOME
                    }
                    Spacer(Modifier.height(10.dp))
                    SolidButton(palette, stringResource(R.string.onboarding_whispers_later)) {
                        step = Step.HOME
                    }
                }

                Step.HOME -> {
                    Text(
                        text = stringResource(R.string.onboarding_home_title),
                        fontFamily = Serif,
                        fontWeight = FontWeight.Light,
                        fontSize = 30.sp,
                        color = palette.ink,
                    )
                    Text(
                        text = stringResource(R.string.onboarding_home_body),
                        fontFamily = Grotesk,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = palette.faint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    Spacer(Modifier.height(40.dp))
                    GhostButton(palette, stringResource(R.string.onboarding_home_set)) {
                        onOpenHomeSettings()
                    }
                    Spacer(Modifier.height(10.dp))
                    SolidButton(palette, stringResource(R.string.onboarding_done)) {
                        onComplete(selected.toList())
                    }
                }
            }
        }
    }
}

@Composable
private fun SolidButton(palette: Palette, label: String, onClick: () -> Unit) {
    Text(
        text = label,
        fontFamily = Grotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp,
        color = palette.bg,
        modifier = Modifier
            .background(palette.ink, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 30.dp, vertical = 13.dp),
    )
}

@Composable
private fun GhostButton(palette: Palette, label: String, onClick: () -> Unit) {
    Text(
        text = label,
        fontFamily = Grotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp,
        color = palette.ink,
        modifier = Modifier
            .border(1.dp, palette.hair, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 30.dp, vertical = 13.dp),
    )
}
