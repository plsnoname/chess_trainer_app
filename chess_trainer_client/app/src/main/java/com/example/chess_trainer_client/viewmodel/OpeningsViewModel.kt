package com.example.chess_trainer_client.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chess_trainer_client.data.Opening
import com.example.chess_trainer_client.data.repository.ChessRepository
import com.example.chess_trainer_client.engine.Board
import com.example.chess_trainer_client.engine.GameState
import com.example.chess_trainer_client.engine.MoveGenerator
import com.example.chess_trainer_client.engine.MoveResult
import com.example.chess_trainer_client.engine.PieceColor
import com.example.chess_trainer_client.engine.Square
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class OpeningMode {
    STUDY,
    TRAINING
}

sealed class TrainingResult {
    object Idle : TrainingResult()
    object Correct : TrainingResult()
    object Incorrect : TrainingResult()
}

class OpeningsViewModel(
    application: Application,
    private val repository: ChessRepository = ChessRepository()
) : AndroidViewModel(application) {
    private val gameState = GameState()
    private val openings = mutableListOf<Opening>()

    private val _openings = MutableLiveData<List<Opening>>(emptyList())
    val openingsList: LiveData<List<Opening>> = _openings

    private val _selectedOpening = MutableLiveData<Opening?>(null)
    val selectedOpening: LiveData<Opening?> = _selectedOpening

    private val _board = MutableLiveData<Board>(gameState.board)
    val board: LiveData<Board> = _board

    private val _moveIndex = MutableLiveData(0)
    val moveIndex: LiveData<Int> = _moveIndex

    private val _mode = MutableLiveData(OpeningMode.STUDY)
    val mode: LiveData<OpeningMode> = _mode

    private val _trainingResult = MutableLiveData<TrainingResult>(TrainingResult.Idle)
    val trainingResult: LiveData<TrainingResult> = _trainingResult

    private val _legalMoves = MutableLiveData<List<Square>>(emptyList())
    val legalMoves: LiveData<List<Square>> = _legalMoves

    private val _selectedSquare = MutableLiveData<Square?>(null)
    val selectedSquare: LiveData<Square?> = _selectedSquare

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        loadOpenings()
    }

    fun selectOpening(opening: Opening) {
        _selectedOpening.value = opening
        resetToStartingPosition()
    }

    fun resetOpening() {
        resetToStartingPosition()
    }

    fun switchMode(newMode: OpeningMode) {
        _mode.value = newMode
        _trainingResult.value = TrainingResult.Idle
        _legalMoves.value = emptyList()
        _selectedSquare.value = null
    }

    fun nextMove() {
        if (_mode.value != OpeningMode.STUDY) return
        val opening = _selectedOpening.value ?: return
        val index = _moveIndex.value ?: 0
        if (index >= opening.moves.size) return

        val move = opening.moves[index]
        if (applyMoveFromString(move)) {
            _moveIndex.value = index + 1
            _board.value = gameState.board
        }
    }

    fun previousMove() {
        if (_mode.value != OpeningMode.STUDY) return
        val index = _moveIndex.value ?: 0
        if (index <= 0) return

        if (gameState.undoLastMove()) {
            _moveIndex.value = index - 1
            _board.value = gameState.board
        }
    }

    fun onSquareTapped(square: Square) {
        if (_mode.value != OpeningMode.TRAINING || _loading.value == true) {
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
            val opening = _selectedOpening.value ?: return
            val index = _moveIndex.value ?: 0
            val expectedMove = opening.moves.getOrNull(index)
            val moveString = selected.toAlgebraic() + square.toAlgebraic()

            val result = gameState.applyMove(selected, square)
            _board.value = gameState.board
            _legalMoves.value = emptyList()
            _selectedSquare.value = null

            if (result is MoveResult.IllegalMove || expectedMove == null || expectedMove != moveString) {
                gameState.undoLastMove()
                _board.value = gameState.board
                _trainingResult.value = TrainingResult.Incorrect
                return
            }

            _moveIndex.value = index + 1
            _trainingResult.value = TrainingResult.Correct
        } else {
            _legalMoves.value = emptyList()
            _selectedSquare.value = null
        }
    }

    private fun resetToStartingPosition() {
        gameState.reset()
        _board.value = gameState.board
        _moveIndex.value = 0
        _trainingResult.value = TrainingResult.Idle
        _legalMoves.value = emptyList()
        _selectedSquare.value = null
    }

    private fun loadOpenings() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            _error.postValue(null)

            val openingList = repository.getOpenings().getOrNull() ?: run {
                try {
                    repository.loadLocalOpenings(getApplication())
                } catch (exception: Exception) {
                    emptyList()
                }
            }

            withContext(Dispatchers.Main) {
                openings.clear()
                openings.addAll(openingList)
                _openings.value = openings.toList()
                if (openings.isEmpty()) {
                    _error.value = "No openings available."
                } else if (_selectedOpening.value == null) {
                    selectOpening(openings.first())
                }
                _loading.value = false
            }
        }
    }

    private fun applyMoveFromString(move: String): Boolean {
        if (move.length < 4) return false
        val from = Square.fromAlgebraic(move.substring(0, 2))
        val to = Square.fromAlgebraic(move.substring(2, 4))
        val result = gameState.applyMove(from, to)
        return result !is MoveResult.IllegalMove
    }
}
