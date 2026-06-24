package com.example.chess_trainer_client.ui

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.chess_trainer_client.R
import com.example.chess_trainer_client.viewmodel.AuthState
import com.example.chess_trainer_client.viewmodel.AuthViewModel

class AuthFragment : Fragment(R.layout.fragment_auth) {
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isLoggedInSync()) {
            navigateToMenu()
            return
        }

        val emailInput = view.findViewById<EditText>(R.id.input_email)
        val passwordInput = view.findViewById<EditText>(R.id.input_password)
        val loginButton = view.findViewById<Button>(R.id.button_login)
        val registerButton = view.findViewById<Button>(R.id.button_register)
        val progress = view.findViewById<ProgressBar>(R.id.auth_progress)
        val errorText = view.findViewById<TextView>(R.id.auth_error)

        fun submit(isRegister: Boolean) {
            val email = emailInput.text?.toString()?.trim().orEmpty()
            val password = passwordInput.text?.toString().orEmpty()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errorText.text = getString(R.string.auth_error_invalid_email)
                errorText.visibility = View.VISIBLE
                return
            }
            if (password.length < 8) {
                errorText.text = getString(R.string.auth_error_invalid_password)
                errorText.visibility = View.VISIBLE
                return
            }

            errorText.visibility = View.GONE
            if (isRegister) {
                viewModel.register(email, password)
            } else {
                viewModel.login(email, password)
            }
        }

        loginButton.setOnClickListener { submit(false) }
        registerButton.setOnClickListener { submit(true) }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                AuthState.Idle -> {
                    progress.visibility = View.GONE
                }
                AuthState.Loading -> {
                    progress.visibility = View.VISIBLE
                    errorText.visibility = View.GONE
                }
                AuthState.Success -> {
                    progress.visibility = View.GONE
                    navigateToMenu()
                    viewModel.resetState()
                }
                is AuthState.Error -> {
                    progress.visibility = View.GONE
                    errorText.text = state.message
                    errorText.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun navigateToMenu() {
        findNavController().navigate(R.id.action_authFragment_to_menuFragment)
    }
}

