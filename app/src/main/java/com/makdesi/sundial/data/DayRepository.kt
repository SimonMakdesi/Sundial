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

/** Editions — the same product wearing different identities (two-faces demo). */
enum class Face { SIGNATURE, INSTRUMENT }

data class Appearance(
    val theme: ThemeChoice = ThemeChoice.SUN,
    val align: Side = Side.LEFT,
    val face: Face = Face.SIGNATURE,
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
    private val faceKey = stringPreferencesKey("face")
    private val onboardedKey = booleanPreferencesKey("onboarded")
    private val pausedKey = booleanPreferencesKey("paused")

    /** null while DataStore loads — the UI holds a blank frame, never a flash of onboarding. */
    val onboarded: StateFlow<Boolean?> = context.sundialDataStore.data
        .map { prefs ->
            // Installs that predate onboarding (already seeded) are considered onboarded.
            (prefs[onboardedKey] ?: false) || (prefs[seededKey] ?: false)
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    val paused: StateFlow<Boolean> = context.sundialDataStore.data
        .map { it[pausedKey] ?: false }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /**
     * Onboarding is the seeder (plan §3.9): the picker's selection fills every
     * mode that is still empty; customized modes are never overwritten.
     */
    fun completeOnboarding(selected: List<String>) {
        scope.launch {
            context.sundialDataStore.edit { prefs ->
                Mode.entries.forEach { mode ->
                    val current = prefs[appsKey(mode)]?.split(',')?.filter { it.isNotEmpty() }
                        ?: emptyList()
                    if (current.isEmpty()) prefs[appsKey(mode)] = selected.joinToString(",")
                }
                prefs[onboardedKey] = true
                prefs[seededKey] = true
            }
        }
    }

    fun setPaused(paused: Boolean) {
        scope.launch {
            context.sundialDataStore.edit { it[pausedKey] = paused }
        }
    }

    val appearance: StateFlow<Appearance> = context.sundialDataStore.data
        .map { prefs ->
            Appearance(
                theme = prefs[themeKey]?.let { runCatching { ThemeChoice.valueOf(it) }.getOrNull() }
                    ?: ThemeChoice.SUN,
                align = prefs[alignKey]?.let { runCatching { Side.valueOf(it) }.getOrNull() }
                    ?: Side.LEFT,
                face = prefs[faceKey]?.let { runCatching { Face.valueOf(it) }.getOrNull() }
                    ?: Face.SIGNATURE,
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

    fun setFace(face: Face) {
        scope.launch {
            context.sundialDataStore.edit { it[faceKey] = face.name }
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

    /** Gentle defaults for the onboarding picker: messages, camera, phone if present. */
    fun suggestedSeed(installedPackages: List<String>): List<String> =
        installedPackages.filter { pkg ->
            listOf("messag", "camera", "dialer", "phone").any { pkg.contains(it) }
        }.distinct()
}
