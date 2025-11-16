package com.example.tetris

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.edit
import com.example.tetris.databinding.ActivityMainBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var bindingClass: ActivityMainBinding
    private lateinit var game: Game
    private var lButtonPressed = false
    private var rButtonPressed = false
    private var dButtonPressed = false
    private var pause = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingClass = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingClass.root)

        bindingClass.canvas.setStartRow(3)

        game = Game()
        game.setStopGame(::stopGame)
        game.setChangeScore(::addScore)
        game.setChangeRowCount(::addRows)
        game.setInvalidateCanvas(bindingClass.canvas::invalidateCanvas)
        game.setChangeNextFigure(bindingClass.nextFigureCanvas::invalidateCanvas)
        game.initGame()
        game.startGame()

        MusicPlayer.startMusic(this, R.raw.game)

        bindingClass.imageButtonLeft.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!pause) {
                        if (!rButtonPressed) lButtonPressed = true
                        thread {
                            while (lButtonPressed) {
                                game.moveFigureLeft()
                                TimeUnit.MILLISECONDS.sleep(100)
                            }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    lButtonPressed = false
                    true
                }

                else -> false
            }
        }
        bindingClass.imageButtonRight.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!pause) {
                        if (!lButtonPressed) rButtonPressed = true
                        thread {
                            while (rButtonPressed) {
                                game.moveFigureRight()
                                TimeUnit.MILLISECONDS.sleep(100)
                            }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    rButtonPressed = false
                    true
                }

                else -> false
            }
        }
        bindingClass.imageButtonDown.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!pause) {
                        dButtonPressed = true
                        thread {
                            while (dButtonPressed) {
                                game.moveFigureDown()
                                TimeUnit.MILLISECONDS.sleep(50)
                            }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    dButtonPressed = false
                    true
                }

                else -> false
            }
        }
        bindingClass.buttonRotate.setOnClickListener { if (!pause) game.rotateFigure() }
        bindingClass.sound.setOnClickListener { MusicPlayer.changeSound() }
        bindingClass.gameOver.setOnClickListener { finish() }
        bindingClass.pause.setOnClickListener {
            pause = !pause
            if (pause) {
                game.pauseGame()
                bindingClass.pauseText.visibility = View.VISIBLE
            } else {
                game.resumeGame()
                bindingClass.pauseText.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MusicPlayer.resumeMusic()
        game.resumeGame()
    }

    override fun onPause() {
        super.onPause()
        MusicPlayer.pauseMusic()
        game.pauseGame()
    }

    @SuppressLint("SetTextI18n")
    private fun stopGame(score: Int) {
        runOnUiThread {
            bindingClass.gameOver.text = "Game Over\nScore: $score\nClick to restart"
            bindingClass.gameOver.visibility = View.VISIBLE
            saveScore(score)
            sendScoresToServer(score)
        }
    }

    private fun saveScore(score: Int) {
        val sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
        val userScore = sharedPreferences.getInt("score", 0)
        val editor = sharedPreferences.edit()
        if (score > userScore) {
            editor.putInt("score", score)
            editor.apply()
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun sendScoresToServer(score: Int) {
        val sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
        var userName = sharedPreferences.getString("player_name", "")
        if (userName!!.trim().isEmpty()) {
            userName = "player_${UUID.randomUUID().toString().take(10)}"
            sharedPreferences.edit {
                putString("player_name", userName)
            }
        }

        val client = OkHttpClient()
        val json = """
        {
            "player": "$userName",
            "score": $score
        }
        """
        val mediaType = "application/json".toMediaType()
        val body = json.toRequestBody(mediaType)

        val secret = BuildConfig.SECRET;

        val request = Request.Builder()
            .url("http://10.0.2.2:3000/rating")
            .post(body)
            .addHeader("android-secret", secret)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("test:333", e.toString())
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("test:333", response.toString())
            }
        })
    }

    private fun addScore(score: Int) {
        bindingClass.score.text = score.toString()
    }

    private fun addRows(rows: Int) {
        bindingClass.rows.text = rows.toString()
    }
}