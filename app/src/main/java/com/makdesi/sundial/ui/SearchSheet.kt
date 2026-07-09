package com.makdesi.sundial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.makdesi.sundial.R
import com.makdesi.sundial.data.AppEntry
import com.makdesi.sundial.theme.Grotesk
import com.makdesi.sundial.theme.Palette
import com.makdesi.sundial.theme.Serif

/**
 * Swipe-up sheet: everything is findable, nothing is blocked (plan §3.4).
 * Apps outside the current mode carry a faint `asleep` tag but launch normally.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSheet(
    palette: Palette,
    apps: List<AppEntry>,
    ritualFlags: Set<String>,
    awakePackages: Set<String>,
    align: com.makdesi.sundial.data.Side,
    onOpen: (AppEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    val right = align == com.makdesi.sundial.data.Side.RIGHT
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.bg,
        contentColor = palette.ink,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 14.dp, bottom = 14.dp)
                    .width(72.dp)
                    .height(4.dp)
                    .background(palette.hair, RoundedCornerShape(999.dp)),
            )
        },
    ) {
        Column(
            Modifier
                .fillMaxHeight(0.86f)
                .padding(horizontal = 32.dp)
                .imePadding(),
        ) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = Grotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = palette.ink,
                    textAlign = if (right) TextAlign.End else TextAlign.Start,
                ),
                cursorBrush = SolidColor(palette.ink),
                decorationBox = { inner ->
                    Column {
                        Box(Modifier.padding(vertical = 10.dp)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.search_placeholder),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )

            val results = apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
            if (results.isEmpty()) {
                Text(
                    text = stringResource(R.string.search_empty),
                    fontFamily = Serif,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    color = palette.faint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp),
                )
            } else {
                LazyColumn(Modifier.padding(top = 8.dp)) {
                    items(results, key = { it.packageName + "/" + it.activityClassName }) { app ->
                        Column(Modifier.clickable { onOpen(app) }) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = if (right) Arrangement.End
                                else Arrangement.Start,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
                            ) {
                                val asleep = app.packageName !in awakePackages
                                val flagged = app.packageName in ritualFlags

                                @Composable
                                fun androidx.compose.foundation.layout.RowScope.name() = Text(
                                    text = app.label.lowercase(),
                                    fontFamily = Grotesk,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp,
                                    letterSpacing = 0.01.em,
                                    color = palette.ink,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )

                                @Composable
                                fun notes() {
                                    if (asleep) {
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = stringResource(R.string.search_asleep),
                                            fontFamily = Grotesk,
                                            fontSize = 12.sp,
                                            letterSpacing = 0.03.em,
                                            color = palette.faint,
                                        )
                                    }
                                    if (flagged) {
                                        Spacer(Modifier.width(10.dp))
                                        Box(
                                            Modifier
                                                .size(5.dp)
                                                .align(Alignment.CenterVertically)
                                                .background(palette.faint, CircleShape),
                                        )
                                    }
                                }

                                if (right) {
                                    notes()
                                    if (asleep || flagged) Spacer(Modifier.width(10.dp))
                                    name()
                                } else {
                                    name()
                                    notes()
                                }
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(palette.hair))
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
