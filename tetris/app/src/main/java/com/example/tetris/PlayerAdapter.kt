package com.example.tetris

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tetris.models.playerScore.PlayerScore
import androidx.core.graphics.toColorInt

class PlayerAdapter(
    private val items: List<PlayerScore>,
    private val currentPlayer: String
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: TextView = itemView.findViewById(R.id.playerName)
        val playerScore: TextView = itemView.findViewById(R.id.playerScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = items[position]

        if (player.player == currentPlayer) {
            holder.playerName.text = "You"
            holder.playerName.setTypeface(null, Typeface.BOLD)
            holder.playerName.setTextColor("#FFD700".toColorInt())
        } else {
            holder.playerName.text = player.player
            holder.playerName.setTypeface(null, Typeface.NORMAL)
        }

        holder.playerScore.text = player.score.toString()
    }
}