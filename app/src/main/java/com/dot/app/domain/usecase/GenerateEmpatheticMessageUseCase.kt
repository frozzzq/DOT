package com.dot.app.domain.usecase

import com.dot.app.domain.model.CyclePrediction
import com.dot.app.domain.model.EmpatheticMessage
import com.dot.app.domain.model.MessageCategory
import com.dot.app.domain.model.PredictionConfidenceContext
import java.time.LocalDate
import javax.inject.Inject

/**
 * Generador de mensajería contextual y empática libre de ansiedad (RF-05, RNF-03).
 *
 * Evalúa el resultado de [CyclePrediction] y construye explicaciones fisiológicas claras.
 * Evita las falsas alarmas de embarazo cuando un retraso sobre la fecha base es explicable
 * por la ingesta reciente de anticoncepción de emergencia o por irregularidad documentada,
 * e informa con precisión clínica cuándo considerar una prueba o valoración médica.
 */
class GenerateEmpatheticMessageUseCase @Inject constructor() {

    operator fun invoke(
        prediction: CyclePrediction,
        evaluationDate: LocalDate = LocalDate.now()
    ): EmpatheticMessage {
        val emergencyEvent = prediction.activeEmergencyContraception

        // Caso 1: Ciclo con ingesta reciente de anticoncepción de emergencia (RF-03, RF-05)
        if (emergencyEvent != null) {
            return buildEmergencyContraceptionMessage(prediction, emergencyEvent.takenDate, evaluationDate)
        }

        // Caso 2: Se superó la ventana ampliada (retraso prolongado sin pastilla de emergencia)
        if (prediction.daysPastExpandedWindow > 0) {
            return buildExtendedVariationMessage(prediction, evaluationDate)
        }

        // Caso 3: Dentro de la ventana esperada (incluyendo días posteriores a la fecha central)
        if (!evaluationDate.isBefore(prediction.estimatedWindowStart) &&
            !evaluationDate.isAfter(prediction.estimatedWindowEnd)
        ) {
            return buildWithinWindowMessage(prediction)
        }

        // Caso 4: Ciclo transcurriendo normalmente antes de la ventana estimada
        return EmpatheticMessage(
            title = "Día ${prediction.currentDayOfCycle} de tu ciclo",
            body = "Tu cuerpo avanza dentro del ritmo esperado. Tu próxima ventana menstrual se estima entre el ${prediction.estimatedWindowStart} y el ${prediction.estimatedWindowEnd}.",
            reassuranceNote = "Recuerda que el ciclo menstrual es un proceso biológico dinámico y pequeñas variaciones de días son completamente naturales.",
            category = MessageCategory.CYCLE_IN_PROGRESS_NORMAL
        )
    }

