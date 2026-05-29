package com.example.chess_trainer_client.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chess_trainer_client.R

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_play_player).setOnClickListener {
            findNavController().navigate(
                R.id.action_homeFragment_to_gameFragment,
                bundleOf("mode" to "two_player")
            )
        }
        view.findViewById<Button>(R.id.button_play_ai).setOnClickListener {
            findNavController().navigate(
                R.id.action_homeFragment_to_gameFragment,
                bundleOf("mode" to "vs_ai")
            )
        }
        view.findViewById<Button>(R.id.button_puzzles).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_puzzleFragment)
        }
        view.findViewById<Button>(R.id.button_openings).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_openingsFragment)
        }
        view.findViewById<Button>(R.id.button_back).setOnClickListener {
            findNavController().popBackStack()
        }
    }
}

