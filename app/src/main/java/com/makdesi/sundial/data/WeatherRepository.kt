package com.makdesi.sundial.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class WeatherCity(val name: String, val country: String, val lat: Double, val lon: Double)

data class WeatherState(
    val enabled: Boolean = false,
    val cityName: String = "",
    /** "18°" when fresh (< 6h) — or null: offline, stale, disabled. Never an error state. */
    val temperature: String? = null,
)

/**
 * The weather whisper (plan §3.8): one number in the dateline, fetched from
 * Open-Meteo (keyless) at most every few hours, cached, failing silently.
 * With weather off this class makes zero network calls — enforced by the
 * `enabled` guard at the single fetch entry point.
 */
class WeatherRepository(private val context: Context, private val scope: CoroutineScope) {

    private val enabledKey = booleanPreferencesKey("weather_on")
    private val cityNameKey = stringPreferencesKey("weather_city")
    private val latKey = doublePreferencesKey("weather_lat")
    private val lonKey = doublePreferencesKey("weather_lon")
    private val tempKey = stringPreferencesKey("weather_temp")
    private val fetchedAtKey = longPreferencesKey("weather_fetched_at")

    private val staleAfterMs = 6 * 60 * 60 * 1000L
    private val throttleMs = 4 * 60 * 60 * 1000L

    val state: StateFlow<WeatherState> = context.sundialDataStore.data
        .map { prefs ->
            val fresh = (prefs[fetchedAtKey] ?: 0L).let {
                System.currentTimeMillis() - it < staleAfterMs
            }
            WeatherState(
                enabled = prefs[enabledKey] ?: false,
                cityName = prefs[cityNameKey] ?: "",
                temperature = if (prefs[enabledKey] == true && fresh) prefs[tempKey] else null,
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, WeatherState())

    fun setEnabled(enabled: Boolean) {
        scope.launch {
            context.sundialDataStore.edit { it[enabledKey] = enabled }
            if (enabled) maybeFetch(force = true)
        }
    }

    fun setCity(city: WeatherCity) {
        scope.launch {
            context.sundialDataStore.edit {
                it[cityNameKey] = city.name
                it[latKey] = city.lat
                it[lonKey] = city.lon
            }
            maybeFetch(force = true)
        }
    }

    /** Throttled fetch — call freely (the launcher resumes on every unlock). */
    fun maybeFetch(force: Boolean = false) {
        scope.launch {
            val prefs = context.sundialDataStore.data.first()
            if (prefs[enabledKey] != true) return@launch // weather off: zero network
            val lat = prefs[latKey] ?: return@launch
            val lon = prefs[lonKey] ?: return@launch
            val last = prefs[fetchedAtKey] ?: 0L
            if (!force && System.currentTimeMillis() - last < throttleMs) return@launch

            val unit = if (Locale.getDefault().country in listOf("US", "LR", "MM"))
                "fahrenheit" else "celsius"
            val temp = withContext(Dispatchers.IO) {
                runCatching {
                    val json = URL(
                        "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                            "&current=temperature_2m&temperature_unit=$unit"
                    ).fetchJson()
                    json.getJSONObject("current").getDouble("temperature_2m")
                }.getOrNull() // offline or bad response: fail silently
            } ?: return@launch

            context.sundialDataStore.edit {
                it[tempKey] = "${temp.toInt()}°"
                it[fetchedAtKey] = System.currentTimeMillis()
            }
        }
    }

    /** Open-Meteo geocoding — used by the settings city field and the ladder. */
    suspend fun searchCities(query: String): List<WeatherCity> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        runCatching {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val json = URL(
                "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=5"
            ).fetchJson()
            val results = json.optJSONArray("results") ?: return@runCatching emptyList()
            (0 until results.length()).map { i ->
                val c = results.getJSONObject(i)
                WeatherCity(
                    name = c.getString("name"),
                    country = c.optString("country", ""),
                    lat = c.getDouble("latitude"),
                    lon = c.getDouble("longitude"),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun URL.fetchJson(): JSONObject {
        val connection = openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            JSONObject(connection.inputStream.bufferedReader().readText())
        } finally {
            connection.disconnect()
        }
    }
}
