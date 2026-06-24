package com.example.chess_trainer_client.data.local.db

data class SavedGameEntity(
    val id: Long = 0L,
    val name: String?,
    val fen: String,
    val activeColor: String,
    val mode: String,
    val timestamp: Long
)

