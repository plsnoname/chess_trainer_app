package com.example.chess_trainer_client.data.network

import com.example.chess_trainer_client.data.AiMoveRequest
import com.example.chess_trainer_client.data.AiMoveResponse
import com.example.chess_trainer_client.data.AuthLoginRequest
import com.example.chess_trainer_client.data.AuthRegisterRequest
import com.example.chess_trainer_client.data.AuthResponse
import com.example.chess_trainer_client.data.Opening
import com.example.chess_trainer_client.data.Puzzle
import com.example.chess_trainer_client.data.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("puzzles")
    suspend fun getPuzzles(): List<Puzzle>

    @GET("puzzles/{id}")
    suspend fun getPuzzleById(@Path("id") id: String): Puzzle

    @GET("openings")
    suspend fun getOpenings(): List<Opening>

    @GET("openings/{id}")
    suspend fun getOpeningById(@Path("id") id: String): Opening

    @POST("ai-move")
    suspend fun getAiMove(@Body request: AiMoveRequest): AiMoveResponse

    @POST("auth/register")
    suspend fun register(@Body request: AuthRegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthLoginRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(@Header("Authorization") token: String): UserResponse
}

