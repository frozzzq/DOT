package com.dot.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dot.app.data.local.entity.NotificationReminderEntity
import com.dot.app.domain.model.ReminderType
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationReminderDao {

    @Query("SELECT * FROM notification_reminders")
    fun observeAllReminders(): Flow<List<NotificationReminderEntity>>

    @Query("SELECT * FROM notification_reminders")
    suspend fun getAllRemindersOnce(): List<NotificationReminderEntity>

    @Query("SELECT * FROM notification_reminders WHERE type = :type LIMIT 1")
    suspend fun getReminderByType(type: ReminderType): NotificationReminderEntity?

    @Upsert
    suspend fun upsertReminder(reminder: NotificationReminderEntity)

    @Upsert
    suspend fun upsertReminders(reminders: List<NotificationReminderEntity>)

    @Query("UPDATE notification_reminders SET discreet_mode = :enabled")
    suspend fun updateDiscreetModeAll(enabled: Boolean)
}
