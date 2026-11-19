package com.dot.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dot.app.domain.model.ReminderType

@Entity(tableName = "notification_reminders")
data class NotificationReminderEntity(
    @PrimaryKey
    @ColumnInfo(name = "type")
    val type: ReminderType,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean,

    @ColumnInfo(name = "hour")
    val hour: Int,

    @ColumnInfo(name = "minute")
    val minute: Int,

    @ColumnInfo(name = "discreet_mode")
    val discreetMode: Boolean = false
)
