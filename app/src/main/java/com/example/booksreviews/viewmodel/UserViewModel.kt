package com.example.booksreviews.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksreviews.model.User
import com.example.booksreviews.model.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

/**
 * ViewModel for managing user-related data and interactions.
 */
class UserViewModel : ViewModel() {

    var userId: String? = null

    /**
     * LiveData to indicate loading state.
     */
    val isLoading = MutableLiveData<Boolean>().apply { value = false }

    var hasUserChanged: Boolean = true

    /**
     * Fetches user data with the specified user ID.
     *
     * @param userId The ID of the user to fetch data for.
     */
    fun fetchUserData(userId: String) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                UserRepository.fetchUserDataWithCache(userId)
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Adds a new user to Firestore.
     *
     * @param user The FirebaseUser object representing the new user.
     */
    fun addUser(user: FirebaseUser) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                UserRepository.addUserToFirestore(user)
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Retrieves cached user data by user ID.
     *
     * @param userId The ID of the user to retrieve cached data for.
     * @return The cached user data, or null if not found.
     */
    fun getCachedUserById(userId: String): User? {
        return UserRepository.getCachedUserById(userId)
    }

    /**
     * Updates user data in Firestore.
     *
     * @param userId The ID of the user to update.
     * @param displayName The new display name for the user.
     * @param photoUri The new profile photo URI for the user.
     */
    fun updateUser(userId: String, displayName: String, photoUri: Uri?) {
        isLoading.value = true // Set loading state to true before updating user data
        viewModelScope.launch {
            try {
                UserRepository.updateUserInFirestore(userId, displayName, photoUri)
            } finally {
                isLoading.value = false // Set loading state to false after updating user data
            }
        }
    }

    /**
     * Retrieves the username from the user ID.
     *
     * @param userId The ID of the user to retrieve the username for.
     * @return The username of the user, or null if not found.
     */
    fun getUsernameFromUserId(userId: String): String? {
        return UserRepository.getUsernameFromUserId(userId)
    }
}
