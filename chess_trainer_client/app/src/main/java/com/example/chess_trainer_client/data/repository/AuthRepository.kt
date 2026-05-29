package com.example.chess_trainer_client.data.repository

import com.example.chess_trainer_client.data.AuthLoginRequest
import com.example.chess_trainer_client.data.AuthRegisterRequest
import com.example.chess_trainer_client.data.AuthResponse
import com.example.chess_trainer_client.data.UserResponse
import com.example.chess_trainer_client.data.local.SessionManager
import com.example.chess_trainer_client.data.local.db.UserSessionStore
import com.example.chess_trainer_client.data.local.db.UserSessionEntity
import com.example.chess_trainer_client.data.network.ApiService
import com.example.chess_trainer_client.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val sessionManager: SessionManager,
    private val userSessionStore: UserSessionStore
) {
    suspend fun register(email: String, password: String): Result<AuthResponse> {
        val result = safeCall { apiService.register(AuthRegisterRequest(email, password)) }
        result.getOrNull()?.let { persistSession(it) }
        return result
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        val result = safeCall { apiService.login(AuthLoginRequest(email, password)) }
        result.getOrNull()?.let { persistSession(it) }
        return result
    }

    suspend fun me(token: String): Result<UserResponse> {
        return safeCall { apiService.me("Bearer $token") }
    }

    private suspend fun persistSession(response: AuthResponse) {
        sessionManager.saveSession(response.token)
        val session = UserSessionEntity(
            userId = response.id,
            email = response.email,
            lastLoginAt = System.currentTimeMillis(),
            tokenLast4 = response.token.takeLast(4)
        )
        withContext(Dispatchers.IO) {
            userSessionStore.upsert(session)
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}

