package com.dot.app.domain.repository

import com.dot.app.domain.model.NotificationReminder
import com.dot.app.domain.model.ReminderType
import kotlinx.coroutines.flow.Flow

interface NotificationReminderRepository {
    fun observeReminders(): Flow<List<NotificationReminder>>
    suspend fun ensureDefaultRemindersInitialized(enablePillByDefault: Boolean = false)
    suspend fun upsertReminder(reminder: NotificationReminder)
    suspend fun setDiscreetModeForAll(enabled: Boolean)
    suspend fun getReminder(type: ReminderType): NotificationReminder?
}
