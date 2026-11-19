package com.dot.app.data.repository

import com.dot.app.data.seed.HealthArticlesCatalog
import com.dot.app.domain.model.HealthArticle
import com.dot.app.domain.repository.HealthArticleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthArticleRepositoryImpl @Inject constructor() : HealthArticleRepository {

    override fun getAllArticles(): List<HealthArticle> = HealthArticlesCatalog.articles

    override fun getArticleById(id: String): HealthArticle? =
        HealthArticlesCatalog.articles.firstOrNull { it.id == id }
}
