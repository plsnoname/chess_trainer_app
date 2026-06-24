package com.example.chess_trainer_client.data.local.db

import android.content.ContentValues

class SavedGameStore(private val database: UserSessionDatabase) {
    fun getLatestTimestamp(): Long? {
        val cursor = database.readableDatabase.query(
            TABLE_NAME,
            arrayOf(COLUMN_TIMESTAMP),
            null,
            null,
            null,
            null,
            "${COLUMN_TIMESTAMP} DESC",
            "1"
        )
        cursor.use {
            if (!it.moveToFirst()) return null
            return it.getLong(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
        }
    }

    fun upsert(game: SavedGameEntity): Long {
        val values = ContentValues().apply {
            put(COLUMN_NAME, game.name)
            put(COLUMN_FEN, game.fen)
            put(COLUMN_ACTIVE_COLOR, game.activeColor)
            put(COLUMN_MODE, game.mode)
            put(COLUMN_TIMESTAMP, game.timestamp)
        }
        // For new games (id=0), use insert to let database generate id
        // For existing games (id>0), could use update but for now just insert new
        return database.writableDatabase.insert(
            TABLE_NAME,
            null,
            values
        )
    }

    fun getAllSavedGames(): List<SavedGameEntity> {
        val games = mutableListOf<SavedGameEntity>()
        val cursor = database.readableDatabase.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_FEN, COLUMN_ACTIVE_COLOR, COLUMN_MODE, COLUMN_TIMESTAMP),
            null,
            null,
            null,
            null,
            "${COLUMN_TIMESTAMP} DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                games.add(
                    SavedGameEntity(
                        id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                        fen = it.getString(it.getColumnIndexOrThrow(COLUMN_FEN)),
                        activeColor = it.getString(it.getColumnIndexOrThrow(COLUMN_ACTIVE_COLOR)),
                        mode = it.getString(it.getColumnIndexOrThrow(COLUMN_MODE)),
                        timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                    )
                )
            }
        }
        return games
    }

    fun getSavedGameById(id: Long): SavedGameEntity? {
        val cursor = database.readableDatabase.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_FEN, COLUMN_ACTIVE_COLOR, COLUMN_MODE, COLUMN_TIMESTAMP),
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (!it.moveToFirst()) return null
            return SavedGameEntity(
                id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                fen = it.getString(it.getColumnIndexOrThrow(COLUMN_FEN)),
                activeColor = it.getString(it.getColumnIndexOrThrow(COLUMN_ACTIVE_COLOR)),
                mode = it.getString(it.getColumnIndexOrThrow(COLUMN_MODE)),
                timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
            )
        }
    }

    fun delete(id: Long): Int {
        return database.writableDatabase.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    companion object {
        const val TABLE_NAME = "saved_games"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_FEN = "fen"
        const val COLUMN_ACTIVE_COLOR = "active_color"
        const val COLUMN_MODE = "mode"
        const val COLUMN_TIMESTAMP = "timestamp"
    }
}