    private fun buildEmergencyContraceptionMessage(
        prediction: CyclePrediction,
        pillTakenDate: LocalDate,
        evaluationDate: LocalDate
    ): EmpatheticMessage {
        val recommendedTestDate = pillTakenDate.plusDays(DAYS_FOR_RELIABLE_PREGNANCY_TEST)

        if (prediction.daysPastBaselineDate > 0 && prediction.isWithinExpectedWindow) {
            return EmpatheticMessage(
                title = "Variación esperada por anticoncepción de emergencia",
                body = "Superaste la fecha estándar de tu calendario, pero registraste una toma de anticoncepción de emergencia el $pillTakenDate. Esta carga hormonal actúa posponiendo la ovulación, por lo que es esperado que tu periodo se desplace hasta el ${prediction.estimatedWindowEnd}.",
                reassuranceNote = "Un retraso en estos días es un efecto secundario normal del medicamento y por sí solo no confirma un embarazo.",
                actionableGuidance = if (evaluationDate.isBefore(recommendedTestDate)) {
                    "No es necesario realizar una prueba de embarazo todavía; los resultados en orina son confiables a partir de los 14 a 21 días posteriores a la toma ($recommendedTestDate) o si el retraso supera una semana después de tu ventana ajustada."
                } else {
                    "Si tu sangrado no se presenta al finalizar la ventana ajustada (${prediction.estimatedWindowEnd}), puedes realizar una prueba de embarazo en orina para mayor tranquilidad."
                },
                suggestedPregnancyTestDate = recommendedTestDate,
                category = MessageCategory.EXPLAINABLE_VARIATION_EMERGENCY_PILL
            )
        }

        if (prediction.daysPastExpandedWindow > 0) {
            return EmpatheticMessage(
                title = "Tu ventana ajustada ha concluido",
                body = "Han pasado ${prediction.daysPastExpandedWindow} días desde el margen estimado tras tu toma de anticoncepción de emergencia del $pillTakenDate.",
                reassuranceNote = "Cada organismo metaboliza el ajuste hormonal a su propio ritmo, pero cuando el periodo demora más de una semana respecto a la ventana esperada es buen momento para dar seguimiento.",
                actionableGuidance = "Para tu tranquilidad, te sugerimos realizar una prueba de embarazo (confiable a partir del $recommendedTestDate) o consultar con una profesional de ginecología.",
                suggestedPregnancyTestDate = recommendedTestDate,
                category = MessageCategory.MEDICAL_CHECKUP_SUGGESTION
            )
        }

        return EmpatheticMessage(
            title = "Ciclo recalibrado por anticoncepción de emergencia",
            body = "Ajustamos tu proyección tras el registro del $pillTakenDate. Tu próxima ventana estimada ahora abarca del ${prediction.estimatedWindowStart} al ${prediction.estimatedWindowEnd}.",
            reassuranceNote = "Es normal experimentar manchado intermenstrual leve (spotting), sensibilidad mamaria o que tu periodo se adelante o retrase algunos días.",
            actionableGuidance = "Registra cualquier flujo o síntoma diario para mantener tu seguimiento actualizado.",
            suggestedPregnancyTestDate = recommendedTestDate,
            category = MessageCategory.EXPLAINABLE_VARIATION_EMERGENCY_PILL
        )
    }

    private fun buildWithinWindowMessage(prediction: CyclePrediction): EmpatheticMessage {
        if (prediction.confidenceContext == PredictionConfidenceContext.EXPANDED_IRREGULAR &&
            prediction.daysPastBaselineDate > 0
        ) {
            return EmpatheticMessage(
                title = "Dentro de tu rango de variabilidad natural",
                body = "Han pasado ${prediction.daysPastBaselineDate} días desde el promedio central, pero sigues dentro de tu ventana habitual (${prediction.estimatedWindowStart} al ${prediction.estimatedWindowEnd}).",
                reassuranceNote = "En ciclos con variabilidad natural, la fase folicular puede extenderse por factores cotidianos como estrés, cambios en el sueño o ejercicio sin que represente una señal de alarma.",
                category = MessageCategory.EXPLAINABLE_VARIATION_IRREGULARITY
            )
        }

        return EmpatheticMessage(
            title = "Estás en tu ventana menstrual estimada",
            body = "Tu periodo puede iniciar en cualquier momento entre el ${prediction.estimatedWindowStart} y el ${prediction.estimatedWindowEnd}.",
            reassuranceNote = "Que el sangrado no inicie en un día exacto del calendario es completamente normal; los ciclos saludables suelen variar algunos días entre un mes y otro.",
            category = MessageCategory.WITHIN_EXPECTED_WINDOW
        )
    }

    private fun buildExtendedVariationMessage(
        prediction: CyclePrediction,
        evaluationDate: LocalDate
    ): EmpatheticMessage {
        return EmpatheticMessage(
            title = "Tu ciclo presenta una variación mayor a la habitual",
            body = "Han transcurrido ${prediction.daysPastExpandedWindow} días adicionales después de tu ventana estimada (${prediction.estimatedWindowEnd}).",
            reassuranceNote = "Los cambios en el estilo de vida, picos de estrés, viajes o fluctuaciones hormonales pueden posponer la ovulación de forma ocasional.",
            actionableGuidance = "Si tuviste relaciones sexuales sin protección en este ciclo, realizar una prueba de embarazo te dará certeza. Si las variaciones continúan, considera comentarlo en consulta ginecológica.",
            suggestedPregnancyTestDate = evaluationDate,
            category = MessageCategory.MEDICAL_CHECKUP_SUGGESTION
        )
    }

    companion object {
        private const val DAYS_FOR_RELIABLE_PREGNANCY_TEST = 21L
    }
}
