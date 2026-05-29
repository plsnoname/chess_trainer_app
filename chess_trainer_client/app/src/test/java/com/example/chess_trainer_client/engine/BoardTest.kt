package com.example.chess_trainer_client.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BoardTest {
    @Test
    fun startingPositionIsCorrect() {
        val board = Board()
        board.initializeStartingPosition()

        assertPiece(board, "e1", PieceType.KING, PieceColor.WHITE)
        assertPiece(board, "d1", PieceType.QUEEN, PieceColor.WHITE)
        assertPiece(board, "e8", PieceType.KING, PieceColor.BLACK)
        assertPiece(board, "d8", PieceType.QUEEN, PieceColor.BLACK)

        for (file in 0..7) {
            assertPiece(board, Square(file, 1).toAlgebraic(), PieceType.PAWN, PieceColor.WHITE)
            assertPiece(board, Square(file, 6).toAlgebraic(), PieceType.PAWN, PieceColor.BLACK)
        }
    }

    @Test
    fun loadFromFenStartingPosition() {
        val board = Board()
        board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")

        assertPiece(board, "e1", PieceType.KING, PieceColor.WHITE)
        assertPiece(board, "d8", PieceType.QUEEN, PieceColor.BLACK)
        assertPiece(board, "a1", PieceType.ROOK, PieceColor.WHITE)
        assertPiece(board, "h8", PieceType.ROOK, PieceColor.BLACK)
    }

    @Test
    fun loadFromFenMidGame() {
        val board = Board()
        board.loadFromFen("8/8/8/3k4/8/8/4K3/8")

        assertPiece(board, "d5", PieceType.KING, PieceColor.BLACK)
        assertPiece(board, "e2", PieceType.KING, PieceColor.WHITE)
        assertNull(board.getPiece(Square.fromAlgebraic("a1")))
    }

    private fun assertPiece(board: Board, square: String, type: PieceType, color: PieceColor) {
        val piece = board.getPiece(Square.fromAlgebraic(square))
        assertEquals(type, piece?.type)
        assertEquals(color, piece?.color)
    }
}

