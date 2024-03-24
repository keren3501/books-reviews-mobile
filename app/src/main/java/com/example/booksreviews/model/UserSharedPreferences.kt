package com.example.booksreviews.model

import android.content.Context
import android.content.SharedPreferences

object UserSharedPreferences {
    private const val PREFS_NAME = "UserPrefs"
    private const val KEY_USER_ID = "userId"
    // Add more keys for other user data as needed

    fun saveUser(context: Context, userId: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_USER_ID, userId)
        editor.apply()
    }

    fun getUser(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_ID, null)
    }

    fun clearUser(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
