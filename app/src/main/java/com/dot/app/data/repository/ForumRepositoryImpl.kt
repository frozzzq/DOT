package com.dot.app.data.repository

import com.dot.app.data.local.dao.ForumDao
import com.dot.app.data.local.entity.ForumPostEntity
import com.dot.app.data.local.entity.ForumReplyEntity
import com.dot.app.data.mapper.toDomain
import com.dot.app.data.seed.ForumSeedData
import com.dot.app.domain.model.ForumCategory
import com.dot.app.domain.model.ForumPost
import com.dot.app.domain.repository.ForumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForumRepositoryImpl @Inject constructor(
    private val forumDao: ForumDao
) : ForumRepository {

    override fun observeAllPosts(): Flow<List<ForumPost>> =
        forumDao.observePostsWithReplies().map { list -> list.map { it.toDomain() } }

    override suspend fun ensureSeeded() {
        if (forumDao.countPosts() > 0) return
        ForumSeedData.initialThreads().forEach { thread ->
            val postId = forumDao.insertPost(thread.post)
            thread.replies.forEach { reply ->
                forumDao.insertReply(reply.copy(postId = postId))
            }
        }
    }

    override suspend fun createPost(
        title: String,
        body: String,
        category: ForumCategory,
        authorAlias: String,
        isAnonymous: Boolean
    ): Long {
        return forumDao.insertPost(
            ForumPostEntity(
                authorAlias = if (isAnonymous) "Anónima" else authorAlias.ifBlank { "Usuaria Dot" },
                isAnonymous = isAnonymous,
                category = category,
                title = title.trim(),
                body = body.trim(),
                createdDate = LocalDate.now(),
                helpfulCount = 0,
                isMarkedHelpfulByMe = false
            )
        )
    }

    override suspend fun addReply(postId: Long, authorAlias: String, body: String) {
        forumDao.insertReply(
            ForumReplyEntity(
                postId = postId,
                authorAlias = authorAlias.ifBlank { "Anónima" },
                body = body.trim(),
                createdDate = LocalDate.now(),
                isEvidenceBasedTip = false
            )
        )
    }

    override suspend fun toggleHelpful(postId: Long) {
        val post = forumDao.getPostById(postId) ?: return
        val newMarked = !post.isMarkedHelpfulByMe
        val newCount = (post.helpfulCount + if (newMarked) 1 else -1).coerceAtLeast(0)
        forumDao.updateHelpful(postId = postId, newCount = newCount, marked = newMarked)
    }
}
