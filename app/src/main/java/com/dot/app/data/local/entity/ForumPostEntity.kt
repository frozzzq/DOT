package com.dot.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dot.app.domain.model.ForumCategory
import java.time.LocalDate

@Entity(tableName = "forum_posts")
data class ForumPostEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "author_alias")
    val authorAlias: String,

    @ColumnInfo(name = "is_anonymous")
    val isAnonymous: Boolean = true,

    @ColumnInfo(name = "category")
    val category: ForumCategory,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "body")
    val body: String,

    @ColumnInfo(name = "created_date")
    val createdDate: LocalDate,

    @ColumnInfo(name = "helpful_count")
    val helpfulCount: Int = 0,

    @ColumnInfo(name = "is_marked_helpful_by_me")
    val isMarkedHelpfulByMe: Boolean = false
)
