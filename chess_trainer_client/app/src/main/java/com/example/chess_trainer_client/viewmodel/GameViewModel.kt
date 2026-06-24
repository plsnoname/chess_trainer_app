package com.example.chess_trainer_client.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess_trainer_client.engine.Board
import com.example.chess_trainer_client.engine.GameState
import com.example.chess_trainer_client.engine.MoveResult
import com.example.chess_trainer_client.engine.PieceColor
import com.example.chess_trainer_client.engine.PieceType
import com.example.chess_trainer_client.engine.Square
import com.example.chess_trainer_client.engine.MoveGenerator
import com.example.chess_trainer_client.engine.AIEngine
import com.example.chess_trainer_client.data.local.db.SavedGameEntity
import com.example.chess_trainer_client.data.local.db.SavedGameStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class GameResult {
    object Ongoing : GameResult()
    data class Checkmate(val winner: PieceColor) : GameResult()
    object Stalemate : GameResult()
}

class GameViewModel : ViewModel() {
    private val gameState = GameState()
    private var savedGameStore: SavedGameStore? = null
    private var gameMode = "two_player"

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

    private val _promotionRequest = MutableLiveData<PromotionRequest?>(null)
    val promotionRequest: LiveData<PromotionRequest?> = _promotionRequest

    private var isAiMode = false

    private val _aiThinking = MutableLiveData(false)
    val aiThinking: LiveData<Boolean> = _aiThinking

    fun setGameStore(store: SavedGameStore) {
        savedGameStore = store
    }

    fun setAiMode(enabled: Boolean) {
        isAiMode = enabled
        gameMode = if (enabled) "ai_vs_player" else "two_player"
    }

    fun onSquareTapped(square: Square) {
        if (_aiThinking.value == true) {
            return
        }
        if (_result.value != GameResult.Ongoing) {
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
            val piece = gameState.board.getPiece(selected)
            if (piece?.type == PieceType.PAWN && (square.rank == 0 || square.rank == 7)) {
                _promotionRequest.value = PromotionRequest(selected, square, piece.color)
                _legalMoves.value = emptyList()
                _selectedSquare.value = null
                return
            }
            applyMoveAndUpdate(selected, square, null)
        } else {
            _legalMoves.value = emptyList()
            _selectedSquare.value = null
        }
    }

    fun confirmPromotion(type: PieceType) {
        val request = _promotionRequest.value ?: return
        _promotionRequest.value = null
        applyMoveAndUpdate(request.from, request.to, type)
    }

    fun undoLastMove(): Boolean {
        if (_aiThinking.value == true) {
            return false
        }
        
        if (isAiMode) {
            // In AI mode, undo both the AI's move and the player's previous move
            val undone1 = gameState.undoLastMove()
            if (!undone1) return false
            
            val undone2 = gameState.undoLastMove()
            if (!undone2) return false
            
            _board.value = gameState.board
            _activeColor.value = gameState.activeColor
            _legalMoves.value = emptyList()
            _selectedSquare.value = null
            _result.value = GameResult.Ongoing
            _aiThinking.value = false
            return true
        } else {
            // In two-player mode, just undo one move
            val undone = gameState.undoLastMove()
            if (undone) {
                _board.value = gameState.board
                _activeColor.value = gameState.activeColor
                _legalMoves.value = emptyList()
                _selectedSquare.value = null
                _result.value = GameResult.Ongoing
                _aiThinking.value = false
            }
            return undone
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
        _promotionRequest.value = null
    }

    private fun applyMoveAndUpdate(from: Square, to: Square, promotion: PieceType?) {
        val result = gameState.applyMove(from, to, promotion)
        updateStateFromResult(result)

        if (isAiMode && gameState.activeColor == PieceColor.BLACK && _result.value == GameResult.Ongoing) {
            triggerAiMove()
        }
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

    fun saveGame(name: String? = null): Boolean {
        if (savedGameStore == null) return false
        
        val gameNameFinal = name ?: generateGameName()
        val entity = SavedGameEntity(
            id = 0L,
            name = gameNameFinal,
            fen = gameState.toFen(),
            activeColor = gameState.activeColor.name,
            mode = gameMode,
            timestamp = System.currentTimeMillis()
        )
        val result = savedGameStore?.upsert(entity)
        android.util.Log.d("GameViewModel", "Saved game: $gameNameFinal with FEN: ${entity.fen}, insert result: $result")
        return true
    }

    fun loadGame(entity: SavedGameEntity): Boolean {
        try {
            gameState.loadFromFen(entity.fen, PieceColor.valueOf(entity.activeColor))
            gameMode = entity.mode
            isAiMode = entity.mode == "ai_vs_player"
            
            _board.value = gameState.board
            _activeColor.value = gameState.activeColor
            _legalMoves.value = emptyList()
            _selectedSquare.value = null
            _result.value = GameResult.Ongoing
            _aiThinking.value = false
            _promotionRequest.value = null
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun generateGameName(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return "Game - ${sdf.format(Date())}"
    }
}

data class PromotionRequest(val from: Square, val to: Square, val color: PieceColor)
