package com.example.puttask

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

class SplashScreen : AppCompatActivity() {

    private lateinit var logo: ImageView
    private lateinit var shadow: ImageView
    private lateinit var title: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        shadow = findViewById(R.id.shadow)
        logo = findViewById(R.id.logo)
        title = findViewById(R.id.title)

        startShadowAnimation()
    }

    private fun startShadowAnimation() {
        // Load the scale and fade-in animation from XML
        val shadowAnimation = AnimationUtils.loadAnimation(this, R.anim.pop_in)
        shadow.startAnimation(shadowAnimation)

        // After shadow animation, start logo bounce animation
        Handler(Looper.getMainLooper()).postDelayed({
            startLogoBounceAnimation()
        }, shadowAnimation.duration + 3000)
    }

    private fun startLogoBounceAnimation() {
        // Load the bounce animation from XML
        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce)

        // Set the initial position of the logo above the shadow
        logo.alpha = 0f
        logo.translationY = 90f // Adjust this value to position the logo above the shadow

        // Fade in the logo
        logo.animate().alpha(1f).setDuration(500).start()

        // Start the bounce animation
        logo.startAnimation(bounceAnimation)

        // After bounce animation, start fade out shadow and handle delay before spinning the logo
        Handler(Looper.getMainLooper()).postDelayed({
            startShadowFadeOut()

            // Delay for the logo to stay centered before starting the spin
            Handler(Looper.getMainLooper()).postDelayed({
                startLogoSpinAnimation()
            }, 3000) // Delay before spin starts, adjust as needed
        }, bounceAnimation.duration + 3000) // Adjust delay to match the total duration
    }

    private fun startShadowFadeOut() {
        // Load the fade-out animation from XML
        val fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        shadow.startAnimation(fadeOutAnimation)

        // Ensure the shadow is gone after the fade-out animation completes
        fadeOutAnimation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                shadow.visibility = ImageView.GONE
            }
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
    }

    private fun startLogoSpinAnimation() {
        // Load the spin animation from XML
        val spinAnimation = AnimationUtils.loadAnimation(this, R.anim.spin)
        logo.startAnimation(spinAnimation)

        // Move to the next activity after the spin animation
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginSignin::class.java))
            finish()
        }, spinAnimation.duration + 500) // Adjust delay to match the total duration
    }
}
