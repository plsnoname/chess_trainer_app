package com.example.chess_trainer_client.data

data class Puzzle(
    val id: String,
    val fen: String,
    val solution: List<String>,
    val description: String,
    val difficulty: String
)

