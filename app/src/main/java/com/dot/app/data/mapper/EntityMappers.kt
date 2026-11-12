package com.dot.app.data.mapper

import com.dot.app.data.local.entity.CycleEntity
import com.dot.app.data.local.entity.DailyLogEntity
import com.dot.app.data.local.entity.EmergencyContraceptionEntity
import com.dot.app.data.local.entity.UserProfileEntity
import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.EmergencyContraceptionLog
import com.dot.app.domain.model.UserProfile

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    lastPeriodStartDate = lastPeriodStartDate,
    averagePeriodDuration = averagePeriodDuration,
    averageCycleLength = averageCycleLength,
    regularity = regularity,
    contraceptiveMethod = contraceptiveMethod,
    isOnboardingCompleted = isOnboardingCompleted
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    id = id,
    lastPeriodStartDate = lastPeriodStartDate,
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
    notes = notes
)

fun DailyLog.toEntity(): DailyLogEntity = DailyLogEntity(
    date = date,
    flowLevel = flowLevel,
    symptoms = symptoms,
    hadSexualActivity = hadSexualActivity,
    protectionUsed = protectionUsed,
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
