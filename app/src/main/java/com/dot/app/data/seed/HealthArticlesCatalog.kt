package com.dot.app.data.seed

import com.dot.app.domain.model.ArticleCategory
import com.dot.app.domain.model.ArticleSection
import com.dot.app.domain.model.HealthArticle

/**
 * Catálogo local de contenido educativo basado en evidencia médica (RF-06).
 * Fuentes clínicas: OMS (WHO), ACOG (American College of Obstetricians and Gynecologists)
 * y FIGO (International Federation of Gynecology and Obstetrics).
 */
object HealthArticlesCatalog {

    val articles: List<HealthArticle> = listOf(
        HealthArticle(
            id = "fases-ciclo-menstrual",
            title = "Las 4 fases de tu ciclo menstrual",
            summary = "Entiende cómo cambian tus hormonas, energía y emociones desde el Día 1 hasta tu próximo periodo.",
            category = ArticleCategory.CYCLE,
            readTimeMinutes = 3,
            keyTakeaway = "Tu ciclo comienza el primer día de sangrado menstrual (Día 1) y termina el día anterior al siguiente periodo.",
            medicalSource = "ACOG & FIGO — Guías de Fisiología Menstrual",
            sections = listOf(
                ArticleSection(
                    heading = "1. Fase menstrual (Días 1 a 5 aprox.)",
                    body = "Inicia el primer día en que aparece el sangrado menstrual. El endometrio se desprende de forma natural porque no hubo embarazo. Los niveles de estrógeno y progesterona están bajos, por lo que es normal sentir menor energía, cólicos o sensibilidad lumbar."
                ),
                ArticleSection(
                    heading = "2. Fase folicular (Día 1 hasta la ovulación)",
                    body = "Tu cerebro libera FSH para madurar un óvulo y el estrógeno sube gradualmente. Muchas mujeres notan más energía, mejor concentración y piel más luminosa durante estos días."
                ),
                ArticleSection(
                    heading = "3. Ovulación (alrededor de 14 días antes del siguiente periodo)",
                    body = "Un pico de hormona LH libera el óvulo maduro. El flujo cervical suele volverse más claro y elástico (similar a clara de huevo). Los 5 días previos y el día de ovulación conforman tu ventana más fértil."
                ),
                ArticleSection(
                    heading = "4. Fase lútea (después de ovular hasta el próximo periodo)",
                    body = "Aumenta la progesterona. Si no ocurre fecundación, las hormonas descienden hacia el final de esta fase, pudiendo aparecer síntomas premenstruales como inflamación, sensibilidad mamaria o cambios de ánimo."
                )
            )
        ),
        HealthArticle(
            id = "ciclo-normal-vs-irregular",
            title = "¿Cuándo un ciclo es normal y cuándo conviene ir al médico?",
            summary = "Conoce los rangos médicos reales de duración, sangrado y variabilidad sin mitos.",
            category = ArticleCategory.WELLNESS,
            readTimeMinutes = 3,
            keyTakeaway = "Un ciclo saludable NO tiene que durar exactamente 28 días: el rango clínico normal en adultas va de 24 a 38 días.",
            medicalSource = "FIGO 2018 — Clasificación de Sangrado Uterino Normal",
            sections = listOf(
                ArticleSection(
                    heading = "Parámetros de un ciclo normal",
                    body = "Según la Federación Internacional de Ginecología y Obstetricia (FIGO), un ciclo adulto normal dura entre 24 y 38 días, con un sangrado de hasta 8 días y una variación máxima de 7 a 9 días entre tu ciclo más corto y el más largo del año."
                ),
                ArticleSection(
                    heading = "En adolescencia y primeros años",
                    body = "Durante los primeros 2 a 3 años tras la primera menstruación (menarquia), los ciclos suelen ser más variables y se consideran aceptables rangos de 21 a 45 días mientras el eje hormonal madura."
                ),
                ArticleSection(
                    heading = "Señales para agendar consulta ginecológica",
                    body = "• Ciclos repetidamente menores a 21 días o mayores a 45 días.\n• Ausencia de periodo por más de 90 días (sin embarazo).\n• Sangrado que dura más de 8 días o empapa una toalla/tampón cada hora.\n• Dolor menstrual incapacitante que no cede con medidas habituales."
                )
            )
        ),
        HealthArticle(
            id = "postday-que-esperar",
            title = "Pastilla del día siguiente (Postday): qué esperar en tu ciclo",
            summary = "Por qué puede adelantar o retrasar tu regla y cuándo hacer una prueba de embarazo confiable.",
            category = ArticleCategory.EMERGENCY,
            readTimeMinutes = 4,
            keyTakeaway = "Un retraso de hasta 7–10 días después de tomar Postday es un efecto hormonal esperado y no confirma un embarazo.",
            medicalSource = "OMS (WHO) — Guía de Anticoncepción de Emergencia",
            sections = listOf(
                ArticleSection(
                    heading = "¿Cómo funciona realmente?",
                    body = "Tanto el Levonorgestrel (efectivo hasta 72 horas) como el Acetato de Ulipristal (hasta 120 horas) actúan principalmente posponiendo o inhibiendo la ovulación. No son abortivas ni interrumpen un embarazo ya establecido."
                ),
                ArticleSection(
                    heading = "Cambios normales tras la toma",
                    body = "Debido a la carga hormonal temporal, tu próximo periodo puede adelantarse unos días o retrasarse hasta una semana (o hasta 10 días con ulipristal). También puede presentarse manchado leve (spotting), náuseas, dolor de cabeza o sensibilidad mamaria."
                ),
                ArticleSection(
                    heading = "¿Cuándo realizar una prueba de embarazo?",
                    body = "Si tu periodo se retrasa más de 7 días respecto a tu nueva ventana ajustada, o han pasado entre 14 y 21 días desde la relación sin protección, una prueba de embarazo en orina te dará un resultado confiable."
                )
            )
        ),
        HealthArticle(
            id = "olvido-pastilla-anticonceptiva",
            title = "Pastillas anticonceptivas diarias: qué hacer si olvidas una",
            summary = "Pasos claros y tranquilos según las horas de retraso y el tipo de pastilla que tomas.",
            category = ArticleCategory.CONTRACEPTION,
            readTimeMinutes = 3,
            keyTakeaway = "Activar una alarma diaria a una hora ligada a tu rutina (como al lavarte los dientes) reduce drásticamente los olvidos.",
            medicalSource = "CDC & OMS — Recomendaciones de Uso de Anticonceptivos",
            sections = listOf(
                ArticleSection(
                    heading = "Olvido de 1 pastilla combinada (menos de 24 h)",
                    body = "Tómala en cuanto te acuerdes (aunque signifique tomar dos el mismo día) y continúa el paquete de manera habitual. Generalmente mantienes protección y no necesitas anticoncepción de emergencia."
                ),
                ArticleSection(
                    heading = "Olvido de 2 o más pastillas combinadas",
                    body = "Toma la última pastilla olvidada de inmediato, continúa con el resto del paquete y utiliza condón durante los siguientes 7 días continuos. Si el olvido ocurrió en la primera semana y hubo sexo sin protección, valora anticoncepción de emergencia."
                ),
                ArticleSection(
                    heading = "Pastillas de solo progestina (minipíldora)",
                    body = "Tienen un margen más estricto (3 horas en norestisterona/levonorgestrel o 12 horas en desogestrel). Si excedes el margen, tómala apenas recuerdes y usa condón durante las siguientes 48 horas."
                )
            )
        ),
        HealthArticle(
            id = "alivio-colicos-menstruales",
            title = "Cólicos menstruales: cómo aliviarlos con evidencia",
            summary = "Estrategias prácticas para reducir el dolor menstrual y diferenciarlo de alertas como endometriosis.",
            category = ArticleCategory.SYMPTOMS,
            readTimeMinutes = 3,
            keyTakeaway = "El calor local en el abdomen bajo tiene evidencia clínica comparable a analgésicos suaves al relajar el músculo uterino.",
            medicalSource = "ACOG — Manejo de Dismenorrea Primaria",
            sections = listOf(
                ArticleSection(
                    heading = "¿Por qué duelen los cólicos?",
                    body = "Durante la menstruación el útero libera prostaglandinas, sustancias que provocan contracciones para expulsar el endometrio. Niveles más altos producen cólicos más intensos."
                ),
                ArticleSection(
                    heading = "Medidas que sí funcionan",
                    body = "• Compresa o bolsa de agua tibia en el vientre bajo por 15–20 minutos.\n• Movimiento suave (caminar o estiramientos de cadera y espalda baja).\n• Hidratación adecuada y reducir cafeína o exceso de sal durante esos días.\n• Analgésicos antiinflamatorios (como ibuprofeno o naproxeno) si te los ha indicado tu médico."
                ),
                ArticleSection(
                    heading = "¿Cuándo no es un cólico normal?",
                    body = "El dolor menstrual nunca debería impedirte estudiar, trabajar o hacer tu vida cotidiana de forma repetida. Si el dolor empeora mes con mes o aparece fuera del periodo, consulta para descartar endometriosis o miomas."
                )
            )
        ),
        HealthArticle(
            id = "sexo-seguro-y-mitos",
            title = "Sexo con condón, sin condón y mitos sobre los 'días seguros'",
            summary = "Lo que debes saber sobre fertilidad, prevención de ITS y protección real en cada fase.",
            category = ArticleCategory.CONTRACEPTION,
            readTimeMinutes = 3,
            keyTakeaway = "El condón es el único método que protege simultáneamente contra embarazos y contra infecciones de transmisión sexual (ITS).",
            medicalSource = "OMS — Salud Sexual y Reproductiva",
            sections = listOf(
                ArticleSection(
                    heading = "El mito de los días 100% seguros",
                    body = "Aunque el riesgo de embarazo es mayor en la ventana fértil, la ovulación puede adelantarse o retrasarse inesperadamente y los espermatozoides pueden sobrevivir hasta 5 días dentro del aparato reproductor femenino."
                ),
                ArticleSection(
                    heading = "Doble protección",
                    body = "Si usas pastillas, DIU o implante, estás protegida frente a embarazos, pero solo el condón externo o interno reduce el riesgo de ITS."
                ),
                ArticleSection(
                    heading = "Registrar sin juicios",
                    body = "Anotar si hubo sexo con o sin condón en Dot te ayuda a entender tus tiempos con claridad y tomar decisiones informadas si algún día necesitas orientación médica."
                )
            )
        )
    )
}
