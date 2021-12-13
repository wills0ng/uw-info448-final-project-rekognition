package edu.uw.minh2804.rekognition

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity(R.layout.activity_history) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        return true
    }
}