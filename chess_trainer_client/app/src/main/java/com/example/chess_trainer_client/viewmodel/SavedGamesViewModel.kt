package com.example.chess_trainer_client.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess_trainer_client.data.local.db.SavedGameEntity
import com.example.chess_trainer_client.data.local.db.SavedGameStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SavedGamesViewModel"

class SavedGamesViewModel : ViewModel() {
    private var savedGameStore: SavedGameStore? = null

    private val _games = MutableLiveData<List<SavedGameEntity>>(emptyList())
    val games: LiveData<List<SavedGameEntity>> = _games

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun setGameStore(store: SavedGameStore) {
        savedGameStore = store
        loadSavedGames()
    }

    fun loadSavedGames() {
        _loading.value = true
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val gamesList = savedGameStore?.getAllSavedGames() ?: emptyList()
                Log.d(TAG, "Loaded ${gamesList.size} saved games")
                withContext(Dispatchers.Main) {
                    _games.value = gamesList
                    _loading.value = false
                    _error.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load saved games", e)
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to load saved games: ${e.message}"
                    _loading.value = false
                }
            }
        }
    }

    fun deleteGame(gameId: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                savedGameStore?.delete(gameId)
                Log.d(TAG, "Deleted game with id: $gameId")
                withContext(Dispatchers.Main) {
                    loadSavedGames()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete game", e)
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to delete game: ${e.message}"
                }
            }
        }
    }

    fun refreshGames() {
        loadSavedGames()
    }
}
