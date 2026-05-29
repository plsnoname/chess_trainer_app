package com.example.chess_trainer_client.data.local.db

data class UserSessionEntity(
    val userId: Long,
    val email: String,
    val lastLoginAt: Long,
    val tokenLast4: String
)

