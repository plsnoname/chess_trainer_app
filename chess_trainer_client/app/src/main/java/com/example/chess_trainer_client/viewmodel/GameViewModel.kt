package com.example.chess_trainer_client.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess_trainer_client.engine.Board
import com.example.chess_trainer_client.engine.GameState
import com.example.chess_trainer_client.engine.MoveResult
import com.example.chess_trainer_client.engine.PieceColor
import com.example.chess_trainer_client.engine.Square
import com.example.chess_trainer_client.engine.MoveGenerator
import com.example.chess_trainer_client.engine.AIEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class GameResult {
    object Ongoing : GameResult()
    data class Checkmate(val winner: PieceColor) : GameResult()
    object Stalemate : GameResult()
}

class GameViewModel : ViewModel() {
    private val gameState = GameState()

    private val _board = MutableLiveData<Board>(gameState.board)
    val board: LiveData<Board> = _board

    private val _activeColor = MutableLiveData<PieceColor>(gameState.activeColor)
    val activeColor: LiveData<PieceColor> = _activeColor

    private val _result = MutableLiveData<GameResult>(GameResult.Ongoing)
    val result: LiveData<GameResult> = _result

    private val _legalMoves = MutableLiveData<List<Square>>(emptyList())
    val legalMoves: LiveData<List<Square>> = _legalMoves

    private val _selectedSquare = MutableLiveData<Square?>(null)
    val selectedSquare: LiveData<Square?> = _selectedSquare

    private var isAiMode = false

    private val _aiThinking = MutableLiveData(false)
    val aiThinking: LiveData<Boolean> = _aiThinking

    fun setAiMode(enabled: Boolean) {
        isAiMode = enabled
    }

    fun onSquareTapped(square: Square) {
        if (_aiThinking.value == true) {
            return
        }

        val selected = _selectedSquare.value
        if (selected == null) {
            val piece = gameState.board.getPiece(square) ?: return
            if (piece.color != gameState.activeColor) {
                return
            }
            _selectedSquare.value = square
            _legalMoves.value = MoveGenerator.getLegalMoves(gameState.board, square)
            return
        }

        val targets = _legalMoves.value.orEmpty()
        if (square in targets) {
            val result = gameState.applyMove(selected, square)
            updateStateFromResult(result)

            if (isAiMode && gameState.activeColor == PieceColor.BLACK && _result.value == GameResult.Ongoing) {
                triggerAiMove()
            }
        } else {
            _legalMoves.value = emptyList()
            _selectedSquare.value = null
        }
    }

    fun resetGame() {
        gameState.reset()
        _board.value = gameState.board
        _activeColor.value = gameState.activeColor
        _legalMoves.value = emptyList()
        _selectedSquare.value = null
        _result.value = GameResult.Ongoing
        _aiThinking.value = false
    }

    private fun updateStateFromResult(result: MoveResult) {
        _board.value = gameState.board
        _activeColor.value = gameState.activeColor
        _legalMoves.value = emptyList()
        _selectedSquare.value = null
        _result.value = when (result) {
            is MoveResult.Checkmate -> GameResult.Checkmate(result.winner)
            MoveResult.Stalemate -> GameResult.Stalemate
            MoveResult.Success -> GameResult.Ongoing
            MoveResult.IllegalMove -> GameResult.Ongoing
        }
    }

    private fun triggerAiMove() {
        _aiThinking.value = true
        viewModelScope.launch(Dispatchers.Default) {
            val move = AIEngine.getBestMove(gameState, PieceColor.BLACK)
            withContext(Dispatchers.Main) {
                if (move == null) {
                    _aiThinking.value = false
                    val result = when {
                        MoveGenerator.isCheckmate(gameState.board, PieceColor.BLACK) -> GameResult.Checkmate(PieceColor.WHITE)
                        MoveGenerator.isStalemate(gameState.board, PieceColor.BLACK) -> GameResult.Stalemate
                        else -> GameResult.Ongoing
                    }
                    _result.value = result
                    return@withContext
                }
                val result = gameState.applyMove(move.first, move.second)
                updateStateFromResult(result)
                _aiThinking.value = false
            }
        }
    }
}
