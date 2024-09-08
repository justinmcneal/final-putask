package com.example.puttask

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.animation.AnimationUtils
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Find the logo and title by ID
        val logoImage: ImageView = findViewById(R.id.putasklogo)
        val appTitle: TextView = findViewById(R.id.appTitle)

        // Load the spin and move animation
        val spinAndMoveAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_spin)
        // Start the spin and move animation on the logo
        logoImage.startAnimation(spinAndMoveAnimation)

        // Set a listener to start zoom animation after spin is done
        spinAndMoveAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}

            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                // Load zoom animation
                val zoomAnimation = AnimationUtils.loadAnimation(this@SplashScreen, R.anim.logo_zoom)

                // Zoom the logo and make title visible with animation
                logoImage.startAnimation(zoomAnimation)
                appTitle.visibility = View.VISIBLE
                appTitle.startAnimation(zoomAnimation)
            }

            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginSignin::class.java))
            finish()
        }, 7000)
    }
}

