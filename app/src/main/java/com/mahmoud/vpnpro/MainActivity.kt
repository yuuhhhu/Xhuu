package com.mahmoud.vpnpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var dark by remember { mutableStateOf(true) }
            AppTheme(dark) { App(dark) { dark = it } }
        }
    }
}
