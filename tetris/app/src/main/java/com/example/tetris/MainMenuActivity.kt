package com.example.tetris

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tetris.databinding.ActivityMainMenuBinding
import androidx.core.content.edit

class MainMenuActivity : AppCompatActivity() {
    private lateinit var bindingClass : ActivityMainMenuBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingClass = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(bindingClass.root)

        bindingClass.play.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        bindingClass.soundMain.setOnClickListener { MusicPlayer.changeSound() }
        bindingClass.ratingBtn.setOnClickListener { startActivity(Intent(this, MainRatingActivity::class.java)) }
        MusicPlayer.startMusic(this, R.raw.vstup)

        setupNameField()
    }

    override fun onResume() {
        super.onResume()
        showScore()
        showName()
        MusicPlayer.startMusic(this, R.raw.vstup)
    }

    override fun onPause() {
        super.onPause()
        saveName()
        MusicPlayer.pauseMusic()
    }

    @SuppressLint("SetTextI18n")
    private fun showScore() {
        val sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
        val userScore = sharedPreferences.getInt("score", 0)
        bindingClass.record.text = "Your record: $userScore"
    }

    private fun showName() {
        val sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
        val playerName = sharedPreferences.getString("player_name", "")
        bindingClass.playerName.setText(playerName)
    }

    private fun saveName() {
        val sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
        sharedPreferences.edit {
            val name = bindingClass.playerName.text.toString().trim()
            putString("player_name", name)
        }
    }

    private fun setupNameField() {
        bindingClass.playerName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveName()
            }
        }
    }
}