package com.example.chess_trainer_client.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class GameStateTest {
    @Test
    fun applyLegalMoveUpdatesState() {
        val game = GameState()

        val result = game.applyMove(Square.fromAlgebraic("e2"), Square.fromAlgebraic("e4"))

        assertTrue(result is MoveResult.Success)
        assertEquals(PieceColor.BLACK, game.activeColor)
        assertEquals(PieceType.PAWN, game.board.getPiece(Square.fromAlgebraic("e4"))?.type)
    }

    @Test
    fun illegalMoveDoesNotChangeBoard() {
        val game = GameState()

        val result = game.applyMove(Square.fromAlgebraic("e2"), Square.fromAlgebraic("e5"))

        assertTrue(result is MoveResult.IllegalMove)
        assertEquals(PieceType.PAWN, game.board.getPiece(Square.fromAlgebraic("e2"))?.type)
    }

    @Test
    fun undoRestoresPreviousState() {
        val game = GameState()
        game.applyMove(Square.fromAlgebraic("e2"), Square.fromAlgebraic("e4"))

        val undone = game.undoLastMove()

        assertTrue(undone)
        assertEquals(PieceColor.WHITE, game.activeColor)
        assertEquals(PieceType.PAWN, game.board.getPiece(Square.fromAlgebraic("e2"))?.type)
        assertFalse(game.board.getPiece(Square.fromAlgebraic("e4")) != null)
        assertEquals(CastlingRights(true, true, true, true), game.board.castlingRights)
    }

    @Test
    fun checkmateReturnsCorrectResult() {
        val game = GameState()

        game.applyMove(Square.fromAlgebraic("f2"), Square.fromAlgebraic("f3"))
        game.applyMove(Square.fromAlgebraic("e7"), Square.fromAlgebraic("e5"))
        game.applyMove(Square.fromAlgebraic("g2"), Square.fromAlgebraic("g4"))
        val result = game.applyMove(Square.fromAlgebraic("d8"), Square.fromAlgebraic("h4"))

        assertTrue(result is MoveResult.Checkmate)
        val winner = (result as MoveResult.Checkmate).winner
        assertEquals(PieceColor.BLACK, winner)
    }
}

