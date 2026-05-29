package com.example.chess_trainer_client.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chess_trainer_client.data.local.SessionManager
import com.example.chess_trainer_client.data.local.db.DatabaseProvider
import com.example.chess_trainer_client.data.local.db.UserSessionStore
import com.example.chess_trainer_client.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application.applicationContext)
    private val database = DatabaseProvider.getInstance(application.applicationContext)
    private val userSessionStore = UserSessionStore(database)
    private val repository = AuthRepository(
        sessionManager = sessionManager,
        userSessionStore = userSessionStore
    )

    private val _state = MutableLiveData<AuthState>(AuthState.Idle)
    val state: LiveData<AuthState> = _state

    private val _isLoggedIn = MutableLiveData<Boolean>(sessionManager.isLoggedIn())
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    fun login(email: String, password: String) {
        authenticate { repository.login(email, password) }
    }

    fun register(email: String, password: String) {
        authenticate { repository.register(email, password) }
    }

    fun isLoggedInSync(): Boolean = sessionManager.isLoggedIn()

    fun clearSession() {
        sessionManager.clearSession()
        _isLoggedIn.value = false
        viewModelScope.launch {
            userSessionStore.clear()
        }
    }

    fun resetState() {
        _state.value = AuthState.Idle
    }

    private fun authenticate(call: suspend () -> Result<*>) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            val result = call()
            _state.value = result.fold(
                onSuccess = {
                    _isLoggedIn.value = true
                    AuthState.Success
                },
                onFailure = { AuthState.Error(it.message ?: "Authentication failed") }
            )
        }
    }
}

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

