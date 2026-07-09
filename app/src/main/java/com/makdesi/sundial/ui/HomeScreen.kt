package com.makdesi.sundial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makdesi.sundial.data.AppEntry
import kotlinx.coroutines.flow.StateFlow

// Dawn palette placeholders; the real theme system arrives in M2.
private val DawnBg = Color(0xFFF3EEE6)
private val DawnInk = Color(0xFF2A2620)

@Composable
fun HomeScreen(
    appsFlow: StateFlow<List<AppEntry>>,
    onOpen: (AppEntry) -> Unit,
) {
    val apps by appsFlow.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DawnBg)
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp)
                .padding(horizontal = 28.dp),
        ) {
            items(apps, key = { it.packageName + "/" + it.activityClassName }) { app ->
                Text(
                    text = app.label.lowercase(),
                    color = DawnInk,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { onOpen(app) }
                        .padding(vertical = 10.dp),
                )
            }
        }
    }
}
