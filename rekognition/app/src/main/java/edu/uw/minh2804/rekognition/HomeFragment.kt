package edu.uw.minh2804.rekognition

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        to_camera_button.setOnClickListener {
            val intent = Intent(activity, CameraActivity::class.java)
            startActivity(intent)
        }
    }
}