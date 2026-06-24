package com.example.chess_trainer_client.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chess_trainer_client.R
import com.example.chess_trainer_client.data.local.db.SavedGameEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavedGamesAdapter(
    private val onLoad: (SavedGameEntity) -> Unit,
    private val onDelete: (SavedGameEntity) -> Unit
) : RecyclerView.Adapter<SavedGamesAdapter.SavedGameViewHolder>() {

    private val items = mutableListOf<SavedGameEntity>()

    fun submitList(games: List<SavedGameEntity>) {
        items.clear()
        items.addAll(games)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedGameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.saved_game_item, parent, false)
        return SavedGameViewHolder(view, onLoad, onDelete)
    }

    override fun onBindViewHolder(holder: SavedGameViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class SavedGameViewHolder(
        itemView: View,
        private val onLoad: (SavedGameEntity) -> Unit,
        private val onDelete: (SavedGameEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.saved_game_name)
        private val dateView: TextView = itemView.findViewById(R.id.saved_game_date)
        private val modeView: TextView = itemView.findViewById(R.id.saved_game_mode)
        private val loadButton: Button = itemView.findViewById(R.id.saved_game_load)
        private val deleteButton: Button = itemView.findViewById(R.id.saved_game_delete)
        private var current: SavedGameEntity? = null

        init {
            loadButton.setOnClickListener {
                current?.let(onLoad)
            }
            deleteButton.setOnClickListener {
                current?.let(onDelete)
            }
        }

        fun bind(game: SavedGameEntity) {
            current = game
            nameView.text = game.name
            dateView.text = formatTimestamp(game.timestamp)
            modeView.text = formatMode(game.mode)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        private fun formatMode(mode: String): String {
            return when (mode) {
                "ai_vs_player" -> "vs AI"
                "two_player" -> "Two Player"
                else -> mode
            }
        }
    }
}
