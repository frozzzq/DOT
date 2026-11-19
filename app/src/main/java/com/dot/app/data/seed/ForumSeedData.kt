package com.dot.app.data.seed

import com.dot.app.data.local.entity.ForumPostEntity
import com.dot.app.data.local.entity.ForumReplyEntity
import com.dot.app.domain.model.ForumCategory
import java.time.LocalDate

/**
 * Hilos iniciales de comunidad empática y respuestas basadas en evidencia
 * para poblar el foro local offline la primera vez que se abre.
 */
object ForumSeedData {

    data class SeedThread(
        val post: ForumPostEntity,
        val replies: List<ForumReplyEntity>
    )

    fun initialThreads(today: LocalDate = LocalDate.now()): List<SeedThread> = listOf(
        SeedThread(
            post = ForumPostEntity(
                authorAlias = "Anónima",
                isAnonymous = true,
                category = ForumCategory.EMERGENCY,
                title = "Tomé la postday hace 10 días y todavía no me baja, ¿es normal?",
                body = "Mi periodo suele ser muy puntual pero este mes tomé levonorgestrel. Ya llevo 4 días de retraso y estoy algo nerviosa. ¿A alguien más le pasó?",
                createdDate = today.minusDays(2),
                helpfulCount = 18
            ),
            replies = listOf(
                ForumReplyEntity(
                    postId = 0L,
                    authorAlias = "Guía Dot • Evidencia",
                    body = "Sí, es muy común. La pastilla del día siguiente retrasa la ovulación, por lo que tu periodo puede moverse hasta 7 días (o hasta 10 con ulipristal). Si pasan 14–21 días desde la toma sin sangrado, una prueba de embarazo en orina te dará tranquilidad.",
                    createdDate = today.minusDays(2),
                    isEvidenceBasedTip = true
                ),
                ForumReplyEntity(
                    postId = 0L,
                    authorAlias = "Valeria M.",
                    body = "A mí se me retrasó 6 días exactos cuando la tomé el año pasado y tuve un poco de manchado antes. Respirar hondo y registrarlo aquí me calmó muchísimo.",
                    createdDate = today.minusDays(1),
                    isEvidenceBasedTip = false
                )
            )
        ),
        SeedThread(
            post = ForumPostEntity(
                authorAlias = "Sofía R.",
                isAnonymous = false,
                category = ForumCategory.CYCLE,
                title = "Mis ciclos duran a veces 26 días y otras 34 días, ¿soy irregular?",
                body = "Siempre pensé que tenía que ser de 28 días exactos. He estado registrando los últimos meses y varían entre 26 y 34 días.",
                createdDate = today.minusDays(4),
                helpfulCount = 24
            ),
            replies = listOf(
                ForumReplyEntity(
                    postId = 0L,
                    authorAlias = "Guía Dot • Evidencia",
                    body = "Según criterios FIGO, un ciclo adulto normal está entre 24 y 38 días y una diferencia de hasta 7–9 días entre el ciclo más corto y el más largo sigue considerándose regular. Puedes revisar tu resumen en la pestaña 'Ciclos'.",
                    createdDate = today.minusDays(4),
                    isEvidenceBasedTip = true
                ),
                ForumReplyEntity(
                    postId = 0L,
                    authorAlias = "Anónima",
                    body = "A mí me pasaba igual y mi ginecóloga me explicó que el estrés de exámenes me movía la ovulación unos días. ¡Llevar el historial ayuda muchísimo!",
                    createdDate = today.minusDays(3),
                    isEvidenceBasedTip = false
                )
            )
        ),
        SeedThread(
            post = ForumPostEntity(
                authorAlias = "Anónima",
                isAnonymous = true,
                category = ForumCategory.CONTRACEPTION,
                title = "¿A qué hora recomiendan poner la alarma para las pastillas diarias?",
                body = "Acabo de empezar con pastillas anticonceptivas combinadas y tengo miedo de olvidarlas en fines de semana.",
                createdDate = today.minusDays(6),
                helpfulCount = 15
            ),
            replies = listOf(
                ForumReplyEntity(
                    postId = 0L,
                    authorAlias = "Camila H.",
                    body = "A mí me funciona perfecto a las 9:00 PM antes de cenar o lavarme los dientes, porque en la mañana los fines de semana me despierto más tarde. ¡Activa el modo discreto en las notificaciones de Dot!",
                    createdDate = today.minusDays(5),
                    isEvidenceBasedTip = false
                )
            )
        ),
        SeedThread(
            post = ForumPostEntity(
                authorAlias = "Mariana G.",
                isAnonymous = false,
                category = ForumCategory.SYMPTOMS,
                title = "¿Qué les ayuda más para los cólicos fuertes el primer día?",
                body = "El primer día siempre me da dolor de vientre bajo y cansancio. ¿Tienen tips naturales que les funcionen?",
                createdDate = today.minusDays(8),
                helpfulCount = 31
            ),
            replies = listOf(
                ForumReplyEntity(
                    postId = 0L,
                    authorAlias = "Guía Dot • Evidencia",
                    body = "Aplicar calor local tibio en el abdomen bajo 15–20 min, tomar agua suficiente y hacer estiramientos suaves ayudan a relajar el útero. Recuerda: si el dolor te impide realizar tus actividades mes a mes, vale la pena consultarlo con una especialista.",
                    createdDate = today.minusDays(8),
                    isEvidenceBasedTip = true
                )
            )
        )
    )
}
