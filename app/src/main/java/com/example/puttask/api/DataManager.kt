package com.example.puttask.api

import android.content.Context
import android.content.SharedPreferences

class DataManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Save the token to SharedPreferences
    fun saveToken(token: String) {
        sharedPreferences.edit().apply {
            putString("auth_token", token)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    // Retrieve the token from SharedPreferences
    fun getToken(): String {
        return sharedPreferences.getString("auth_token", "") ?: ""
    }

    // Clear the token from SharedPreferences
    fun clearToken() {
        sharedPreferences.edit().apply {
            remove("auth_token")
            putBoolean("isLoggedIn", false)
            apply()
        }
    }

    // Save user credentials for Remember Me feature
    fun saveCredentials(email: String, password: String) {
        sharedPreferences.edit().apply {
            putString("saved_email", email)
            putString("saved_password", password)
            apply()
        }
    }

    // Retrieve saved email from SharedPreferences
    fun getSavedEmail(): String {
        return sharedPreferences.getString("saved_email", "") ?: ""
    }

    // Retrieve saved password from SharedPreferences
    fun getSavedPassword(): String {
        return sharedPreferences.getString("saved_password", "") ?: ""
    }

    // Clear saved credentials
    fun clearCredentials() {
        sharedPreferences.edit().apply {
            remove("saved_email")
            remove("saved_password")
            apply()
        }
    }
}
