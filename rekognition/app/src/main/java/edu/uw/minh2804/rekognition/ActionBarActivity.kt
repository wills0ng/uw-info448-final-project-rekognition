/** Shane Fretwell: I was responsible for the contents of this file and it's corresponding xml and theme files. **/

package edu.uw.minh2804.rekognition

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

// Base activity that enables the action bar by default.
// This allows for consistency across the app. hideIcon is for the history activity, which should
// not have navigation to itself
open class ActionBarActivity(contentLayoutId: Int, private val hideIcon: Boolean = false) : AppCompatActivity(contentLayoutId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    // Adds icons to toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (hideIcon) return super.onCreateOptionsMenu(menu)
        // Pass in the menu resource that defines the icons to be displayed
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    // Handles when an icon is selected
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