package com.dot.app.domain.model

import java.time.LocalDate

enum class ForumCategory(val label: String) {
    CYCLE("Ciclo"),
    CONTRACEPTION("Anticonceptivos"),
    SYMPTOMS("Síntomas"),
    EMERGENCY("Postday"),
    WELLNESS("Bienestar")
}

data class ForumReply(
    val id: Long = 0L,
    val postId: Long,
    val authorAlias: String,
    val body: String,
    val createdDate: LocalDate = LocalDate.now(),
    val isEvidenceBasedTip: Boolean = false
)

data class ForumPost(
    val id: Long = 0L,
    val authorAlias: String,
    val isAnonymous: Boolean = true,
    val category: ForumCategory,
    val title: String,
    val body: String,
    val createdDate: LocalDate = LocalDate.now(),
    val helpfulCount: Int = 0,
    val isMarkedHelpfulByMe: Boolean = false,
    val replies: List<ForumReply> = emptyList()
)
