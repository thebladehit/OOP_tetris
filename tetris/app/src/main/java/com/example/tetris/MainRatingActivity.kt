package com.example.tetris

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tetris.databinding.ActivityMainRatingBinding
import com.example.tetris.models.playerScore.PlayerScore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainRatingActivity : AppCompatActivity() {
    private lateinit var bindingClass: ActivityMainRatingBinding
    private lateinit var recycler: RecyclerView
    private var client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle??) {
        super.onCreate(savedInstanceState)
        bindingClass = ActivityMainRatingBinding.inflate(layoutInflater)
        setContentView(bindingClass.root)

        recycler = findViewById(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)


        this.getRating()
    }

    private fun getRating() {
        val sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
        val playerName = sharedPreferences.getString("player_name", "")

        val request = Request.Builder()
            .url("http://10.0.2.2:3000/rating")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    val gson = Gson()
                    val type = object : TypeToken<List<PlayerScore>>() {}.type
                    val list: List<PlayerScore> = gson.fromJson(jsonString, type)

                    runOnUiThread {
                        recycler.adapter = PlayerAdapter(list, playerName!!)
                    }
                }
            }
        })
    }
}