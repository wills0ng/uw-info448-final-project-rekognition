/** Shane Fretwell: I was responsible for the contents of this file and it's corresponding xml **/

package edu.uw.minh2804.rekognition

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController

// The History activity extends the functionality of the ActionBarActivity to support Toolbar up
// navigation with a nav graph
class HistoryActivity : ActionBarActivity(R.layout.activity_history, hideIcon = true) {
    private lateinit var navController: NavController
    private lateinit var appBarConfig : AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.findFragmentById(R.id.fragment_container_history_host).let {
            navController = (it as NavHostFragment).navController
            appBarConfig = AppBarConfiguration(navController.graph)
            setupActionBar()
        }
    }

    private fun setupActionBar() {
        setupActionBarWithNavController(navController, appBarConfig)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfig)
    }
}