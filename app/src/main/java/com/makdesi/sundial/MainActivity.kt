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
import com.makdesi.sundial.data.DayRepository
import com.makdesi.sundial.data.ModeConfig
import com.makdesi.sundial.data.NotificationWhisperService
import com.makdesi.sundial.data.WeatherCity
import com.makdesi.sundial.data.WeatherRepository
import com.makdesi.sundial.domain.Mode
import com.makdesi.sundial.domain.ModeEngine
import com.makdesi.sundial.domain.RitualGate
import com.makdesi.sundial.system.AlarmScheduler
import com.makdesi.sundial.ui.HomeState
import com.makdesi.sundial.ui.SundialRoot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class ClockState(
    val mode: Mode,
    val time: String,
    val meridiem: String?,
    val dateline: String,
    val dateShort: String,
    val dayFraction: Float,
    val nextMode: Mode,
    val nextModeAt: String,
)

class SundialViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application, viewModelScope)
    private val ritualGate = RitualGate(application, viewModelScope)
    val day = DayRepository(application, viewModelScope)
    val weather = WeatherRepository(application, viewModelScope)

    val apps = repository.apps
    val ritualFlags = ritualGate.flags
    val daySettings = day.settings
    val appearance = day.appearance

    /** The app waiting behind the breath ritual, if any. */
    val pendingRitual = MutableStateFlow<AppEntry?>(null)

    /** In-app toast lines ("Good call.", "Welcome back."). */
    val toasts = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val onboarded = day.onboarded
    val paused = day.paused

    init {
        // The lock screen mirrors the current face/theme; guarded, so
        // unchanged looks cost nothing (plus the boundary receivers re-sync).
        viewModelScope.launch {
            appearance.collect {
                com.makdesi.sundial.data.WallpaperSync.sync(getApplication())
            }
        }
    }

    fun completeOnboarding(selected: List<String>) = day.completeOnboarding(selected)

    fun pause() = day.setPaused(true)

    fun resume() {
        day.setPaused(false)
        toasts.tryEmit(getApplication<Application>().getString(R.string.toast_welcome_back))
    }

    /** Ticks on the minute while the UI is visible; silent otherwise (zero polling in background). */
    private val minuteTicker = flow {
        while (true) {
            emit(Unit)
            val now = ZonedDateTime.now()
            delay(60_000L - (now.second * 1000L + now.nano / 1_000_000L))
        }
    }

    private val clock = merge(minuteTicker, ModeEngine.events).map {
        weather.maybeFetch() // throttled internally; no-op (zero network) when weather is off
        buildClockState()
    }

    val home = combine(
        clock, apps, daySettings, NotificationWhisperService.counts, weather.state,
    ) { clock, installed, day, counts, weatherState ->
        val config = day[clock.mode] ?: ModeConfig()
        val byPackage = installed.associateBy { it.packageName }
        val modeApps = config.apps.mapNotNull { byPackage[it] }
        HomeState(
            mode = clock.mode,
            time = clock.time,
            meridiem = clock.meridiem,
            dateline = clock.dateline,
            dateShort = clock.dateShort,
            dayFraction = clock.dayFraction,
            temperature = weatherState.temperature,
            nextMode = clock.nextMode,
            nextModeAt = clock.nextModeAt,
            modeApps = modeApps,
            intention = config.intention,
            asleepCount = installed.size - modeApps.size,
            awakePackages = modeApps.map { it.packageName }.toSet(),
            counts = counts,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialHomeState())

    private fun initialHomeState(): HomeState {
        val c = buildClockState()
        return HomeState(
            mode = c.mode, time = c.time, meridiem = c.meridiem, dateline = c.dateline,
            dateShort = c.dateShort, dayFraction = c.dayFraction, temperature = null,
            nextMode = c.nextMode, nextModeAt = c.nextModeAt,
            modeApps = emptyList(), intention = "", asleepCount = 0, awakePackages = emptySet(),
            counts = emptyMap(),
        )
    }

    private fun buildClockState(): ClockState {
        val context = getApplication<Application>()
        val locale = Locale.getDefault()
        val now = ZonedDateTime.now()
        val is24 = DateFormat.is24HourFormat(context)

        val timePattern = if (is24) DateFormat.getBestDateTimePattern(locale, "Hm") else "h:mm"
        val datePattern = DateFormat.getBestDateTimePattern(locale, "EEEEMMMMd")
        val boundary = ModeEngine.nextBoundary(now)
        val boundaryPattern = if (is24) timePattern else "h:mm a"

        val dateShortPattern = DateFormat.getBestDateTimePattern(locale, "EEEddMMM")
        return ClockState(
            mode = ModeEngine.modeAt(now),
            time = now.format(DateTimeFormatter.ofPattern(timePattern, locale)),
            meridiem = if (is24) null
            else now.format(DateTimeFormatter.ofPattern("a", locale)).lowercase(locale),
            dateline = now.format(DateTimeFormatter.ofPattern(datePattern, locale)),
            dateShort = now.format(DateTimeFormatter.ofPattern(dateShortPattern, locale)),
            dayFraction = (now.hour * 60 + now.minute) / 1440f,
            nextMode = ModeEngine.nextSpan(now).mode,
            nextModeAt = boundary.format(DateTimeFormatter.ofPattern(boundaryPattern, locale))
                .lowercase(locale),
        )
    }

    fun open(app: AppEntry) {
        if (ritualGate.shouldAsk(app.packageName)) {
            pendingRitual.value = app
        } else {
            repository.launch(app)
        }
    }

    /** Paused grid launches: the plain phone, no rituals. */
    fun openPlain(app: AppEntry) = repository.launch(app)

    fun ritualOpenForTen() {
        pendingRitual.value?.let {
            ritualGate.openWindow(it.packageName)
            repository.launch(it)
        }
        pendingRitual.value = null
    }

    fun ritualNotNow() {
        pendingRitual.value = null
        toasts.tryEmit(getApplication<Application>().getString(R.string.toast_good_call))
    }

    fun toggleRitualFlag(packageName: String) = ritualGate.toggle(packageName)

    fun toggleModeApp(mode: Mode, packageName: String) = day.toggleApp(mode, packageName)

    fun setIntention(mode: Mode, text: String) = day.setIntention(mode, text)

    fun setTheme(theme: com.makdesi.sundial.data.ThemeChoice) = day.setTheme(theme)

    fun setAlign(align: com.makdesi.sundial.data.Side) = day.setAlign(align)

    fun setFace(face: com.makdesi.sundial.data.Face) = day.setFace(face)

    /**
     * The location ladder (plan §3.8): granted → nearest city via last known
     * coarse location; declined or unavailable → pre-fill from the timezone.
     * The result is always visible and editable in settings — self-correcting.
     */
    fun enableWeather(locationGranted: Boolean) {
        weather.setEnabled(true)
        viewModelScope.launch {
            if (weather.state.value.cityName.isNotEmpty()) return@launch // keep the chosen city
            val app = getApplication<Application>()
            var city: WeatherCity? = null
            if (locationGranted) {
                city = runCatching {
                    val lm = app.getSystemService(android.location.LocationManager::class.java)
                    val location = lm.getLastKnownLocation(android.location.LocationManager.PASSIVE_PROVIDER)
                        ?: lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                    location?.let {
                        @Suppress("DEPRECATION")
                        val address = android.location.Geocoder(app)
                            .getFromLocation(it.latitude, it.longitude, 1)?.firstOrNull()
                        address?.locality?.let { name ->
                            WeatherCity(name, address.countryName ?: "", it.latitude, it.longitude)
                        }
                    }
                }.getOrNull()
            }
            if (city == null) {
                val timezoneCity = java.util.TimeZone.getDefault().id
                    .substringAfterLast('/').replace('_', ' ')
                city = weather.searchCities(timezoneCity).firstOrNull()
            }
            city?.let { weather.setCity(it) }
        }
    }

    fun disableWeather() = weather.setEnabled(false)

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
            SundialRoot(viewModel)
        }
    }
}
