package com.dot.app.domain.repository

import com.dot.app.domain.model.ForumCategory
import com.dot.app.domain.model.ForumPost
import kotlinx.coroutines.flow.Flow

interface ForumRepository {
    fun observeAllPosts(): Flow<List<ForumPost>>
    suspend fun ensureSeeded()
    suspend fun createPost(
        title: String,
        body: String,
        category: ForumCategory,
        authorAlias: String,
        isAnonymous: Boolean
    ): Long
    suspend fun addReply(postId: Long, authorAlias: String, body: String)
    suspend fun toggleHelpful(postId: Long)
}
