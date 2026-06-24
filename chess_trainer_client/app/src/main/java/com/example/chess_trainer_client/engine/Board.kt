package com.example.chess_trainer_client.engine

class Board {
    private val grid = Array(8) { arrayOfNulls<Piece>(8) }

    var castlingRights: CastlingRights = CastlingRights()
    var enPassantTarget: Square? = null

    fun clear() {
        for (rank in 0..7) {
            for (file in 0..7) {
                grid[rank][file] = null
            }
        }
    }

    fun initializeStartingPosition() {
        clear()
        castlingRights = CastlingRights(
            whiteKingSide = true,
            whiteQueenSide = true,
            blackKingSide = true,
            blackQueenSide = true
        )
        enPassantTarget = null

        for (file in 0..7) {
            setPiece(Square(file, 1), Piece(PieceType.PAWN, PieceColor.WHITE))
            setPiece(Square(file, 6), Piece(PieceType.PAWN, PieceColor.BLACK))
        }

        setPiece(Square(0, 0), Piece(PieceType.ROOK, PieceColor.WHITE))
        setPiece(Square(7, 0), Piece(PieceType.ROOK, PieceColor.WHITE))
        setPiece(Square(1, 0), Piece(PieceType.KNIGHT, PieceColor.WHITE))
        setPiece(Square(6, 0), Piece(PieceType.KNIGHT, PieceColor.WHITE))
        setPiece(Square(2, 0), Piece(PieceType.BISHOP, PieceColor.WHITE))
        setPiece(Square(5, 0), Piece(PieceType.BISHOP, PieceColor.WHITE))
        setPiece(Square(3, 0), Piece(PieceType.QUEEN, PieceColor.WHITE))
        setPiece(Square(4, 0), Piece(PieceType.KING, PieceColor.WHITE))

        setPiece(Square(0, 7), Piece(PieceType.ROOK, PieceColor.BLACK))
        setPiece(Square(7, 7), Piece(PieceType.ROOK, PieceColor.BLACK))
        setPiece(Square(1, 7), Piece(PieceType.KNIGHT, PieceColor.BLACK))
        setPiece(Square(6, 7), Piece(PieceType.KNIGHT, PieceColor.BLACK))
        setPiece(Square(2, 7), Piece(PieceType.BISHOP, PieceColor.BLACK))
        setPiece(Square(5, 7), Piece(PieceType.BISHOP, PieceColor.BLACK))
        setPiece(Square(3, 7), Piece(PieceType.QUEEN, PieceColor.BLACK))
        setPiece(Square(4, 7), Piece(PieceType.KING, PieceColor.BLACK))
    }

    fun getPiece(square: Square): Piece? = grid[square.rank][square.file]

    fun setPiece(square: Square, piece: Piece?) {
        grid[square.rank][square.file] = piece
    }

    fun movePiece(from: Square, to: Square) {
        val piece = getPiece(from)
        setPiece(to, piece)
        setPiece(from, null)
    }

    fun loadFromFen(fen: String) {
        clear()
        castlingRights = CastlingRights()
        enPassantTarget = null

        val parts = fen.trim().split(Regex("\\s+"))
        val placement = parts[0]
        val ranks = placement.split("/")
        require(ranks.size == 8) { "FEN must have 8 ranks" }

        for (rankIndex in 0..7) {
            var file = 0
            for (symbol in ranks[rankIndex]) {
                if (symbol.isDigit()) {
                    file += symbol.digitToInt()
                } else {
                    val piece = when (symbol) {
                        'K' -> Piece(PieceType.KING, PieceColor.WHITE)
                        'Q' -> Piece(PieceType.QUEEN, PieceColor.WHITE)
                        'R' -> Piece(PieceType.ROOK, PieceColor.WHITE)
                        'B' -> Piece(PieceType.BISHOP, PieceColor.WHITE)
                        'N' -> Piece(PieceType.KNIGHT, PieceColor.WHITE)
                        'P' -> Piece(PieceType.PAWN, PieceColor.WHITE)
                        'k' -> Piece(PieceType.KING, PieceColor.BLACK)
                        'q' -> Piece(PieceType.QUEEN, PieceColor.BLACK)
                        'r' -> Piece(PieceType.ROOK, PieceColor.BLACK)
                        'b' -> Piece(PieceType.BISHOP, PieceColor.BLACK)
                        'n' -> Piece(PieceType.KNIGHT, PieceColor.BLACK)
                        'p' -> Piece(PieceType.PAWN, PieceColor.BLACK)
                        else -> throw IllegalArgumentException("Invalid FEN symbol: $symbol")
                    }
                    setPiece(Square(file, 7 - rankIndex), piece)
                    file += 1
                }
            }
            require(file == 8) { "Invalid FEN rank width" }
        }

        if (parts.size >= 3) {
            castlingRights = parseCastlingRights(parts[2])
        }
        if (parts.size >= 4) {
            enPassantTarget = if (parts[3] == "-") null else Square.fromAlgebraic(parts[3])
        }
    }

    fun toFenPlacement(): String {
        val ranks = mutableListOf<String>()
        for (rank in 7 downTo 0) {
            var emptyCount = 0
            val builder = StringBuilder()
            for (file in 0..7) {
                val piece = getPiece(Square(file, rank))
                if (piece == null) {
                    emptyCount += 1
                } else {
                    if (emptyCount > 0) {
                        builder.append(emptyCount)
                        emptyCount = 0
                    }
                    builder.append(pieceSymbol(piece))
                }
            }
            if (emptyCount > 0) {
                builder.append(emptyCount)
            }
            ranks.add(builder.toString())
        }
        return ranks.joinToString("/")
    }

    private fun parseCastlingRights(value: String): CastlingRights {
        if (value == "-") return CastlingRights()
        return CastlingRights(
            whiteKingSide = value.contains('K'),
            whiteQueenSide = value.contains('Q'),
            blackKingSide = value.contains('k'),
            blackQueenSide = value.contains('q')
        )
    }

    private fun pieceSymbol(piece: Piece): Char {
        return when (piece.type) {
            PieceType.KING -> if (piece.color == PieceColor.WHITE) 'K' else 'k'
            PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) 'Q' else 'q'
            PieceType.ROOK -> if (piece.color == PieceColor.WHITE) 'R' else 'r'
            PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) 'B' else 'b'
            PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) 'N' else 'n'
            PieceType.PAWN -> if (piece.color == PieceColor.WHITE) 'P' else 'p'
        }
    }

    fun copy(): Board {
        val board = Board()
        for (rank in 0..7) {
            for (file in 0..7) {
                board.grid[rank][file] = grid[rank][file]
            }
        }
        board.castlingRights = castlingRights.copy()
        board.enPassantTarget = enPassantTarget
        return board
    }

    fun findKing(color: PieceColor): Square? {
        for (rank in 0..7) {
            for (file in 0..7) {
                val piece = grid[rank][file]
                if (piece?.type == PieceType.KING && piece.color == color) {
                    return Square(file, rank)
                }
            }
        }
        return null
    }
}

