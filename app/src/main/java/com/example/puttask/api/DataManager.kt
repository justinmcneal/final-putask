package com.example.puttask.api

import android.content.Context
import android.content.SharedPreferences

class DataManager(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // Save the authentication token
    fun saveAuthToken(token: String) {
        preferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
        preferences.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
    }

    // Get the authentication token
    fun getAuthToken(): String? {
        return preferences.getString(KEY_AUTH_TOKEN, null)
    }

    // Clear only the auth token
    fun clearAuthToken() {
        preferences.edit().remove(KEY_AUTH_TOKEN).apply()
    }

    fun clearLoginData() {
        preferences.edit().remove(KEY_IS_LOGGED_IN).apply()
    }

    // Clear all saved data (delete account)
    fun clear() {
        preferences.edit().clear().apply()
    }
}
