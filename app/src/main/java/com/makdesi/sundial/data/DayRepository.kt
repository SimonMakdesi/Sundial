package com.makdesi.sundial.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.makdesi.sundial.domain.Mode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ModeConfig(
    val apps: List<String> = emptyList(), // package names, in order of addition
    val intention: String = "",
)

/** Theme: follow the sun, or lock one palette. Rhythm always follows time. */
enum class ThemeChoice { SUN, DAWN, NOON, DUSK }

/** Home/search mirroring for one-handed reach. */
enum class Side { LEFT, RIGHT }

data class Appearance(
    val theme: ThemeChoice = ThemeChoice.SUN,
    val align: Side = Side.LEFT,
)

/**
 * Per-mode app lists and intentions, persisted in DataStore (plan §3.6).
 * Lists are stored as ordered comma-joined strings — order of addition is the
 * v1 sort (drag-to-reorder is a Later item).
 */
class DayRepository(private val context: Context, private val scope: CoroutineScope) {

    private fun appsKey(mode: Mode) = stringPreferencesKey("apps_${mode.name.lowercase()}")
    private fun intentionKey(mode: Mode) = stringPreferencesKey("intention_${mode.name.lowercase()}")
    private val seededKey = booleanPreferencesKey("seeded")
    private val themeKey = stringPreferencesKey("theme")
    private val alignKey = stringPreferencesKey("align")

    val appearance: StateFlow<Appearance> = context.sundialDataStore.data
        .map { prefs ->
            Appearance(
                theme = prefs[themeKey]?.let { runCatching { ThemeChoice.valueOf(it) }.getOrNull() }
                    ?: ThemeChoice.SUN,
                align = prefs[alignKey]?.let { runCatching { Side.valueOf(it) }.getOrNull() }
                    ?: Side.LEFT,
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, Appearance())

    fun setTheme(theme: ThemeChoice) {
        scope.launch {
            context.sundialDataStore.edit { it[themeKey] = theme.name }
        }
    }

    fun setAlign(align: Side) {
        scope.launch {
            context.sundialDataStore.edit { it[alignKey] = align.name }
        }
    }

    val settings: StateFlow<Map<Mode, ModeConfig>> = context.sundialDataStore.data
        .map { prefs ->
            Mode.entries.associateWith { mode ->
                ModeConfig(
                    apps = prefs[appsKey(mode)]?.split(',')?.filter { it.isNotEmpty() }
                        ?: emptyList(),
                    intention = prefs[intentionKey(mode)] ?: "",
                )
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, Mode.entries.associateWith { ModeConfig() })

    fun toggleApp(mode: Mode, packageName: String) {
        scope.launch {
            context.sundialDataStore.edit { prefs ->
                val current = prefs[appsKey(mode)]?.split(',')?.filter { it.isNotEmpty() }
                    ?: emptyList()
                val next = if (packageName in current) current - packageName
                else current + packageName
                prefs[appsKey(mode)] = next.joinToString(",")
            }
        }
    }

    fun setIntention(mode: Mode, text: String) {
        scope.launch {
            context.sundialDataStore.edit { prefs ->
                prefs[intentionKey(mode)] = text.take(60)
            }
        }
    }

    /**
     * First run: seed every mode with gentle defaults if present on the device
     * (messages, camera, phone — plan §3.9). The user adjusts in the editor.
     */
    fun seedIfFirstRun(installedPackages: List<String>) {
        scope.launch {
            val prefs = context.sundialDataStore.data.first()
            if (prefs[seededKey] == true) return@launch
            val wanted = installedPackages.filter { pkg ->
                listOf("messag", "camera", "dialer", "phone").any { pkg.contains(it) }
            }.distinct()
            context.sundialDataStore.edit { p ->
                Mode.entries.forEach { mode ->
                    if (p[appsKey(mode)] == null) p[appsKey(mode)] = wanted.joinToString(",")
                }
                p[seededKey] = true
            }
        }
    }
}
