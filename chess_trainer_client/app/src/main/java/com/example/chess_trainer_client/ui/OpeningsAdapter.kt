package com.example.chess_trainer_client.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chess_trainer_client.R
import com.example.chess_trainer_client.data.Opening

class OpeningsAdapter(
    private val onClick: (Opening) -> Unit
) : RecyclerView.Adapter<OpeningsAdapter.OpeningViewHolder>() {

    private val items = mutableListOf<Opening>()

    fun submitList(openings: List<Opening>) {
        items.clear()
        items.addAll(openings)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpeningViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.opening_list_item, parent, false)
        return OpeningViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: OpeningViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class OpeningViewHolder(
        itemView: View,
        private val onClick: (Opening) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.opening_name)
        private var current: Opening? = null

        init {
            itemView.setOnClickListener {
                current?.let(onClick)
            }
        }

        fun bind(opening: Opening) {
            current = opening
            nameView.text = opening.name
        }
    }
}

