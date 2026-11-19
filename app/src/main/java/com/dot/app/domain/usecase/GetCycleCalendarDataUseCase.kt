package com.dot.app.domain.usecase

import com.dot.app.domain.model.Cycle
import com.dot.app.domain.model.CyclePrediction
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.UserProfile
import java.time.LocalDate
import javax.inject.Inject

data class CalendarHighlights(
    val menstruationDays: Set<LocalDate>,
    val predictedPeriodDays: Set<LocalDate>,
    val fertileDays: Set<LocalDate>,
    val ovulationDay: LocalDate?,
    val loggedDays: Set<LocalDate>,
    val emergencyPillDays: Set<LocalDate>
)

/**
 * Construye los conjuntos de días resaltados para el calendario vertical deslizable:
 * - Días de menstruación guardados en el cuestionario inicial, ciclos y registros diarios (#B77076).
 * - Ventana estimada del próximo periodo (tono suave).
 * - Ventana fértil orientativa y días con síntomas registrados.
 */
class GetCycleCalendarDataUseCase @Inject constructor() {

    operator fun invoke(
        userProfile: UserProfile?,
        cycles: List<Cycle>,
        dailyLogs: List<DailyLog>,
        prediction: CyclePrediction?,
        emergencyDates: Set<LocalDate> = emptySet(),
        today: LocalDate = LocalDate.now()
    ): CalendarHighlights {
        val menstruationDays = mutableSetOf<LocalDate>()
        val loggedDays = mutableSetOf<LocalDate>()
        val logsByDate = dailyLogs.associateBy { it.date }

        // 1. Días explícitos de sangrado desde DailyLogs
        dailyLogs.forEach { log ->
            if (log.hasMenstrualBleeding) {
                menstruationDays.add(log.date)
            }
            if (log.symptoms.isNotEmpty() || log.hadSexualActivity || log.tookContraceptivePill ||
                log.tookEmergencyPill || !log.notes.isNullOrBlank()
            ) {
                loggedDays.add(log.date)
            }
        }

        // 2. Días de menstruación derivados de cada ciclo guardado (incluye el del cuestionario inicial)
        val fallbackDuration = userProfile?.averagePeriodDuration ?: 5
        cycles.forEach { cycle ->
            val duration = (cycle.bleedingDurationDays ?: fallbackDuration).coerceIn(1, 14)
            for (offset in 0 until duration) {
                val day = cycle.startDate.plusDays(offset.toLong())
                if (!day.isAfter(today)) {
                    val existingLog = logsByDate[day]
                    // Respetar si la usuaria explicitó "Sin sangrado" en ese día
                    if (existingLog == null || existingLog.hasMenstrualBleeding) {
                        menstruationDays.add(day)
                    }
                }
            }
        }

        // 3. Fallback directo al UserProfile (garantiza que los días del cuestionario inicial siempre aparezcan)
        if (userProfile != null) {
            val start = userProfile.lastPeriodStartDate
            val end = when {
                userProfile.isCurrentlyMenstruating -> today
                userProfile.lastPeriodEndDate != null -> userProfile.lastPeriodEndDate
                else -> start.plusDays((userProfile.averagePeriodDuration - 1).coerceAtLeast(0).toLong())
            }
            var cursor = start
            while (!cursor.isAfter(end) && !cursor.isAfter(today)) {
                val existingLog = logsByDate[cursor]
                if (existingLog == null || existingLog.hasMenstrualBleeding) {
                    menstruationDays.add(cursor)
                }
                cursor = cursor.plusDays(1)
            }
        }

        // 4. Proyección de próximos 3 periodos estimados para el scroll vertical del calendario
        val predictedDays = mutableSetOf<LocalDate>()
        val fertileDays = mutableSetOf<LocalDate>()
        var ovulationDay: LocalDate? = null

        if (prediction != null) {
            val cycleLen = prediction.expectedCycleLengthDays.coerceIn(21, 45).toLong()
            val bleedLen = prediction.expectedBleedingDurationDays.coerceIn(1, 10)

            for (cycleOffset in 0L..2L) {
                val baseStart = prediction.baselineExpectedDate.plusDays(cycleOffset * cycleLen)
                for (dayIdx in 0 until bleedLen) {
                    val d = baseStart.plusDays(dayIdx.toLong())
                    if (d.isAfter(today) && d !in menstruationDays) {
                        predictedDays.add(d)
                    }
                }
            }

            val fStart = prediction.fertileWindowStart
            val fEnd = prediction.fertileWindowEnd
            if (fStart != null && fEnd != null) {
                var c: LocalDate = fStart
                while (!c.isAfter(fEnd)) {
                    if (c !in menstruationDays) {
                        fertileDays.add(c)
                    }
                    c = c.plusDays(1)
                }
                ovulationDay = prediction.estimatedOvulationDate
            }
        }

        return CalendarHighlights(
            menstruationDays = menstruationDays,
            predictedPeriodDays = predictedDays,
            fertileDays = fertileDays,
            ovulationDay = ovulationDay,
            loggedDays = loggedDays,
            emergencyPillDays = emergencyDates
        )
    }
}
