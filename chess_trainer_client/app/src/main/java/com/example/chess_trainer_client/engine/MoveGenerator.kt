package com.example.chess_trainer_client.engine

import kotlin.math.abs

object MoveGenerator {
    fun getLegalMoves(board: Board, square: Square): List<Square> {
        val piece = board.getPiece(square) ?: return emptyList()
        return getLegalMovesForPiece(board, square, piece).map { it.to }.distinct()
    }

    fun getAllLegalMoves(board: Board, color: PieceColor): List<Move> {
        val moves = mutableListOf<Move>()
        for (rank in 0..7) {
            for (file in 0..7) {
                val square = Square(file, rank)
                val piece = board.getPiece(square) ?: continue
                if (piece.color == color) {
                    moves += getLegalMovesForPiece(board, square, piece)
                }
            }
        }
        return moves
    }

    fun isInCheck(board: Board, color: PieceColor): Boolean {
        val kingSquare = board.findKing(color) ?: return false
        return isSquareAttacked(board, kingSquare, color.opposite())
    }

    fun isCheckmate(board: Board, color: PieceColor): Boolean {
        if (!isInCheck(board, color)) {
            return false
        }
        return getAllLegalMoves(board, color).isEmpty()
    }

    fun isStalemate(board: Board, color: PieceColor): Boolean {
        if (isInCheck(board, color)) {
            return false
        }
        return getAllLegalMoves(board, color).isEmpty()
    }

    private fun getLegalMovesForPiece(board: Board, from: Square, piece: Piece): List<Move> {
        val pseudoMoves = getPseudoMoves(board, from, piece)
        return pseudoMoves.filter { move ->
            val copy = board.copy()
            applyMove(copy, move)
            !isInCheck(copy, piece.color)
        }
    }

