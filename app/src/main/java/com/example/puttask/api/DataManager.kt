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
}
