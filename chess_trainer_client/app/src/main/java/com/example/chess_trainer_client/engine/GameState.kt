package com.example.chess_trainer_client.engine

import kotlin.math.abs

sealed class MoveResult {
    object Success : MoveResult()
    object IllegalMove : MoveResult()
    data class Checkmate(val winner: PieceColor) : MoveResult()
    object Stalemate : MoveResult()
}

class GameState {
    val board: Board = Board()
    var activeColor: PieceColor = PieceColor.WHITE
        private set

    private val moveHistory = ArrayDeque<MoveRecord>()

    init {
        board.initializeStartingPosition()
    }

    fun applyMove(from: Square, to: Square, promotion: PieceType? = null): MoveResult {
        val piece = board.getPiece(from) ?: return MoveResult.IllegalMove
        if (piece.color != activeColor) {
            return MoveResult.IllegalMove
        }
        val legalTargets = MoveGenerator.getLegalMoves(board, from)
        if (to !in legalTargets) {
            return MoveResult.IllegalMove
        }

        val move = buildMove(from, to, piece, promotion)
        val record = applyMoveInternal(move)
        moveHistory.addLast(record)

        activeColor = activeColor.opposite()

        return when {
            MoveGenerator.isCheckmate(board, activeColor) -> MoveResult.Checkmate(activeColor.opposite())
            MoveGenerator.isStalemate(board, activeColor) -> MoveResult.Stalemate
            else -> MoveResult.Success
        }
    }

    fun reset() {
        board.initializeStartingPosition()
        activeColor = PieceColor.WHITE
        moveHistory.clear()
    }

    fun loadFromFen(fen: String, activeColor: PieceColor) {
        board.loadFromFen(fen)
        this.activeColor = activeColor
        moveHistory.clear()
    }

    fun loadFromFen(fen: String) {
        board.loadFromFen(fen)
        activeColor = parseActiveColor(fen)
        moveHistory.clear()
    }

    fun toFen(): String {
        val active = if (activeColor == PieceColor.WHITE) "w" else "b"
        val castling = castlingRightsToFen(board.castlingRights)
        val enPassant = board.enPassantTarget?.toAlgebraic() ?: "-"
        return "${board.toFenPlacement()} $active $castling $enPassant 0 1"
    }

    fun toSnapshot(): GameStateSnapshot = GameStateSnapshot(toFen())

    fun loadFromSnapshot(snapshot: GameStateSnapshot) {
        loadFromFen(snapshot.fen)
    }

    fun undoLastMove(): Boolean {
        if (moveHistory.isEmpty()) {
            return false
        }
        val record = moveHistory.removeLast()
        undoMove(record)
        return true
    }

    private fun buildMove(from: Square, to: Square, piece: Piece, promotionOverride: PieceType?): Move {
        val isCastling = piece.type == PieceType.KING && abs(to.file - from.file) == 2
        val isEnPassant = piece.type == PieceType.PAWN && board.enPassantTarget == to && board.getPiece(to) == null
        val capturedSquare = if (isEnPassant) Square(to.file, from.rank) else to
        val capturedPiece = board.getPiece(capturedSquare)
        val promotion = if (piece.type == PieceType.PAWN && (to.rank == 0 || to.rank == 7)) {
            promotionOverride ?: PieceType.QUEEN
        } else {
            null
        }
        return Move(from, to, piece, captured = capturedPiece, promotion = promotion, isEnPassant = isEnPassant, isCastling = isCastling)
    }

    private fun applyMoveInternal(move: Move): MoveRecord {
        val previousRights = board.castlingRights.copy()
        val previousEnPassant = board.enPassantTarget
        val previousActive = activeColor
        val capturedSquare = if (move.isEnPassant) Square(move.to.file, move.from.rank) else move.to

        if (move.isCastling) {
            board.setPiece(move.from, null)
            board.setPiece(move.to, move.piece)
            val rookFrom = if (move.to.file > move.from.file) Square(7, move.from.rank) else Square(0, move.from.rank)
            val rookTo = if (move.to.file > move.from.file) Square(5, move.from.rank) else Square(3, move.from.rank)
            val rook = board.getPiece(rookFrom)
            board.setPiece(rookFrom, null)
            board.setPiece(rookTo, rook)
        } else {
            if (move.isEnPassant) {
                board.setPiece(capturedSquare, null)
            }
            val movedPiece = if (move.promotion != null) Piece(move.promotion, move.piece.color) else move.piece
            board.setPiece(move.from, null)
            board.setPiece(move.to, movedPiece)
        }

        updateCastlingRights(move, move.captured, capturedSquare)
        board.enPassantTarget = if (move.piece.type == PieceType.PAWN && abs(move.to.rank - move.from.rank) == 2) {
            Square(move.from.file, (move.from.rank + move.to.rank) / 2)
        } else {
            null
        }

        return MoveRecord(
            move = move,
            capturedSquare = capturedSquare,
            capturedPiece = move.captured,
            previousCastlingRights = previousRights,
            previousEnPassantTarget = previousEnPassant,
            previousActiveColor = previousActive
        )
    }

