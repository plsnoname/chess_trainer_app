package com.example.chess_trainer_client.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chess_trainer_client.data.local.db.DatabaseProvider
import com.example.chess_trainer_client.data.local.db.SavedGameStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenuViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getInstance(application.applicationContext)
    private val savedGameStore = SavedGameStore(database)

    private val _lastSavedAt = MutableLiveData<Long?>(null)
    val lastSavedAt: LiveData<Long?> = _lastSavedAt

    init {
        refreshLastSaved()
    }

    fun refreshLastSaved() {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = savedGameStore.getLatestTimestamp()
            withContext(Dispatchers.Main) {
                _lastSavedAt.value = timestamp
            }
        }
    }
}

