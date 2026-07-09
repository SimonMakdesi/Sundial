package com.makdesi.sundial.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * One DataStore for every Sundial setting, enrolled in Auto Backup by default
 * (plan §3.6). Keys are owned by their feature (RitualGate, and later
 * mode lists / intentions / appearance in M5-M6).
 */
val Context.sundialDataStore by preferencesDataStore(name = "sundial")
