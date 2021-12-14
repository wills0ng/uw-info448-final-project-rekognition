package edu.uw.minh2804.rekognition

import android.os.Bundle
import edu.uw.minh2804.rekognition.services.FirebaseAuthService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ActionBarActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch { FirebaseAuthService.signIn() }
    }
}