package edu.uw.minh2804.rekognition

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

// Base class for all other activities within this app.
// This allow for consistency across the app.
open class ActionBarActivity(contentLayoutId: Int, private val hideIcon: Boolean = false) : AppCompatActivity(contentLayoutId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (hideIcon) return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (hideIcon) return super.onOptionsItemSelected(item)
        // This is implemented with a switch statement to easily support adding more icon types
        return when (item.itemId) {
            R.id.miHistory -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}