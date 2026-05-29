package com.example.chess_trainer_client.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluatorTest {
    @Test
    fun startingPositionIsBalanced() {
        val board = Board()
        board.initializeStartingPosition()

        val score = Evaluator.evaluate(board, PieceColor.WHITE)
        assertEquals(0, score)
    }

    @Test
    fun missingPawnIsNegative() {
        val board = Board()
        board.initializeStartingPosition()
        board.setPiece(Square.fromAlgebraic("a2"), null)

        val score = Evaluator.evaluate(board, PieceColor.WHITE)
        assertTrue(score < 0)
    }

    @Test
    fun extraQueenIsPositive() {
        val board = Board()
        board.initializeStartingPosition()
        board.setPiece(Square.fromAlgebraic("a3"), Piece(PieceType.QUEEN, PieceColor.WHITE))

        val score = Evaluator.evaluate(board, PieceColor.WHITE)
        assertTrue(score > 800)
    }
}

