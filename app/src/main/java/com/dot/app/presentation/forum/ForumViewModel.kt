package com.dot.app.presentation.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.app.domain.model.ForumCategory
import com.dot.app.domain.model.ForumPost
import com.dot.app.domain.repository.AuthRepository
import com.dot.app.domain.repository.ForumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForumUiState(
    val posts: List<ForumPost> = emptyList(),
    val selectedCategory: ForumCategory? = null,
    val activePostId: Long? = null,
    val showCreateDialog: Boolean = false,
    val currentUserName: String = "Usuaria Dot"
) {
    val filteredPosts: List<ForumPost>
        get() = if (selectedCategory == null) posts else posts.filter { it.category == selectedCategory }

    val activePost: ForumPost?
        get() = activePostId?.let { id -> posts.firstOrNull { it.id == id } }
}

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val forumRepository: ForumRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val localUiControl = MutableStateFlow(LocalControlState())

    val uiState: StateFlow<ForumUiState> = combine(
        forumRepository.observeAllPosts(),
        authRepository.observeCurrentAccount(),
        localUiControl
    ) { posts, account, control ->
        ForumUiState(
            posts = posts,
            selectedCategory = control.selectedCategory,
            activePostId = control.activePostId,
            showCreateDialog = control.showCreateDialog,
            currentUserName = account?.name ?: "Usuaria Dot"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ForumUiState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            forumRepository.ensureSeeded()
        }
    }

    fun onCategorySelected(category: ForumCategory?) {
        localUiControl.update { it.copy(selectedCategory = category) }
    }

    fun onOpenPost(postId: Long) {
        localUiControl.update { it.copy(activePostId = postId) }
    }

    fun onClosePost() {
        localUiControl.update { it.copy(activePostId = null) }
    }

    fun onShowCreateDialog(show: Boolean) {
        localUiControl.update { it.copy(showCreateDialog = show) }
    }

    fun createPost(
        title: String,
        body: String,
        category: ForumCategory,
        isAnonymous: Boolean
    ) {
        if (title.isBlank() || body.isBlank()) return
        val author = uiState.value.currentUserName
        viewModelScope.launch(Dispatchers.IO) {
            forumRepository.createPost(
                title = title,
                body = body,
                category = category,
                authorAlias = author,
                isAnonymous = isAnonymous
            )
            localUiControl.update { it.copy(showCreateDialog = false) }
        }
    }

    fun addReply(postId: Long, replyBody: String, isAnonymous: Boolean) {
        if (replyBody.isBlank()) return
        val alias = if (isAnonymous) "Anónima" else uiState.value.currentUserName
        viewModelScope.launch(Dispatchers.IO) {
            forumRepository.addReply(
                postId = postId,
                authorAlias = alias,
                body = replyBody
            )
        }
    }

    fun toggleHelpful(postId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            forumRepository.toggleHelpful(postId)
        }
    }

    private data class LocalControlState(
        val selectedCategory: ForumCategory? = null,
        val activePostId: Long? = null,
        val showCreateDialog: Boolean = false
    )
}
