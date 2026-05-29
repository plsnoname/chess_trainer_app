package com.example.chess_trainer_client.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.chess_trainer_client.R
import com.example.chess_trainer_client.viewmodel.AuthViewModel

class MenuFragment : Fragment(R.layout.fragment_menu) {
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_play).setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_homeFragment)
        }
        view.findViewById<Button>(R.id.button_logout).setOnClickListener {
            viewModel.clearSession()
            findNavController().navigate(R.id.action_menuFragment_to_authFragment)
        }
    }
}

