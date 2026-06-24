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

    private val _items = mutableListOf<Opening>()
    val items: List<Opening> get() = _items
    private var selectedPosition: Int = -1

    fun submitList(openings: List<Opening>) {
        _items.clear()
        _items.addAll(openings)
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        if (oldPosition >= 0) notifyItemChanged(oldPosition)
        if (position >= 0) notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpeningViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.opening_list_item, parent, false)
        return OpeningViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: OpeningViewHolder, position: Int) {
        holder.bind(_items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = _items.size

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

        fun bind(opening: Opening, isSelected: Boolean) {
            current = opening
            nameView.text = opening.name
            nameView.isSelected = isSelected
        }
    }
}

