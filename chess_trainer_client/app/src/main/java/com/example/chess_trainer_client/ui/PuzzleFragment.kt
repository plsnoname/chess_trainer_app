package com.example.chess_trainer_client.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.chess_trainer_client.R
import com.example.chess_trainer_client.viewmodel.PuzzleResult
import com.example.chess_trainer_client.viewmodel.PuzzleViewModel

class PuzzleFragment : Fragment(R.layout.fragment_puzzle) {
    private val viewModel: PuzzleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val boardView = view.findViewById<ChessBoardView>(R.id.puzzle_board)
        val description = view.findViewById<TextView>(R.id.puzzle_description)
        val difficulty = view.findViewById<TextView>(R.id.puzzle_difficulty)
        val feedback = view.findViewById<TextView>(R.id.puzzle_feedback)
        val error = view.findViewById<TextView>(R.id.puzzle_error)
        val loading = view.findViewById<ProgressBar>(R.id.puzzle_loading)
        val retryButton = view.findViewById<Button>(R.id.puzzle_retry)
        val skipButton = view.findViewById<Button>(R.id.puzzle_skip)
        val nextButton = view.findViewById<Button>(R.id.puzzle_next)
        val backButton = view.findViewById<Button>(R.id.puzzle_back)

        boardView.setOnSquareTappedListener(object : ChessBoardView.OnSquareTappedListener {
            override fun onSquareTapped(square: com.example.chess_trainer_client.engine.Square) {
                viewModel.onSquareTapped(square)
            }
        })

        retryButton.setOnClickListener { viewModel.retryPuzzle() }
        skipButton.setOnClickListener { viewModel.nextPuzzle() }
        nextButton.setOnClickListener { viewModel.nextPuzzle() }
        backButton.setOnClickListener { findNavController().navigate(R.id.action_puzzleFragment_to_menuFragment) }

        viewModel.board.observe(viewLifecycleOwner) { board ->
            boardView.setBoard(board)
        }
        viewModel.legalMoves.observe(viewLifecycleOwner) { moves ->
            boardView.highlightSquares(moves)
        }
        viewModel.selectedSquare.observe(viewLifecycleOwner) { square ->
            boardView.highlightSelectedSquare(square)
        }
        viewModel.puzzle.observe(viewLifecycleOwner) { puzzle ->
            description.text = puzzle?.description ?: ""
            difficulty.text = puzzle?.let { getString(R.string.difficulty_label, it.difficulty) } ?: ""
        }
        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                PuzzleResult.Idle -> {
                    feedback.text = ""
                    nextButton.visibility = View.GONE
                }
                PuzzleResult.Correct -> {
                    feedback.text = getString(R.string.puzzle_correct)
                    nextButton.visibility = View.GONE
                }
                PuzzleResult.Incorrect -> {
                    feedback.text = getString(R.string.puzzle_incorrect)
                    nextButton.visibility = View.GONE
                }
                PuzzleResult.Completed -> {
                    feedback.text = getString(R.string.puzzle_completed)
                    nextButton.visibility = View.VISIBLE
                }
            }
        }
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            loading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { errorText ->
            if (errorText.isNullOrBlank()) {
                error.visibility = View.GONE
                error.text = ""
            } else {
                error.visibility = View.VISIBLE
                error.text = errorText
            }
        }
    }
}
