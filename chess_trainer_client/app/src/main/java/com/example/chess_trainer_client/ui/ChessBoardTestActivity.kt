package com.example.chess_trainer_client.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.chess_trainer_client.R
import com.example.chess_trainer_client.engine.Board

class ChessBoardTestActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chessboard_test)

        val boardView = findViewById<ChessBoardView>(R.id.chess_board_view)
        val board = Board().apply { initializeStartingPosition() }
        boardView.setBoard(board)
    }
}

