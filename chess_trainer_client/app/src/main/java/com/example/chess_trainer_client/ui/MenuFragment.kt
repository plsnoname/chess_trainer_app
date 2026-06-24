package com.example.chess_trainer_client.ui

import android.os.Bundle
import android.view.View
import android.text.format.DateFormat
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.chess_trainer_client.R
import com.example.chess_trainer_client.viewmodel.AuthViewModel
import com.example.chess_trainer_client.viewmodel.MenuViewModel

class MenuFragment : Fragment(R.layout.fragment_menu) {
    private val viewModel: AuthViewModel by viewModels()
    private val menuViewModel: MenuViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lastSavedLabel = view.findViewById<TextView>(R.id.menu_last_saved)

        view.findViewById<Button>(R.id.button_play_player).setOnClickListener {
            findNavController().navigate(
                R.id.action_menuFragment_to_gameFragment,
                bundleOf("mode" to "two_player")
            )
        }
        view.findViewById<Button>(R.id.button_play_ai).setOnClickListener {
            findNavController().navigate(
                R.id.action_menuFragment_to_gameFragment,
                bundleOf("mode" to "vs_ai")
            )
        }
        view.findViewById<Button>(R.id.button_puzzles).setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_puzzleFragment)
        }
        view.findViewById<Button>(R.id.button_openings).setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_openingsFragment)
        }
        view.findViewById<Button>(R.id.button_saved_games).setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_savedGamesFragment)
        }
        view.findViewById<Button>(R.id.button_logout).setOnClickListener {
            viewModel.clearSession()
            findNavController().navigate(R.id.action_menuFragment_to_authFragment)
        }

        menuViewModel.lastSavedAt.observe(viewLifecycleOwner) { timestamp ->
            if (timestamp == null) {
                lastSavedLabel.text = getString(R.string.menu_last_saved_empty)
            } else {
                val formatted = DateFormat.getMediumDateFormat(requireContext()).format(timestamp)
                val time = DateFormat.getTimeFormat(requireContext()).format(timestamp)
                lastSavedLabel.text = getString(R.string.menu_last_saved, "$formatted $time")
            }
        }
    }
}

