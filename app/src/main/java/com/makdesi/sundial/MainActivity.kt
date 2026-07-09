package com.makdesi.sundial

import android.app.Application
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.makdesi.sundial.data.AppEntry
import com.makdesi.sundial.data.AppRepository
import com.makdesi.sundial.domain.Mode
import com.makdesi.sundial.domain.ModeEngine
import com.makdesi.sundial.system.AlarmScheduler
import com.makdesi.sundial.ui.HomeScreen
import com.makdesi.sundial.ui.HomeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class SundialViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application, viewModelScope)
    val apps = repository.apps

    /** Ticks on the minute while the UI is visible; silent otherwise (zero polling in background). */
    private val minuteTicker = flow {
        while (true) {
            emit(Unit)
            val now = ZonedDateTime.now()
            delay(60_000L - (now.second * 1000L + now.nano / 1_000_000L))
        }
    }

    val home = merge(minuteTicker, ModeEngine.events)
        .map { buildHomeState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), buildHomeState())

    private fun buildHomeState(): HomeState {
        val context = getApplication<Application>()
        val locale = Locale.getDefault()
        val now = ZonedDateTime.now()
        val is24 = DateFormat.is24HourFormat(context)

        val timePattern = if (is24) DateFormat.getBestDateTimePattern(locale, "Hm") else "h:mm"
        val datePattern = DateFormat.getBestDateTimePattern(locale, "EEEEMMMMd")
        val boundary = ModeEngine.nextBoundary(now)
        val boundaryPattern = if (is24) timePattern else "h:mm a"

        return HomeState(
            mode = ModeEngine.modeAt(now),
            time = now.format(DateTimeFormatter.ofPattern(timePattern, locale)),
            meridiem = if (is24) null
            else now.format(DateTimeFormatter.ofPattern("a", locale)).lowercase(locale),
            dateline = now.format(DateTimeFormatter.ofPattern(datePattern, locale)),
            nextMode = ModeEngine.nextSpan(now).mode,
            nextModeAt = boundary.format(DateTimeFormatter.ofPattern(boundaryPattern, locale))
                .lowercase(locale),
        )
    }

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
        AlarmScheduler.scheduleNext(this)
        setContent {
            HomeScreen(
                homeFlow = viewModel.home,
                appsFlow = viewModel.apps,
                onOpen = viewModel::open,
            )
        }
    }
}
