package com.example.puttask

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.View
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.puttask.authentications.LoginSignin
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
        val spinToRightAnimation = AnimationUtils.loadAnimation(this, R.anim.spin_to_right)
        val spinToLeftAnimation = AnimationUtils.loadAnimation(this, R.anim.spin_to_left)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // Start animations
        putasklogo.startAnimation(spinToLeftAnimation.apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation?) {
                    putasklogo.startAnimation(spinToRightAnimation)
                    title.apply {
                        visibility = View.VISIBLE
                        startAnimation(fadeInAnimation)
                    }
                }
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        })

        // Delay before navigating to the next activity
        val token = dataManager.getAuthToken()
        putasklogo.postDelayed({
            startActivity(Intent(this, if (token != null) MainActivity::class.java else LoginSignin::class.java))
            finish()
        }, delayDuration)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear animations to prevent memory leaks
        putasklogo.clearAnimation()
        title.clearAnimation()
    }
}
