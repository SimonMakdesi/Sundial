package com.makdesi.sundial.data

import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Notification whispers (plan §3.7): a faint count per package, nothing more.
 * No reds, no badges, no icons. Everything works when access is not granted —
 * the counts simply never appear.
 */
class NotificationWhisperService : NotificationListenerService() {

    companion object {
        private val _counts = MutableStateFlow<Map<String, Int>>(emptyMap())
        val counts: StateFlow<Map<String, Int>> = _counts

        fun isEnabled(context: Context): Boolean =
            NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
    }

    private fun recount() {
        val active = runCatching { activeNotifications }.getOrNull() ?: return
        _counts.value = active
            .filterNot { it.isGroupSummary() }
            .groupingBy { it.packageName }
            .eachCount()
    }

    private fun StatusBarNotification.isGroupSummary(): Boolean =
        notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY != 0

    override fun onListenerConnected() = recount()

    override fun onListenerDisconnected() {
        _counts.value = emptyMap()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) = recount()

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = recount()
}
