package com.hasani.messageapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "courses",
    indices = [Index("channelId")]
)
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val channelId: String, // Links to the automatically created channel
    val creatorId: String,
    val createdAt: Long = System.currentTimeMillis()
)
