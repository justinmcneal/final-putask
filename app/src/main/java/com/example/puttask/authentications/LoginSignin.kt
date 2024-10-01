package com.example.puttask.authentications

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.puttask.MainActivity
import com.example.puttask.R

class LoginSignin : AppCompatActivity() {

    private lateinit var btnSignUp: Button
    private lateinit var btnLogIn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_signin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        btnSignUp = findViewById(R.id.btnSignIn)
        btnLogIn = findViewById(R.id.btnLogIn)
        
        btnSignUp.setOnClickListener {
            // Start SignUpActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnLogIn.setOnClickListener {
            // Start LogInActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}