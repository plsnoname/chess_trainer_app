package com.example.chess_trainer_client.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chess_trainer_client.R
import com.example.chess_trainer_client.data.local.db.DatabaseProvider
import com.example.chess_trainer_client.viewmodel.SavedGamesViewModel

class SavedGamesFragment : Fragment(R.layout.fragment_saved_games) {
    private val viewModel: SavedGamesViewModel by viewModels()
    private lateinit var adapter: SavedGamesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gamesList = view.findViewById<RecyclerView>(R.id.saved_games_list)
        val loading = view.findViewById<ProgressBar>(R.id.saved_games_loading)
        val error = view.findViewById<TextView>(R.id.saved_games_error)
        val backButton = view.findViewById<Button>(R.id.saved_games_back)

        // Initialize store and adapter
        val store = DatabaseProvider.getSavedGameStore(requireContext())
        viewModel.setGameStore(store)

        adapter = SavedGamesAdapter(
            onLoad = { game ->
                // Pass game data to GameFragment via bundle
                val bundle = Bundle().apply {
                    putLong("saved_game_id", game.id)
                    putString("saved_game_fen", game.fen)
                    putString("saved_game_color", game.activeColor)
                    putString("saved_game_mode", game.mode)
                }
                findNavController().navigate(R.id.action_savedGamesFragment_to_gameFragment, bundle)
            },
            onDelete = { game ->
                viewModel.deleteGame(game.id)
            }
        )
        
        // Set LayoutManager for RecyclerView
        gamesList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        gamesList.adapter = adapter

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_savedGamesFragment_to_menuFragment)
        }

        viewModel.games.observe(viewLifecycleOwner) { games ->
            adapter.submitList(games)
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

