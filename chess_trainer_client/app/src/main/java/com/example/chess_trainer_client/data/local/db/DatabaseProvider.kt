package com.example.chess_trainer_client.data.local.db

import android.content.Context

object DatabaseProvider {
    @Volatile
    private var instance: UserSessionDatabase? = null
    
    @Volatile
    private var savedGameStore: SavedGameStore? = null

    fun getInstance(context: Context): UserSessionDatabase {
        return instance ?: synchronized(this) {
            instance ?: UserSessionDatabase(context.applicationContext).also { instance = it }
        }
    }

    fun getSavedGameStore(context: Context): SavedGameStore {
        return savedGameStore ?: synchronized(this) {
            savedGameStore ?: SavedGameStore(getInstance(context)).also { savedGameStore = it }
        }
    }
}

