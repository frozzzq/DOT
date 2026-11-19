package com.dot.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "forum_replies",
    foreignKeys = [
        ForeignKey(
            entity = ForumPostEntity::class,
            parentColumns = ["id"],
            childColumns = ["post_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["post_id"])]
)
data class ForumReplyEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "post_id")
    val postId: Long,

    @ColumnInfo(name = "author_alias")
    val authorAlias: String,

    @ColumnInfo(name = "body")
    val body: String,

    @ColumnInfo(name = "created_date")
    val createdDate: LocalDate,

    @ColumnInfo(name = "is_evidence_based_tip")
    val isEvidenceBasedTip: Boolean = false
)
