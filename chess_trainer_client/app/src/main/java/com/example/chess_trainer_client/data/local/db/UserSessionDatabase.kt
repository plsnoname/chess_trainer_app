package com.example.chess_trainer_client.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserSessionDatabase(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS ${UserSessionStore.TABLE_NAME} (
                    ${UserSessionStore.COLUMN_USER_ID} INTEGER PRIMARY KEY,
                    ${UserSessionStore.COLUMN_EMAIL} TEXT NOT NULL,
                    ${UserSessionStore.COLUMN_LAST_LOGIN_AT} INTEGER NOT NULL,
                    ${UserSessionStore.COLUMN_TOKEN_LAST4} TEXT NOT NULL
                )
            """.trimIndent()
        )
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS ${SavedGameStore.TABLE_NAME} (
                    ${SavedGameStore.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                    ${SavedGameStore.COLUMN_NAME} TEXT,
                    ${SavedGameStore.COLUMN_FEN} TEXT NOT NULL,
                    ${SavedGameStore.COLUMN_ACTIVE_COLOR} TEXT NOT NULL,
                    ${SavedGameStore.COLUMN_MODE} TEXT NOT NULL,
                    ${SavedGameStore.COLUMN_TIMESTAMP} INTEGER NOT NULL
                )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${UserSessionStore.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${SavedGameStore.TABLE_NAME}")
        onCreate(db)
    }

    private companion object {
        const val DATABASE_NAME = "chess_trainer.db"
        const val DATABASE_VERSION = 3
    }
}


