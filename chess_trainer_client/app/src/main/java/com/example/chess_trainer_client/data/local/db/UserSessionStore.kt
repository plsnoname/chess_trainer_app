package com.example.chess_trainer_client.data.local.db

import android.content.ContentValues

class UserSessionStore(private val database: UserSessionDatabase) {
    fun upsert(session: UserSessionEntity) {
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, session.userId)
            put(COLUMN_EMAIL, session.email)
            put(COLUMN_LAST_LOGIN_AT, session.lastLoginAt)
            put(COLUMN_TOKEN_LAST4, session.tokenLast4)
        }
        database.writableDatabase.insertWithOnConflict(
            TABLE_NAME,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getLatest(): UserSessionEntity? {
        val cursor = database.readableDatabase.query(
            TABLE_NAME,
            arrayOf(COLUMN_USER_ID, COLUMN_EMAIL, COLUMN_LAST_LOGIN_AT, COLUMN_TOKEN_LAST4),
            null,
            null,
            null,
            null,
            "${COLUMN_LAST_LOGIN_AT} DESC",
            "1"
        )
        cursor.use {
            if (!it.moveToFirst()) return null
            return UserSessionEntity(
                userId = it.getLong(it.getColumnIndexOrThrow(COLUMN_USER_ID)),
                email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                lastLoginAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_LAST_LOGIN_AT)),
                tokenLast4 = it.getString(it.getColumnIndexOrThrow(COLUMN_TOKEN_LAST4))
            )
        }
    }

    fun clear() {
        database.writableDatabase.delete(TABLE_NAME, null, null)
    }

    companion object {
        const val TABLE_NAME = "user_session"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_LAST_LOGIN_AT = "last_login_at"
        const val COLUMN_TOKEN_LAST4 = "token_last4"
    }
}


