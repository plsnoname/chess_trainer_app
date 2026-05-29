package com.example.chess_trainer_client.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveGeneratorTest {
    @Test
    fun startingPositionHasTwentyLegalMoves() {
        val board = Board()
        board.initializeStartingPosition()

        val moves = MoveGenerator.getAllLegalMoves(board, PieceColor.WHITE)
        assertEquals(20, moves.size)
    }

    @Test
    fun pinnedPieceIsRestricted() {
        val board = Board()
        board.loadFromFen("4r3/8/8/8/8/8/4R3/4K3")

        val moves = MoveGenerator.getLegalMoves(board, Square.fromAlgebraic("e2"))
            .map { it.toAlgebraic() }
            .sorted()

        assertEquals(listOf("e3", "e4", "e5", "e6", "e7", "e8"), moves)
    }

    @Test
    fun kingCannotMoveIntoCheck() {
        val board = Board()
        board.loadFromFen("4r3/8/8/8/8/8/8/4K3")

        val moves = MoveGenerator.getLegalMoves(board, Square.fromAlgebraic("e1"))
            .map { it.toAlgebraic() }

        assertFalse(moves.contains("e2"))
    }

    @Test
    fun castlingMovesGeneratedWhenLegal() {
        val board = Board()
        board.loadFromFen("8/8/8/8/8/8/8/R3K2R")
        board.castlingRights = CastlingRights(whiteKingSide = true, whiteQueenSide = true)

        val moves = MoveGenerator.getLegalMoves(board, Square.fromAlgebraic("e1"))
            .map { it.toAlgebraic() }

        assertTrue(moves.contains("g1"))
        assertTrue(moves.contains("c1"))
    }

    @Test
    fun castlingBlockedWhenInCheck() {
        val board = Board()
        board.loadFromFen("4r3/8/8/8/8/8/8/R3K2R")
        board.castlingRights = CastlingRights(whiteKingSide = true, whiteQueenSide = true)

        val moves = MoveGenerator.getLegalMoves(board, Square.fromAlgebraic("e1"))
            .map { it.toAlgebraic() }

        assertFalse(moves.contains("g1"))
        assertFalse(moves.contains("c1"))
    }

    @Test
    fun enPassantMoveIsAvailable() {
        val board = Board()
        board.loadFromFen("8/8/8/3pP3/8/8/8/8")
        board.enPassantTarget = Square.fromAlgebraic("d6")

        val moves = MoveGenerator.getLegalMoves(board, Square.fromAlgebraic("e5"))
            .map { it.toAlgebraic() }

        assertTrue(moves.contains("d6"))
    }

    @Test
    fun checkmateDetected() {
        val board = Board()
        board.loadFromFen("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR")

        assertTrue(MoveGenerator.isCheckmate(board, PieceColor.WHITE))
    }

    @Test
    fun stalemateDetected() {
        val board = Board()
        board.loadFromFen("7k/5Q2/6K1/8/8/8/8/8")

        assertTrue(MoveGenerator.isStalemate(board, PieceColor.BLACK))
    }
}
