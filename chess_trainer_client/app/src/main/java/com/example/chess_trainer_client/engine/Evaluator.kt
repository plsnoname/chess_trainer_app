package com.example.chess_trainer_client.engine

object Evaluator {
    fun evaluate(board: Board, color: PieceColor): Int {
        var score = 0
        for (rank in 0..7) {
            for (file in 0..7) {
                val piece = board.getPiece(Square(file, rank)) ?: continue
                val value = when (piece.type) {
                    PieceType.PAWN -> 100
                    PieceType.KNIGHT -> 300
                    PieceType.BISHOP -> 300
                    PieceType.ROOK -> 500
                    PieceType.QUEEN -> 900
                    PieceType.KING -> 10000
                }
                score += if (piece.color == color) value else -value
            }
        }

        if (MoveGenerator.isInCheck(board, color.opposite())) {
            score += 50
        }

        return score
    }
}

