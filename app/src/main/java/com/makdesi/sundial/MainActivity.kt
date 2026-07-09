package com.makdesi.sundial

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.makdesi.sundial.data.AppEntry
import com.makdesi.sundial.data.AppRepository
import com.makdesi.sundial.ui.HomeScreen

class SundialViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application, viewModelScope)
    val apps = repository.apps

    fun open(app: AppEntry) = repository.launch(app)

    override fun onCleared() {
        repository.dispose()
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: SundialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeScreen(appsFlow = viewModel.apps, onOpen = viewModel::open)
        }
    }
}
