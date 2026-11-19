package com.dot.app.presentation.tips

import androidx.lifecycle.ViewModel
import com.dot.app.domain.model.ArticleCategory
import com.dot.app.domain.model.HealthArticle
import com.dot.app.domain.repository.HealthArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TipsUiState(
    val selectedCategory: ArticleCategory? = null,
    val articles: List<HealthArticle> = emptyList(),
    val selectedArticle: HealthArticle? = null
) {
    val filteredArticles: List<HealthArticle>
        get() = if (selectedCategory == null) {
            articles
        } else {
            articles.filter { it.category == selectedCategory }
        }
}

@HiltViewModel
class TipsViewModel @Inject constructor(
    healthArticleRepository: HealthArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TipsUiState(articles = healthArticleRepository.getAllArticles())
    )
    val uiState: StateFlow<TipsUiState> = _uiState.asStateFlow()

    fun onCategorySelected(category: ArticleCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onArticleOpened(article: HealthArticle) {
        _uiState.update { it.copy(selectedArticle = article) }
    }

    fun onArticleClosed() {
        _uiState.update { it.copy(selectedArticle = null) }
    }
}
