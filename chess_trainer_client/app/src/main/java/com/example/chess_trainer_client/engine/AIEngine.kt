package com.example.chess_trainer_client.engine

import kotlin.math.abs

object AIEngine {
    private const val MAX_TIME_MS = 800L

    fun getBestMove(gameState: GameState, color: PieceColor): Pair<Square, Square>? {
        val startTime = System.nanoTime()
        val moves = MoveGenerator.getAllLegalMoves(gameState.board, color)
        if (moves.isEmpty()) {
            return null
        }

        var bestScore = Int.MIN_VALUE
        val bestMoves = mutableListOf<Move>()

        for (move in moves) {
            val boardCopy = gameState.board.copy()
            applyMove(boardCopy, move)
            val score = Evaluator.evaluate(boardCopy, color)

            when {
                score > bestScore -> {
                    bestScore = score
                    bestMoves.clear()
                    bestMoves += move
                }
                score == bestScore -> bestMoves += move
            }

            val elapsedMs = (System.nanoTime() - startTime) / 1_000_000
            if (elapsedMs > MAX_TIME_MS) {
                break
            }
        }

        val chosenMove = if (bestMoves.isNotEmpty()) bestMoves.random() else moves.first()
        return chosenMove.from to chosenMove.to
    }

    private fun applyMove(board: Board, move: Move) {
        if (move.isCastling) {
            board.setPiece(move.from, null)
            board.setPiece(move.to, move.piece)
            val rookFrom = if (move.to.file > move.from.file) Square(7, move.from.rank) else Square(0, move.from.rank)
            val rookTo = if (move.to.file > move.from.file) Square(5, move.from.rank) else Square(3, move.from.rank)
            val rook = board.getPiece(rookFrom)
            board.setPiece(rookFrom, null)
            board.setPiece(rookTo, rook)
            board.enPassantTarget = null
            return
        }

        if (move.isEnPassant) {
            val capturedSquare = Square(move.to.file, move.from.rank)
            board.setPiece(capturedSquare, null)
        }

        val movedPiece = if (move.promotion != null) Piece(move.promotion, move.piece.color) else move.piece
        board.setPiece(move.from, null)
        board.setPiece(move.to, movedPiece)

        board.enPassantTarget = if (move.piece.type == PieceType.PAWN && abs(move.to.rank - move.from.rank) == 2) {
            Square(move.from.file, (move.from.rank + move.to.rank) / 2)
        } else {
            null
        }
    }
}

