package com.dot.app.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.dot.app.data.local.entity.ForumPostEntity
import com.dot.app.data.local.entity.ForumReplyEntity
import kotlinx.coroutines.flow.Flow

data class ForumPostWithReplies(
    @Embedded val post: ForumPostEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "post_id"
    )
    val replies: List<ForumReplyEntity>
)

@Dao
interface ForumDao {

    @Transaction
    @Query("SELECT * FROM forum_posts ORDER BY id DESC")
    fun observePostsWithReplies(): Flow<List<ForumPostWithReplies>>

    @Query("SELECT COUNT(*) FROM forum_posts")
    suspend fun countPosts(): Int

    @Query("SELECT * FROM forum_posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: Long): ForumPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: ForumPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: ForumReplyEntity): Long

    @Query(
        """
        UPDATE forum_posts
        SET helpful_count = :newCount, is_marked_helpful_by_me = :marked
        WHERE id = :postId
        """
    )
    suspend fun updateHelpful(postId: Long, newCount: Int, marked: Boolean)
}
