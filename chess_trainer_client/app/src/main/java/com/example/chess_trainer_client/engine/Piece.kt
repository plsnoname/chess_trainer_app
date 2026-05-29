package com.example.chess_trainer_client.engine

enum class PieceType {
    KING,
    QUEEN,
    ROOK,
    BISHOP,
    KNIGHT,
    PAWN
}

enum class PieceColor {
    WHITE,
    BLACK;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
}

data class Piece(val type: PieceType, val color: PieceColor)

data class Square(val file: Int, val rank: Int) {
    init {
        require(file in 0..7) { "file must be 0..7" }
        require(rank in 0..7) { "rank must be 0..7" }
    }

    fun toAlgebraic(): String {
        val fileChar = ('a'.code + file).toChar()
        val rankChar = ('1'.code + rank).toChar()
        return "$fileChar$rankChar"
    }

    companion object {
        fun fromAlgebraic(value: String): Square {
            require(value.length == 2) { "square must be 2 chars" }
            val file = value[0].lowercaseChar() - 'a'
            val rank = value[1] - '1'
            return Square(file, rank)
        }
    }
}

data class CastlingRights(
    var whiteKingSide: Boolean = false,
    var whiteQueenSide: Boolean = false,
    var blackKingSide: Boolean = false,
    var blackQueenSide: Boolean = false
)

data class Move(
    val from: Square,
    val to: Square,
    val piece: Piece,
    val captured: Piece? = null,
    val promotion: PieceType? = null,
    val isEnPassant: Boolean = false,
    val isCastling: Boolean = false
)

