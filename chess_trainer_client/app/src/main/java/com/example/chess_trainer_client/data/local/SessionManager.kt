package com.example.chess_trainer_client.data.local

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun clearSession() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val PREFS_NAME = "auth_session"
        const val KEY_TOKEN = "auth_token"
    }
}

