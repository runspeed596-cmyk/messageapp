package com.hasani.messageapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ChatWithParticipants(
    @Embedded val chat: ChatEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ChatParticipantEntity::class,
            parentColumn = "chatId",
            entityColumn = "userId"
        )
    )
    val participants: List<UserEntity>
)
