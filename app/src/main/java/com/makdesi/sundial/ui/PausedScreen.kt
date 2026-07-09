package com.makdesi.sundial.ui

import android.content.ComponentName
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.makdesi.sundial.R
import com.makdesi.sundial.data.AppEntry
import com.makdesi.sundial.theme.Grotesk
import com.makdesi.sundial.theme.Serif

private val PausedBg = Color(0xFF101014)
private val PausedInk = Color(0xFFC9C7C2)
private val PausedFaint = Color(0xFF86868C)
private val PausedButton = Color(0xFFE8E6E1)

/**
 * "This is your phone without Sundial." (plan §3.6) — a plain, ordinary grid
 * of icons. Deliberately unlovely. Resume brings the light back.
 */
@Composable
fun PausedScreen(
    apps: List<AppEntry>,
    onOpen: (AppEntry) -> Unit,
    onChangeLauncher: () -> Unit,
    onResume: () -> Unit,
) {
    val packageManager = LocalContext.current.packageManager

    Box(Modifier.fillMaxSize().background(PausedBg).safeDrawingPadding()) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 28.dp)
                .padding(top = 64.dp, bottom = 24.dp),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(apps, key = { it.packageName + "/" + it.activityClassName }) { app ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onOpen(app) },
                    ) {
                        val icon = remember(app.packageName) {
                            runCatching {
                                packageManager
                                    .getActivityIcon(
                                        ComponentName(app.packageName, app.activityClassName)
                                    )
                                    .toBitmap(144, 144)
                                    .asImageBitmap()
                            }.getOrNull()
                        }
                        if (icon != null) {
                            Image(
                                bitmap = icon,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(13.dp)),
                            )
                        } else {
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .background(PausedFaint, RoundedCornerShape(13.dp)),
                            )
                        }
                        Text(
                            text = app.label.lowercase(),
                            fontFamily = Grotesk,
                            fontSize = 9.5.sp,
                            color = PausedFaint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.paused_msg),
                fontFamily = Serif,
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = PausedFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
            Text(
                text = stringResource(R.string.paused_resume),
                fontFamily = Grotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PausedBg,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PausedButton, RoundedCornerShape(999.dp))
                    .clickable { onResume() }
                    .padding(vertical = 13.dp),
            )
            Text(
                text = stringResource(R.string.paused_change_launcher),
                fontFamily = Grotesk,
                fontSize = 11.5.sp,
                color = PausedFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChangeLauncher() }
                    .padding(top = 14.dp, bottom = 4.dp),
            )
        }
    }
}
