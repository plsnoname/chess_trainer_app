package com.example.chess_trainer_client.data.repository

import android.content.Context
import com.example.chess_trainer_client.data.AiMoveRequest
import com.example.chess_trainer_client.data.AiMoveResponse
import com.example.chess_trainer_client.data.Opening
import com.example.chess_trainer_client.data.Puzzle
import com.example.chess_trainer_client.data.network.ApiService
import com.example.chess_trainer_client.data.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChessRepository(private val apiService: ApiService = RetrofitClient.apiService) {
    suspend fun getPuzzles(): Result<List<Puzzle>> = safeCall { apiService.getPuzzles() }

    suspend fun getPuzzleById(id: String): Result<Puzzle> = safeCall { apiService.getPuzzleById(id) }

    suspend fun getOpenings(): Result<List<Opening>> = safeCall { apiService.getOpenings() }

    suspend fun getOpeningById(id: String): Result<Opening> = safeCall { apiService.getOpeningById(id) }

    suspend fun getAiMove(request: AiMoveRequest): Result<AiMoveResponse> = safeCall { apiService.getAiMove(request) }

    fun loadLocalPuzzles(context: Context): List<Puzzle> {
        val json = context.assets.open("puzzles.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Puzzle>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun loadLocalOpenings(context: Context): List<Opening> {
        val json = context.assets.open("openings.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Opening>>() {}.type
        return Gson().fromJson(json, type)
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}

