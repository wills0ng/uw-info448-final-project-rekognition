package edu.uw.minh2804.rekognition.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import edu.uw.minh2804.rekognition.AccessibilityActivity
import edu.uw.minh2804.rekognition.R

class MainFragment : Fragment(R.layout.fragment_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_main_to_camera).setOnClickListener {
            val intent = Intent(activity, AccessibilityActivity::class.java)
            startActivity(intent)
        }
    }
}