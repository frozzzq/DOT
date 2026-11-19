package com.dot.app.data.repository

import com.dot.app.core.notifications.DotReminderScheduler
import com.dot.app.data.local.dao.NotificationReminderDao
import com.dot.app.data.mapper.toDomain
import com.dot.app.data.mapper.toEntity
import com.dot.app.domain.model.NotificationReminder
import com.dot.app.domain.model.ReminderType
import com.dot.app.domain.repository.NotificationReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationReminderRepositoryImpl @Inject constructor(
    private val reminderDao: NotificationReminderDao,
    private val scheduler: DotReminderScheduler
) : NotificationReminderRepository {

    override fun observeReminders(): Flow<List<NotificationReminder>> =
        reminderDao.observeAllReminders().map { entities ->
            val byType = entities.associateBy { it.type }
            ReminderType.entries.map { type ->
                byType[type]?.toDomain() ?: NotificationReminder(
                    type = type,
                    isEnabled = false,
                    hour = type.defaultHour,
                    minute = type.defaultMinute,
                    discreetMode = false
                )
            }
        }

    override suspend fun ensureDefaultRemindersInitialized(enablePillByDefault: Boolean) {
        val existing = reminderDao.getAllRemindersOnce()
        if (existing.isEmpty()) {
            val defaults = ReminderType.entries.map { type ->
                NotificationReminder(
                    type = type,
                    isEnabled = when (type) {
                        ReminderType.DAILY_CONTRACEPTIVE_PILL -> enablePillByDefault
                        ReminderType.PERIOD_UPCOMING -> true
                        ReminderType.PERIOD_LOG_REMINDER -> true
                        ReminderType.DAILY_SYMPTOMS_CHECKIN -> false
                    },
                    hour = type.defaultHour,
                    minute = type.defaultMinute,
                    discreetMode = false
                )
            }
            reminderDao.upsertReminders(defaults.map { it.toEntity() })
            defaults.forEach { scheduler.scheduleOrCancel(it) }
        }
    }

    override suspend fun upsertReminder(reminder: NotificationReminder) {
        reminderDao.upsertReminder(reminder.toEntity())
        scheduler.scheduleOrCancel(reminder)
    }

    override suspend fun setDiscreetModeForAll(enabled: Boolean) {
        reminderDao.updateDiscreetModeAll(enabled)
        val updated = reminderDao.getAllRemindersOnce().map { it.toDomain() }
        updated.forEach { scheduler.scheduleOrCancel(it) }
    }

    override suspend fun getReminder(type: ReminderType): NotificationReminder? =
        reminderDao.getReminderByType(type)?.toDomain()
}
