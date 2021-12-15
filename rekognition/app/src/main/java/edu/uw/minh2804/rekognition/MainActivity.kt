package edu.uw.minh2804.rekognition

import android.os.Bundle
import edu.uw.minh2804.rekognition.services.FirebaseAuthService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

// Main entry point for this application.
class MainActivity : ActionBarActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // GlobalScope is safe here as long as the operation can timeout within CONNECTION_TIMEOUT_IN_SECONDS.
        GlobalScope.launch {
            withTimeout(CONNECTION_TIMEOUT_IN_SECONDS) {
                // This is not required, but for a better user experience, it is better to sign into Firebase before the user interact with the app.
                FirebaseAuthService.signIn()
            }
        }
    }

    companion object {
        const val CONNECTION_TIMEOUT_IN_SECONDS = 10L
    }
}