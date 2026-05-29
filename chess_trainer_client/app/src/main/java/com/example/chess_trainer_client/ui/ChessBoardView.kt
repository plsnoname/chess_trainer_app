package com.example.chess_trainer_client.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.chess_trainer_client.engine.Board
import com.example.chess_trainer_client.engine.Piece
import com.example.chess_trainer_client.engine.PieceColor
import com.example.chess_trainer_client.engine.PieceType
import com.example.chess_trainer_client.engine.Square
import kotlin.math.min

class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface OnSquareTappedListener {
        fun onSquareTapped(square: Square)
    }

    private val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#EEEED2") }
    private val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#769656") }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#66FFD700") }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#6642A5F5") }
    private val piecePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }

    private val boardRect = RectF()
    private var squareSize = 0f

    private var board: Board? = null
    private var highlightedSquares: Set<Square> = emptySet()
    private var selectedSquare: Square? = null
    private var listener: OnSquareTappedListener? = null

    fun setBoard(board: Board) {
        this.board = board
        invalidate()
    }

    fun highlightSquares(squares: List<Square>) {
        highlightedSquares = squares.toSet()
        invalidate()
    }

    fun highlightSelectedSquare(square: Square?) {
        selectedSquare = square
        invalidate()
    }

    fun setOnSquareTappedListener(listener: OnSquareTappedListener?) {
        this.listener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val boardSize = min(width, height).toFloat()
        val left = (width - boardSize) / 2f
        val top = (height - boardSize) / 2f
        boardRect.set(left, top, left + boardSize, top + boardSize)
        squareSize = boardSize / 8f

        for (rank in 0..7) {
            for (file in 0..7) {
                val displayRow = 7 - rank
                val rect = squareRect(file, displayRow)
                val isLightSquare = (file + rank) % 2 == 0
                canvas.drawRect(rect, if (isLightSquare) lightPaint else darkPaint)
            }
        }

        selectedSquare?.let { square ->
            val rect = squareRect(square.file, 7 - square.rank)
            canvas.drawRect(rect, selectedPaint)
        }

        highlightedSquares.forEach { square ->
            val rect = squareRect(square.file, 7 - square.rank)
            canvas.drawRect(rect, highlightPaint)
        }

        board?.let { currentBoard ->
            for (rank in 0..7) {
                for (file in 0..7) {
                    val piece = currentBoard.getPiece(Square(file, rank)) ?: continue
                    drawPiece(canvas, piece, file, 7 - rank)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) {
            return true
        }
        if (!boardRect.contains(event.x, event.y)) {
            return true
        }
        val file = ((event.x - boardRect.left) / squareSize).toInt().coerceIn(0, 7)
        val row = ((event.y - boardRect.top) / squareSize).toInt().coerceIn(0, 7)
        val rank = 7 - row
        listener?.onSquareTapped(Square(file, rank))
        return true
    }

    private fun drawPiece(canvas: Canvas, piece: Piece, file: Int, row: Int) {
        val rect = squareRect(file, row)
        val symbol = pieceSymbol(piece)
        piecePaint.color = if (piece.color == PieceColor.WHITE) Color.WHITE else Color.BLACK
        piecePaint.textSize = squareSize * 0.7f
        val textX = rect.centerX()
        val textY = rect.centerY() - (piecePaint.descent() + piecePaint.ascent()) / 2
        canvas.drawText(symbol, textX, textY, piecePaint)
    }

    private fun pieceSymbol(piece: Piece): String {
        val base = when (piece.type) {
            PieceType.KING -> "K"
            PieceType.QUEEN -> "Q"
            PieceType.ROOK -> "R"
            PieceType.BISHOP -> "B"
            PieceType.KNIGHT -> "N"
            PieceType.PAWN -> "P"
        }
        return if (piece.color == PieceColor.WHITE) base else base.lowercase()
    }

    private fun squareRect(file: Int, row: Int): RectF {
        val left = boardRect.left + file * squareSize
        val top = boardRect.top + row * squareSize
        return RectF(left, top, left + squareSize, top + squareSize)
    }
}

