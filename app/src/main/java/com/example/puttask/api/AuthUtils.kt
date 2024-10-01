//package com.example.puttask.api
//
//import android.content.Context
//import android.content.SharedPreferences
//
//object AuthUtils {
//    private const val PREFS_NAME = "auth_prefs"
//    private const val TOKEN_KEY = "auth_token"
//
//    private var sharedPreferences: SharedPreferences? = null
//
//    fun init(context: Context) {
//        sharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//    }
//
//    fun storeToken(token: String) {
//        sharedPreferences?.edit()?.apply {
//            putString(TOKEN_KEY, token)
//            apply()
//        }
//    }
//
//    fun getToken(): String? {
//        return sharedPreferences?.getString(TOKEN_KEY, null)
//    }
//}
