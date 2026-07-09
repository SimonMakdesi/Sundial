package com.makdesi.sundial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// Dawn paper, from the design contract's PALETTES. The full theme system lands in M2.
private val DawnBg = Color(0xFFF3EEE6)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlankHome()
        }
    }
}

@Composable
private fun BlankHome() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DawnBg)
    )
}
