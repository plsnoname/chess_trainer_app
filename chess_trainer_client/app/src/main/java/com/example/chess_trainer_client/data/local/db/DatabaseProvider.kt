package com.example.chess_trainer_client.data.local.db

import android.content.Context

object DatabaseProvider {
    @Volatile
    private var instance: UserSessionDatabase? = null

    fun getInstance(context: Context): UserSessionDatabase {
        return instance ?: synchronized(this) {
            instance ?: UserSessionDatabase(context.applicationContext).also { instance = it }
        }
    }
}

