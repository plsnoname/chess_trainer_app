package com.example.chess_trainer_client

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.example.chess_trainer_client.viewmodel.AuthViewModel

class MainActivity : FragmentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupNavigationGuard()
    }

    private fun setupNavigationGuard() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Check if trying to navigate to a protected screen while logged out
            val protectedScreenIds = setOf(
                R.id.homeFragment,
                R.id.gameFragment,
                R.id.puzzleFragment,
                R.id.openingsFragment
            )

            if (destination.id in protectedScreenIds && !authViewModel.isLoggedInSync()) {
                // Redirect to auth if not logged in
                navController.navigate(R.id.authFragment)
            }
        }
    }
}