package com.example.puttask.api

import android.content.Context
import android.content.SharedPreferences

class DataManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun saveCredentials(email: String, password: String) {
        sharedPreferences.edit()
            .putString("saved_email", email)
            .putString("saved_password", password)
            .apply()
    }

    fun clearCredentials() {
        sharedPreferences.edit()
            .remove("saved_email")
            .remove("saved_password")
            .remove("username") // Clear username too
            .apply()
    }

    fun saveEmail(email: String) {
        sharedPreferences.edit().putString("saved_email", email).apply() // Ensure this matches the retrieval
    }

    fun getSavedEmail(): String {
        return sharedPreferences.getString("saved_email", "") ?: ""
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit().putString("username", username).apply()
    }

    fun getUsername(): String {
        return sharedPreferences.getString("username", "") ?: ""
    }

    fun getSavedPassword(): String {
        return sharedPreferences.getString("saved_password", "") ?: ""
    }
}
