package com.example.chess_trainer_client.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.chess_trainer_client.R
import com.example.chess_trainer_client.data.local.db.DatabaseProvider
import com.example.chess_trainer_client.engine.PieceColor
import com.example.chess_trainer_client.engine.PieceType
import com.example.chess_trainer_client.viewmodel.GameResult
import com.example.chess_trainer_client.viewmodel.GameViewModel

class GameFragment : Fragment(R.layout.fragment_game) {
    private val viewModel: GameViewModel by viewModels()
    private var resultDialog: AlertDialog? = null
    private var aiThinking = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = arguments?.getString("mode") ?: "two_player"
        viewModel.setAiMode(mode == "vs_ai")
        
        // Initialize game store
        val gameStore = DatabaseProvider.getSavedGameStore(requireContext())
        viewModel.setGameStore(gameStore)
        
        // Check if loading a saved game
        val savedGameId = arguments?.getLong("saved_game_id", -1L) ?: -1L
        if (savedGameId > 0) {
            val fen = arguments?.getString("saved_game_fen") ?: ""
            val activeColor = arguments?.getString("saved_game_color") ?: "WHITE"
            val gameMode = arguments?.getString("saved_game_mode") ?: "two_player"
            
            // Create and load the saved game
            val savedEntity = com.example.chess_trainer_client.data.local.db.SavedGameEntity(
                id = savedGameId,
                name = "Loaded Game",
                fen = fen,
                activeColor = activeColor,
                mode = gameMode,
                timestamp = System.currentTimeMillis()
            )
            viewModel.loadGame(savedEntity)
            android.util.Log.d("GameFragment", "Loaded saved game: $savedGameId with mode: $gameMode")
        }

        val boardView = view.findViewById<ChessBoardView>(R.id.chess_board)
        val turnLabel = view.findViewById<TextView>(R.id.turn_label)
        val resetButton = view.findViewById<Button>(R.id.reset_button)
        val undoButton = view.findViewById<Button>(R.id.undo_button)
        val saveButton = view.findViewById<Button>(R.id.save_button)
        val backButton = view.findViewById<Button>(R.id.back_button)
        val aiThinkingLabel = view.findViewById<TextView>(R.id.ai_thinking_label)
        val aiThinkingSpinner = view.findViewById<View>(R.id.ai_thinking_spinner)

        boardView.setOnSquareTappedListener(object : ChessBoardView.OnSquareTappedListener {
            override fun onSquareTapped(square: com.example.chess_trainer_client.engine.Square) {
                if (!aiThinking) {
                    viewModel.onSquareTapped(square)
                }
            }
        })

        resetButton.setOnClickListener { viewModel.resetGame() }
        undoButton.setOnClickListener { viewModel.undoLastMove() }
        saveButton.setOnClickListener { saveGame() }
        backButton.setOnClickListener { findNavController().navigate(R.id.action_gameFragment_to_menuFragment) }

        viewModel.board.observe(viewLifecycleOwner) { board ->
            boardView.setBoard(board)
        }
        viewModel.legalMoves.observe(viewLifecycleOwner) { moves ->
            boardView.highlightSquares(moves)
        }
        viewModel.selectedSquare.observe(viewLifecycleOwner) { square ->
            boardView.highlightSelectedSquare(square)
        }
        viewModel.activeColor.observe(viewLifecycleOwner) { color ->
            turnLabel.text = when (color) {
                PieceColor.WHITE -> getString(R.string.white_to_move)
                PieceColor.BLACK -> getString(R.string.black_to_move)
            }
        }
        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                GameResult.Ongoing -> resultDialog?.dismiss()
                is GameResult.Checkmate -> showResultDialog(
                    title = getString(R.string.game_over_title),
                    message = buildGameOverMessage(result.winner, mode == "vs_ai")
                )
                GameResult.Stalemate -> showResultDialog(
                    title = getString(R.string.game_over_title),
                    message = getString(R.string.stalemate_message)
                )
            }
        }
        viewModel.aiThinking.observe(viewLifecycleOwner) { thinking ->
            aiThinking = thinking
            val visibility = if (thinking) View.VISIBLE else View.GONE
            aiThinkingLabel.visibility = visibility
            aiThinkingSpinner.visibility = visibility
        }

        viewModel.promotionRequest.observe(viewLifecycleOwner) { request ->
            if (request == null) return@observe
            showPromotionDialog()
        }
    }

    private fun showPromotionDialog() {
        val choices = arrayOf(
            getString(R.string.promotion_queen),
            getString(R.string.promotion_rook),
            getString(R.string.promotion_bishop),
            getString(R.string.promotion_knight)
        )
        val types = arrayOf(
            PieceType.QUEEN,
            PieceType.ROOK,
            PieceType.BISHOP,
            PieceType.KNIGHT
        )
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.promotion_title)
            .setItems(choices) { _, index ->
                viewModel.confirmPromotion(types[index])
            }
            .setCancelable(false)
            .show()
    }

    private fun showResultDialog(title: String, message: String) {
        if (resultDialog?.isShowing == true) {
            return
        }
        resultDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.play_again) { _, _ ->
                viewModel.resetGame()
            }
            .setCancelable(false)
            .show()
        resultDialog?.setOnDismissListener {
            resultDialog = null
        }
    }

    private fun buildGameOverMessage(winner: PieceColor, isVsAi: Boolean): String {
        return if (isVsAi) {
            when (winner) {
                PieceColor.WHITE -> getString(R.string.game_over_you_won)
                PieceColor.BLACK -> getString(R.string.game_over_you_lost)
            }
        } else {
            when (winner) {
                PieceColor.WHITE -> getString(R.string.game_over_white_wins)
                PieceColor.BLACK -> getString(R.string.game_over_black_wins)
            }
        }
    }

    private fun saveGame() {
        val success = viewModel.saveGame()
        if (success) {
            AlertDialog.Builder(requireContext())
                .setTitle("Game Saved")
                .setMessage("Your game has been saved successfully!")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Save Failed")
                .setMessage("Failed to save the game. Please try again.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}
