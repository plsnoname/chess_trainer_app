package com.example.chess_trainer_client.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chess_trainer_client.data.Puzzle
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

sealed class PuzzleResult {
    object Idle : PuzzleResult()
    object Correct : PuzzleResult()
    object Incorrect : PuzzleResult()
    object Completed : PuzzleResult()
}

class PuzzleViewModel(
    application: Application,
    private val repository: ChessRepository = ChessRepository()
) : AndroidViewModel(application) {
    private val gameState = GameState()
    private val puzzles = mutableListOf<Puzzle>()
    private var currentIndex = 0
    private var solutionIndex = 0

    private val _puzzle = MutableLiveData<Puzzle?>(null)
    val puzzle: LiveData<Puzzle?> = _puzzle

    private val _board = MutableLiveData<Board>(gameState.board)
    val board: LiveData<Board> = _board

    private val _result = MutableLiveData<PuzzleResult>(PuzzleResult.Idle)
    val result: LiveData<PuzzleResult> = _result

    private val _legalMoves = MutableLiveData<List<Square>>(emptyList())
    val legalMoves: LiveData<List<Square>> = _legalMoves

    private val _selectedSquare = MutableLiveData<Square?>(null)
    val selectedSquare: LiveData<Square?> = _selectedSquare

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        loadPuzzles()
    }

    fun onSquareTapped(square: Square) {
        if (_loading.value == true || _result.value == PuzzleResult.Completed) {
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
            val expectedMove = currentSolutionMove()
            val moveString = selected.toAlgebraic() + square.toAlgebraic()
            val result = gameState.applyMove(selected, square)
            _board.value = gameState.board
            _legalMoves.value = emptyList()
            _selectedSquare.value = null

            if (result is MoveResult.IllegalMove || expectedMove == null || expectedMove != moveString) {
                gameState.undoLastMove()
                _board.value = gameState.board
                _result.value = PuzzleResult.Incorrect
                return
            }

            solutionIndex += 1
            _result.value = if (solutionIndex >= puzzleSolutionSize()) {
                PuzzleResult.Completed
            } else {
                PuzzleResult.Correct
            }
        } else {
            _legalMoves.value = emptyList()
            _selectedSquare.value = null
        }
    }

    fun nextPuzzle() {
        if (currentIndex + 1 >= puzzles.size) {
            _error.value = "No more puzzles available."
            return
        }
        currentIndex += 1
        loadPuzzle(puzzles[currentIndex])
    }

    fun retryPuzzle() {
        val puzzle = _puzzle.value ?: return
        loadPuzzle(puzzle)
    }

    private fun loadPuzzles() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            _error.postValue(null)

            val puzzleList = repository.getPuzzles().getOrNull() ?: run {
                try {
                    repository.loadLocalPuzzles(getApplication())
                } catch (exception: Exception) {
                    emptyList()
                }
            }

            withContext(Dispatchers.Main) {
                puzzles.clear()
                puzzles.addAll(puzzleList)
                if (puzzles.isEmpty()) {
                    _error.value = "No puzzles available."
                    _loading.value = false
                    return@withContext
                }
                currentIndex = 0
                loadPuzzle(puzzles[currentIndex])
                _loading.value = false
            }
        }
    }

    private fun loadPuzzle(puzzle: Puzzle) {
        val activeColor = parseActiveColor(puzzle.fen)
        gameState.loadFromFen(puzzle.fen, activeColor)
        solutionIndex = 0
        _puzzle.value = puzzle
        _board.value = gameState.board
        _result.value = PuzzleResult.Idle
        _legalMoves.value = emptyList()
        _selectedSquare.value = null
        _error.value = null
    }

    private fun parseActiveColor(fen: String): PieceColor {
        val parts = fen.trim().split(" ")
        if (parts.size >= 2) {
            return if (parts[1].lowercase() == "b") PieceColor.BLACK else PieceColor.WHITE
        }
        return PieceColor.WHITE
    }

    private fun currentSolutionMove(): String? {
        val solution = _puzzle.value?.solution ?: return null
        return solution.getOrNull(solutionIndex)
    }

    private fun puzzleSolutionSize(): Int {
        return _puzzle.value?.solution?.size ?: 0
    }

}

