package com.example.chess_trainer_client.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AIEngineTest {
    @Test
    fun takesFreeQueenCapture() {
        val game = GameState()
        game.loadFromFen("q3k3/8/8/8/8/8/8/R3K3", PieceColor.WHITE)

        val move = AIEngine.getBestMove(game, PieceColor.WHITE)

        assertNotNull(move)
        assertEquals(Square.fromAlgebraic("a1"), move?.first)
        assertEquals(Square.fromAlgebraic("a8"), move?.second)
    }

    @Test
    fun chosenMoveIsLegal() {
        val game = GameState()
        val move = AIEngine.getBestMove(game, PieceColor.WHITE)

        assertNotNull(move)
        val legalTargets = MoveGenerator.getLegalMoves(game.board, move!!.first)
        assertTrue(legalTargets.contains(move.second))
    }

    @Test
    fun returnsNullWhenNoLegalMoves() {
        val game = GameState()
        game.loadFromFen("7k/5Q2/6K1/8/8/8/8/8", PieceColor.BLACK)

        val move = AIEngine.getBestMove(game, PieceColor.BLACK)

        assertEquals(null, move)
    }
}

