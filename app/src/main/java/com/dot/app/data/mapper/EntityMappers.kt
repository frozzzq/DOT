package com.dot.app.data.mapper

import com.dot.app.data.local.dao.ForumPostWithReplies
import com.dot.app.data.local.entity.CycleEntity
import com.dot.app.data.local.entity.DailyLogEntity
import com.dot.app.data.local.entity.EmergencyContraceptionEntity
import com.dot.app.data.local.entity.ForumReplyEntity
import com.dot.app.data.local.entity.NotificationReminderEntity
import com.dot.app.data.local.entity.UserAccountEntity
import com.dot.app.data.local.entity.UserProfileEntity
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.EmergencyContraceptionLog
import com.dot.app.domain.model.ForumPost
import com.dot.app.domain.model.ForumReply
import com.dot.app.domain.model.NotificationReminder
import com.dot.app.domain.model.UserAccount
import com.dot.app.domain.model.UserProfile

fun UserAccountEntity.toDomain(): UserAccount = UserAccount(
    id = id,
    name = name,
    email = email
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    lastPeriodStartDate = lastPeriodStartDate,
    lastPeriodEndDate = lastPeriodEndDate,
    isCurrentlyMenstruating = isCurrentlyMenstruating,
    averagePeriodDuration = averagePeriodDuration,
    averageCycleLength = averageCycleLength,
    regularity = regularity,
    contraceptiveMethod = contraceptiveMethod,
    isOnboardingCompleted = isOnboardingCompleted
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    id = id,
    lastPeriodStartDate = lastPeriodStartDate,
    lastPeriodEndDate = lastPeriodEndDate,
    isCurrentlyMenstruating = isCurrentlyMenstruating,
    averagePeriodDuration = averagePeriodDuration,
    averageCycleLength = averageCycleLength,
    regularity = regularity,
    contraceptiveMethod = contraceptiveMethod,
    isOnboardingCompleted = isOnboardingCompleted
)

fun CycleEntity.toDomain(): Cycle = Cycle(
    id = id,
    startDate = startDate,
    endDate = endDate,
    bleedingDurationDays = bleedingDurationDays,
    actualCycleLengthDays = actualCycleLengthDays,
    isAlteredByEmergencyContraception = isAlteredByEmergencyContraception,
    contextNotes = contextNotes
)

fun Cycle.toEntity(): CycleEntity = CycleEntity(
    id = id,
    startDate = startDate,
    endDate = endDate,
    bleedingDurationDays = bleedingDurationDays,
    actualCycleLengthDays = actualCycleLengthDays,
    isAlteredByEmergencyContraception = isAlteredByEmergencyContraception,
    contextNotes = contextNotes
)

fun DailyLogEntity.toDomain(): DailyLog = DailyLog(
    date = date,
    flowLevel = flowLevel,
    symptoms = symptoms,
    hadSexualActivity = hadSexualActivity,
    protectionUsed = protectionUsed,
    tookContraceptivePill = tookContraceptivePill,
    tookEmergencyPill = tookEmergencyPill,
    notes = notes
)

fun DailyLog.toEntity(): DailyLogEntity = DailyLogEntity(
    date = date,
    flowLevel = flowLevel,
    symptoms = symptoms,
    hadSexualActivity = hadSexualActivity,
    protectionUsed = protectionUsed,
    tookContraceptivePill = tookContraceptivePill,
    tookEmergencyPill = tookEmergencyPill,
    notes = notes
)

fun EmergencyContraceptionEntity.toDomain(): EmergencyContraceptionLog = EmergencyContraceptionLog(
    id = id,
    takenDate = takenDate,
    approximateTime = approximateTime,
    compoundType = compoundType,
    priorProtectionUsed = priorProtectionUsed,
    hadSubsequentIntercourse = hadSubsequentIntercourse,
    notes = notes
)

fun EmergencyContraceptionLog.toEntity(): EmergencyContraceptionEntity = EmergencyContraceptionEntity(
    id = id,
    takenDate = takenDate,
    approximateTime = approximateTime,
    compoundType = compoundType,
    priorProtectionUsed = priorProtectionUsed,
    hadSubsequentIntercourse = hadSubsequentIntercourse,
    notes = notes
)

fun NotificationReminderEntity.toDomain(): NotificationReminder = NotificationReminder(
    type = type,
    isEnabled = isEnabled,
    hour = hour,
    minute = minute,
    discreetMode = discreetMode
)

fun NotificationReminder.toEntity(): NotificationReminderEntity = NotificationReminderEntity(
    type = type,
    isEnabled = isEnabled,
    hour = hour,
    minute = minute,
    discreetMode = discreetMode
)

fun ForumReplyEntity.toDomain(): ForumReply = ForumReply(
    id = id,
    postId = postId,
    authorAlias = authorAlias,
    body = body,
    createdDate = createdDate,
    isEvidenceBasedTip = isEvidenceBasedTip
)

fun ForumPostWithReplies.toDomain(): ForumPost = ForumPost(
    id = post.id,
    authorAlias = post.authorAlias,
    isAnonymous = post.isAnonymous,
    category = post.category,
    title = post.title,
    body = post.body,
    createdDate = post.createdDate,
    helpfulCount = post.helpfulCount,
    isMarkedHelpfulByMe = post.isMarkedHelpfulByMe,
    replies = replies.sortedBy { it.id }.map { it.toDomain() }
)
