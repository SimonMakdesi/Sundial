package com.makdesi.sundial.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.makdesi.sundial.R
import com.makdesi.sundial.theme.Grotesk
import com.makdesi.sundial.theme.Palette
import com.makdesi.sundial.theme.Serif

/**
 * "Your day" — reached by long-press. M3 ships the shell (title + Done);
 * mode cards, appearance, and system rows land in M5/M6.
 */
@Composable
fun SettingsScreen(
    palette: Palette,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .padding(horizontal = 32.dp, vertical = 44.dp),
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
            Text(
                text = stringResource(R.string.settings_coming),
                fontFamily = Serif,
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = palette.faint,
                modifier = Modifier.padding(top = 28.dp),
            )
        }
    }
}
