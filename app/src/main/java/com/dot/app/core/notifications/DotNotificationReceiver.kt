package com.dot.app.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dot.app.domain.model.ReminderType

/**
 * Receptor de alarmas locales que dispara la notificación correspondiente
 * para pastillas anticonceptivas, aviso de periodo o registro diario.
 */
class DotNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DotReminderScheduler.ACTION_SHOW_REMINDER) {
            val typeName = intent.getStringExtra(DotReminderScheduler.EXTRA_REMINDER_TYPE) ?: return
            val reminderType = runCatching { ReminderType.valueOf(typeName) }.getOrNull() ?: return
            val discreetMode = intent.getBooleanExtra(DotReminderScheduler.EXTRA_DISCREET_MODE, false)

            val helper = DotNotificationHelper(context.applicationContext)
            helper.showReminderNotification(
                type = reminderType,
                discreetMode = discreetMode
            )
        }
    }
}
