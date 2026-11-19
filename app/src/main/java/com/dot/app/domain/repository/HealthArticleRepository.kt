package com.dot.app.domain.repository

import com.dot.app.domain.model.HealthArticle

interface HealthArticleRepository {
    fun getAllArticles(): List<HealthArticle>
    fun getArticleById(id: String): HealthArticle?
}
