package com.makdesi.sundial.system

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.makdesi.sundial.domain.ModeEngine
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

/**
 * Inexact alarms at mode boundaries — a minute of drift is acceptable and
 * battery-kind (plan §3.2). Each fire recomputes and schedules the next one.
 */
object AlarmScheduler {
    private const val REQUEST_CODE = 1

    fun scheduleNext(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val boundary = ModeEngine.nextBoundary(ZonedDateTime.now())
        val intent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, ModeAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.set(AlarmManager.RTC, boundary.toInstant().toEpochMilli(), intent)
    }
}

/** Recompute, reschedule, and let the lock screen catch the light too. */
private fun BroadcastReceiver.onBoundary(context: Context) {
    ModeEngine.events.tryEmit(Unit)
    AlarmScheduler.scheduleNext(context)
    val pending = goAsync()
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            com.makdesi.sundial.data.WallpaperSync.sync(context)
        } finally {
            pending.finish()
        }
    }
}

class ModeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = onBoundary(context)
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) onBoundary(context)
    }
}

class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> onBoundary(context)
        }
    }
}
