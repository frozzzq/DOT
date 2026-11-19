package com.dot.app.domain.model

enum class ArticleCategory(val label: String) {
    CYCLE("Tu ciclo"),
    CONTRACEPTION("Anticoncepción"),
    EMERGENCY("Postday"),
    SYMPTOMS("Síntomas"),
    WELLNESS("Salud médica")
}

data class ArticleSection(
    val heading: String,
    val body: String
)

/**
 * Artículo educativo basado en evidencia clínica (RF-06).
 */
data class HealthArticle(
    val id: String,
    val title: String,
    val summary: String,
    val category: ArticleCategory,
    val readTimeMinutes: Int,
    val keyTakeaway: String,
    val medicalSource: String,
    val sections: List<ArticleSection>
)
