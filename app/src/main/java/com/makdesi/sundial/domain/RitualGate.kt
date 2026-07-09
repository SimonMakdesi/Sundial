package com.makdesi.sundial.domain

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.makdesi.sundial.data.sundialDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The breath ritual's two pieces of state (plan §3.5):
 *  - per-app flags (persisted; global, applies everywhere at all hours)
 *  - 10-minute windows after "Open for 10 min" (in-memory; a lapsed window
 *    simply expires — no enforcement, no alarm)
 */
class RitualGate(private val context: Context, private val scope: CoroutineScope) {

    private val flagsKey = stringSetPreferencesKey("ritual_flags")
    private val windows = mutableMapOf<String, Long>()

    val flags: StateFlow<Set<String>> = context.sundialDataStore.data
        .map { it[flagsKey] ?: emptySet() }
        .stateIn(scope, SharingStarted.Eagerly, emptySet())

    fun shouldAsk(packageName: String): Boolean {
        if (packageName !in flags.value) return false
        val until = windows[packageName] ?: return true
        return System.currentTimeMillis() >= until
    }

    fun openWindow(packageName: String) {
        windows[packageName] = System.currentTimeMillis() + 10 * 60 * 1000L
    }

    fun toggle(packageName: String) {
        scope.launch {
            context.sundialDataStore.edit { prefs ->
                val current = prefs[flagsKey] ?: emptySet()
                prefs[flagsKey] =
                    if (packageName in current) current - packageName else current + packageName
            }
        }
    }
}
