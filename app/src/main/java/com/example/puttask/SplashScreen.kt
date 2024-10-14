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

    private lateinit var dataManager: DataManager
    private lateinit var putasklogo: ImageView
    private lateinit var title: TextView

    private val delayDuration: Long = 8000 // 8 seconds delay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        dataManager = DataManager(this)

        putasklogo = findViewById(R.id.putasklogo)
        title = findViewById(R.id.tvTitle)

        // Load animations
        val spinToRightAnimation = AnimationUtils.loadAnimation(this, R.anim.spin_to_right)
        val spinToLeftAnimation = AnimationUtils.loadAnimation(this, R.anim.spin_to_left)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // Start the left spin animation on the logo
        putasklogo.startAnimation(spinToLeftAnimation)

        // Set animation listener for the spin-to-left animation
        spinToLeftAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Start the right spin animation after the left spin ends
                putasklogo.startAnimation(spinToRightAnimation)

                // Start fading in the title
                title.visibility = View.VISIBLE
                title.startAnimation(fadeInAnimation)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        // Check for authentication token
        val token = dataManager.getAuthToken()

        // Delay before navigating to the next activity
        Handler(Looper.getMainLooper()).postDelayed({
            if (token != null) {
                // Token exists, navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // No token, navigate to LoginSignin
                startActivity(Intent(this, LoginSignin::class.java))
            }
            finish() // Close the splash screen
        }, delayDuration)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear animations to prevent memory leaks
        putasklogo.clearAnimation()
        title.clearAnimation()
    }
}
