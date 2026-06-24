package com.example.chess_trainer_client.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chess_trainer_client.R
import com.example.chess_trainer_client.viewmodel.OpeningMode
import com.example.chess_trainer_client.viewmodel.OpeningsViewModel
import com.example.chess_trainer_client.viewmodel.TrainingResult

class OpeningsFragment : Fragment(R.layout.fragment_openings) {
    private val viewModel: OpeningsViewModel by viewModels()
    private lateinit var adapter: OpeningsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val openingsList = view.findViewById<RecyclerView>(R.id.openings_list)
        val boardView = view.findViewById<ChessBoardView>(R.id.openings_board)
        val openingName = view.findViewById<TextView>(R.id.opening_name)
        val moveCounter = view.findViewById<TextView>(R.id.opening_move_counter)
        val previousButton = view.findViewById<Button>(R.id.opening_previous)
        val nextButton = view.findViewById<Button>(R.id.opening_next)
        val resetButton = view.findViewById<Button>(R.id.opening_reset)
        val modeToggle = view.findViewById<ToggleButton>(R.id.opening_mode_toggle)
        val feedbackLabel = view.findViewById<TextView>(R.id.opening_feedback)
        val loading = view.findViewById<ProgressBar>(R.id.opening_loading)
        val error = view.findViewById<TextView>(R.id.opening_error)
        val backButton = view.findViewById<Button>(R.id.opening_back)

        adapter = OpeningsAdapter { opening -> viewModel.selectOpening(opening) }
        openingsList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        openingsList.adapter = adapter

        boardView.setOnSquareTappedListener(object : ChessBoardView.OnSquareTappedListener {
            override fun onSquareTapped(square: com.example.chess_trainer_client.engine.Square) {
                viewModel.onSquareTapped(square)
            }
        })

        previousButton.setOnClickListener { viewModel.previousMove() }
        nextButton.setOnClickListener { viewModel.nextMove() }
        resetButton.setOnClickListener { viewModel.resetOpening() }
        modeToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.switchMode(if (isChecked) OpeningMode.TRAINING else OpeningMode.STUDY)
        }
        backButton.setOnClickListener { findNavController().navigate(R.id.action_openingsFragment_to_menuFragment) }

        viewModel.openingsList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
        viewModel.selectedOpening.observe(viewLifecycleOwner) { opening ->
            openingName.text = opening?.name ?: ""
            val total = opening?.moves?.size ?: 0
            val index = viewModel.moveIndex.value ?: 0
            moveCounter.text = getString(R.string.move_counter, index, total)
            
            // Update selected position in adapter
            if (opening != null) {
                val position = adapter.items.indexOfFirst { it.id == opening.id }
                if (position >= 0) {
                    adapter.setSelectedPosition(position)
                }
            }
        }
        viewModel.board.observe(viewLifecycleOwner) { board ->
            boardView.setBoard(board)
        }
        viewModel.legalMoves.observe(viewLifecycleOwner) { moves ->
            boardView.highlightSquares(moves)
        }
        viewModel.selectedSquare.observe(viewLifecycleOwner) { square ->
            boardView.highlightSelectedSquare(square)
        }
        viewModel.moveIndex.observe(viewLifecycleOwner) { index ->
            val total = viewModel.selectedOpening.value?.moves?.size ?: 0
            moveCounter.text = getString(R.string.move_counter, index, total)
        }
        viewModel.mode.observe(viewLifecycleOwner) { mode ->
            val inTraining = mode == OpeningMode.TRAINING
            previousButton.visibility = if (inTraining) View.GONE else View.VISIBLE
            nextButton.visibility = if (inTraining) View.GONE else View.VISIBLE
            feedbackLabel.visibility = if (inTraining) View.VISIBLE else View.GONE
            modeToggle.isChecked = inTraining
        }
        viewModel.trainingResult.observe(viewLifecycleOwner) { result ->
            feedbackLabel.text = when (result) {
                TrainingResult.Idle -> ""
                TrainingResult.Correct -> getString(R.string.training_correct)
                TrainingResult.Incorrect -> getString(R.string.training_incorrect)
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