    private fun updateCastlingRights(move: Move, captured: Piece?, capturedSquare: Square) {
        val rights = board.castlingRights
        if (move.piece.type == PieceType.KING) {
            if (move.piece.color == PieceColor.WHITE) {
                rights.whiteKingSide = false
                rights.whiteQueenSide = false
            } else {
                rights.blackKingSide = false
                rights.blackQueenSide = false
            }
        }

        if (move.piece.type == PieceType.ROOK) {
            if (move.piece.color == PieceColor.WHITE && move.from.rank == 0) {
                if (move.from.file == 0) rights.whiteQueenSide = false
                if (move.from.file == 7) rights.whiteKingSide = false
            }
            if (move.piece.color == PieceColor.BLACK && move.from.rank == 7) {
                if (move.from.file == 0) rights.blackQueenSide = false
                if (move.from.file == 7) rights.blackKingSide = false
            }
        }

        if (captured?.type == PieceType.ROOK) {
            if (captured.color == PieceColor.WHITE && capturedSquare.rank == 0) {
                if (capturedSquare.file == 0) rights.whiteQueenSide = false
                if (capturedSquare.file == 7) rights.whiteKingSide = false
            }
            if (captured.color == PieceColor.BLACK && capturedSquare.rank == 7) {
                if (capturedSquare.file == 0) rights.blackQueenSide = false
                if (capturedSquare.file == 7) rights.blackKingSide = false
            }
        }
    }

    private fun undoMove(record: MoveRecord) {
        board.castlingRights = record.previousCastlingRights
        board.enPassantTarget = record.previousEnPassantTarget
        activeColor = record.previousActiveColor

        val move = record.move
        if (move.isCastling) {
            board.setPiece(move.to, null)
            board.setPiece(move.from, move.piece)
            val rookFrom = if (move.to.file > move.from.file) Square(7, move.from.rank) else Square(0, move.from.rank)
            val rookTo = if (move.to.file > move.from.file) Square(5, move.from.rank) else Square(3, move.from.rank)
            val rook = board.getPiece(rookTo)
            board.setPiece(rookTo, null)
            board.setPiece(rookFrom, rook)
            return
        }

        board.setPiece(move.to, null)
        val restoredPiece = if (move.promotion != null) Piece(PieceType.PAWN, move.piece.color) else move.piece
        board.setPiece(move.from, restoredPiece)

        if (record.capturedPiece != null) {
            board.setPiece(record.capturedSquare, record.capturedPiece)
        }
    }
}

data class GameStateSnapshot(val fen: String)

private fun parseActiveColor(fen: String): PieceColor {
    val parts = fen.trim().split(Regex("\\s+"))
    val colorPart = parts.getOrNull(1) ?: "w"
    return if (colorPart.lowercase() == "b") PieceColor.BLACK else PieceColor.WHITE
}

private fun castlingRightsToFen(rights: CastlingRights): String {
    val builder = StringBuilder()
    if (rights.whiteKingSide) builder.append('K')
    if (rights.whiteQueenSide) builder.append('Q')
    if (rights.blackKingSide) builder.append('k')
    if (rights.blackQueenSide) builder.append('q')
    return if (builder.isEmpty()) "-" else builder.toString()
}

private data class MoveRecord(
    val move: Move,
    val capturedSquare: Square,
    val capturedPiece: Piece?,
    val previousCastlingRights: CastlingRights,
    val previousEnPassantTarget: Square?,
    val previousActiveColor: PieceColor
)