    private fun getPseudoMoves(board: Board, from: Square, piece: Piece): List<Move> {
        return when (piece.type) {
            PieceType.PAWN -> pawnMoves(board, from, piece)
            PieceType.ROOK -> slidingMoves(board, from, piece, listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1))
            PieceType.BISHOP -> slidingMoves(board, from, piece, listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1))
            PieceType.QUEEN -> slidingMoves(
                board,
                from,
                piece,
                listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1, 1 to 1, 1 to -1, -1 to 1, -1 to -1)
            )
            PieceType.KNIGHT -> knightMoves(board, from, piece)
            PieceType.KING -> kingMoves(board, from, piece)
        }
    }

    private fun pawnMoves(board: Board, from: Square, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        val direction = if (piece.color == PieceColor.WHITE) 1 else -1
        val startRank = if (piece.color == PieceColor.WHITE) 1 else 6
        val promotionRank = if (piece.color == PieceColor.WHITE) 7 else 0

        val oneForward = Square(from.file, from.rank + direction)
        if (isOnBoard(oneForward) && board.getPiece(oneForward) == null) {
            val promotion = if (oneForward.rank == promotionRank) PieceType.QUEEN else null
            moves += Move(from, oneForward, piece, promotion = promotion)
            if (from.rank == startRank) {
                val twoForward = Square(from.file, from.rank + 2 * direction)
                if (board.getPiece(twoForward) == null) {
                    moves += Move(from, twoForward, piece)
                }
            }
        }

        for (deltaFile in listOf(-1, 1)) {
            val targetFile = from.file + deltaFile
            val targetRank = from.rank + direction
            if (targetFile !in 0..7 || targetRank !in 0..7) {
                continue
            }
            val target = Square(targetFile, targetRank)
            val targetPiece = board.getPiece(target)
            if (targetPiece != null && targetPiece.color != piece.color) {
                val promotion = if (target.rank == promotionRank) PieceType.QUEEN else null
                moves += Move(from, target, piece, captured = targetPiece, promotion = promotion)
            }

            val enPassantTarget = board.enPassantTarget
            if (enPassantTarget != null && enPassantTarget == target) {
                val capturedSquare = Square(targetFile, from.rank)
                val capturedPiece = board.getPiece(capturedSquare)
                if (capturedPiece != null && capturedPiece.color != piece.color && capturedPiece.type == PieceType.PAWN) {
                    moves += Move(from, target, piece, captured = capturedPiece, isEnPassant = true)
                }
            }
        }

        return moves
    }

    private fun slidingMoves(
        board: Board,
        from: Square,
        piece: Piece,
        directions: List<Pair<Int, Int>>
    ): List<Move> {
        val moves = mutableListOf<Move>()
        for ((fileStep, rankStep) in directions) {
            var file = from.file + fileStep
            var rank = from.rank + rankStep
            while (file in 0..7 && rank in 0..7) {
                val target = Square(file, rank)
                val targetPiece = board.getPiece(target)
                if (targetPiece == null) {
                    moves += Move(from, target, piece)
                } else {
                    if (targetPiece.color != piece.color) {
                        moves += Move(from, target, piece, captured = targetPiece)
                    }
                    break
                }
                file += fileStep
                rank += rankStep
            }
        }
        return moves
    }

    private fun knightMoves(board: Board, from: Square, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        val offsets = listOf(
            1 to 2, 2 to 1, -1 to 2, -2 to 1,
            1 to -2, 2 to -1, -1 to -2, -2 to -1
        )
        for ((fileStep, rankStep) in offsets) {
            val file = from.file + fileStep
            val rank = from.rank + rankStep
            if (file !in 0..7 || rank !in 0..7) {
                continue
            }
            val target = Square(file, rank)
            val targetPiece = board.getPiece(target)
            if (targetPiece == null || targetPiece.color != piece.color) {
                moves += Move(from, target, piece, captured = targetPiece)
            }
        }
        return moves
    }

    private fun kingMoves(board: Board, from: Square, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        for (fileStep in -1..1) {
            for (rankStep in -1..1) {
                if (fileStep == 0 && rankStep == 0) continue
                val file = from.file + fileStep
                val rank = from.rank + rankStep
                if (file !in 0..7 || rank !in 0..7) continue
                val target = Square(file, rank)
                val targetPiece = board.getPiece(target)
                if (targetPiece == null || targetPiece.color != piece.color) {
                    moves += Move(from, target, piece, captured = targetPiece)
                }
            }
        }

        moves += castlingMoves(board, from, piece)
        return moves
    }

    private fun castlingMoves(board: Board, from: Square, piece: Piece): List<Move> {
        if (piece.type != PieceType.KING) return emptyList()
        val moves = mutableListOf<Move>()
        val opponent = piece.color.opposite()

        if (piece.color == PieceColor.WHITE && from == Square(4, 0)) {
            if (board.castlingRights.whiteKingSide && canCastle(board, from, Square(7, 0), listOf(5, 6), opponent)) {
                moves += Move(from, Square(6, 0), piece, isCastling = true)
            }
            if (board.castlingRights.whiteQueenSide && canCastle(board, from, Square(0, 0), listOf(1, 2, 3), opponent, passThroughFiles = listOf(3, 2))) {
                moves += Move(from, Square(2, 0), piece, isCastling = true)
            }
        }

        if (piece.color == PieceColor.BLACK && from == Square(4, 7)) {
            if (board.castlingRights.blackKingSide && canCastle(board, from, Square(7, 7), listOf(5, 6), opponent)) {
                moves += Move(from, Square(6, 7), piece, isCastling = true)
            }
            if (board.castlingRights.blackQueenSide && canCastle(board, from, Square(0, 7), listOf(1, 2, 3), opponent, passThroughFiles = listOf(3, 2))) {
                moves += Move(from, Square(2, 7), piece, isCastling = true)
            }
        }

        return moves
    }

    private fun canCastle(
        board: Board,
        kingSquare: Square,
        rookSquare: Square,
        emptyFiles: List<Int>,
        opponent: PieceColor,
        passThroughFiles: List<Int> = listOf(5, 6)
    ): Boolean {
        val rook = board.getPiece(rookSquare)
        if (rook?.type != PieceType.ROOK || rook.color != board.getPiece(kingSquare)?.color) {
            return false
        }
        for (file in emptyFiles) {
            if (board.getPiece(Square(file, kingSquare.rank)) != null) {
                return false
            }
        }
        if (isSquareAttacked(board, kingSquare, opponent)) {
            return false
        }
        for (file in passThroughFiles) {
            if (isSquareAttacked(board, Square(file, kingSquare.rank), opponent)) {
                return false
            }
        }
        return true
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
            return
        }

        if (move.isEnPassant) {
            val capturedSquare = Square(move.to.file, move.from.rank)
            board.setPiece(capturedSquare, null)
        }

        val movedPiece = if (move.promotion != null) Piece(move.promotion, move.piece.color) else move.piece
        board.setPiece(move.from, null)
        board.setPiece(move.to, movedPiece)
    }

    private fun isSquareAttacked(board: Board, square: Square, byColor: PieceColor): Boolean {
        val pawnDirection = if (byColor == PieceColor.WHITE) 1 else -1
        val pawnRank = square.rank - pawnDirection
        for (fileStep in listOf(-1, 1)) {
            val file = square.file + fileStep
            if (file !in 0..7 || pawnRank !in 0..7) continue
            val piece = board.getPiece(Square(file, pawnRank))
            if (piece?.type == PieceType.PAWN && piece.color == byColor) {
                return true
            }
        }

        val knightOffsets = listOf(
            1 to 2, 2 to 1, -1 to 2, -2 to 1,
            1 to -2, 2 to -1, -1 to -2, -2 to -1
        )
        for ((fileStep, rankStep) in knightOffsets) {
            val file = square.file + fileStep
            val rank = square.rank + rankStep
            if (file !in 0..7 || rank !in 0..7) continue
            val piece = board.getPiece(Square(file, rank))
            if (piece?.type == PieceType.KNIGHT && piece.color == byColor) {
                return true
            }
        }

        if (isAttackedBySlidingPiece(board, square, byColor, listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1), setOf(PieceType.ROOK, PieceType.QUEEN))) {
            return true
        }

        if (isAttackedBySlidingPiece(board, square, byColor, listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1), setOf(PieceType.BISHOP, PieceType.QUEEN))) {
            return true
        }

        for (fileStep in -1..1) {
            for (rankStep in -1..1) {
                if (fileStep == 0 && rankStep == 0) continue
                val file = square.file + fileStep
                val rank = square.rank + rankStep
                if (file !in 0..7 || rank !in 0..7) continue
                val piece = board.getPiece(Square(file, rank))
                if (piece?.type == PieceType.KING && piece.color == byColor) {
                    return true
                }
            }
        }

        return false
    }

    private fun isAttackedBySlidingPiece(
        board: Board,
        square: Square,
        byColor: PieceColor,
        directions: List<Pair<Int, Int>>,
        attackers: Set<PieceType>
    ): Boolean {
        for ((fileStep, rankStep) in directions) {
            var file = square.file + fileStep
            var rank = square.rank + rankStep
            while (file in 0..7 && rank in 0..7) {
                val piece = board.getPiece(Square(file, rank))
                if (piece != null) {
                    if (piece.color == byColor && piece.type in attackers) {
                        return true
                    }
                    break
                }
                file += fileStep
                rank += rankStep
            }
        }
        return false
    }

    private fun isOnBoard(square: Square): Boolean = square.file in 0..7 && square.rank in 0..7
}
