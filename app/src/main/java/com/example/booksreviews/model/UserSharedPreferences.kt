package com.example.booksreviews.model

import android.content.Context
import android.content.SharedPreferences

/**
 * Singleton object responsible for managing user preferences using SharedPreferences.
 */
object UserSharedPreferences {
    private const val PREFS_NAME = "UserPrefs"
    private const val KEY_USER_ID = "userId"

    /**
     * Saves the user ID to SharedPreferences.
     *
     * @param context The application context.
     * @param userId The ID of the user to save.
     */
    fun saveUser(context: Context, userId: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_USER_ID, userId)
        editor.apply()
    }

    /**
     * Retrieves the user ID from SharedPreferences.
     *
     * @param context The application context.
     * @return The user ID, or null if not found.
     */
    fun getUser(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_ID, null)
    }

    /**
     * Clears the user data from SharedPreferences.
     *
     * @param context The application context.
     */
    fun clearUser(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
