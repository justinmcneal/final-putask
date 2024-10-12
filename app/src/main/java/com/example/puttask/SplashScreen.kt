package com.example.puttask

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AnimationUtils
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import com.example.puttask.authentications.LoginSignin
import com.example.puttask.MainActivity
import com.example.puttask.api.DataManager

class SplashScreen : AppCompatActivity() {

    private lateinit var dataManager: DataManager // Declare DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        dataManager = DataManager(this) // Initialize DataManager

        val putasklogo: ImageView = findViewById(R.id.putasklogo)
        val title: TextView = findViewById(R.id.tvTitle)  // Title text view

        // Load the animations
        val spinToRightAnimation = AnimationUtils.loadAnimation(this, R.anim.spin_to_right)
        val spinToLeftAnimation = AnimationUtils.loadAnimation(this, R.anim.spin_to_left)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // Start the right spin animation on the logo
        putasklogo.startAnimation(spinToLeftAnimation)

        // Set a listener for the spin to right animation
        spinToLeftAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Start the left spin animation after the right spin ends
                putasklogo.startAnimation(spinToRightAnimation)

                // Start fading in the title as the left spin starts
                title.visibility = View.VISIBLE
                title.startAnimation(fadeInAnimation)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        // Delay before navigating to the next activity
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if the user is already authenticated
            val token = dataManager.getAuthToken() // Fetch the saved token from DataManager

            // Navigate to MainActivity or LoginSignin after splash screen
            if (token != null) {
                // If the token is not null, go to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // If the token is null, go to LoginSignin
                startActivity(Intent(this, LoginSignin::class.java))
            }
            finish() // Close the splash screen
        }, 8000)  // Total delay of 8 seconds before transitioning
    }
}
