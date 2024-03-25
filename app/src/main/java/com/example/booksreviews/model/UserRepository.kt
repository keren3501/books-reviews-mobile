package com.example.booksreviews.model

import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG: String = "UserRepository"

/**
 * Repository responsible for managing user data and related operations.
 */
object UserRepository {

    // Local cache to store user data
    private val userCache: MutableMap<String, User> = mutableMapOf()

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val storageRef = FirebaseStorage.getInstance().reference

    /**
     * Fetches user data from Firestore and caches it locally if not already cached.
     *
     * @param userId The ID of the user to fetch.
     * @return The user data, or null if an error occurs.
     */
    suspend fun fetchUserDataWithCache(userId: String): User? {
        // If user data is not in the cache, fetch it from Firestore
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            val userData = documentSnapshot.toObject(User::class.java)
            if (userData != null) {
                fetchUserImageFromStorage(userId)

                // Update the cache with fetched user data
                userCache[userId] = userData
            }
            userData
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Adds a new user to Firestore.
     *
     * @param user The Firebase user to add.
     */
    suspend fun addUserToFirestore(user: FirebaseUser) {
        val userData = hashMapOf(
            "id" to user.uid,
            "username" to user.displayName,
            "photoUrl" to user.photoUrl.toString()
        )

        if (user.photoUrl != null) {
            val storageRef = storageRef.child("users/${user.uid}.png")
            storageRef.putFile(user.photoUrl!!).await()
        }

        usersCollection.document(user.uid)
            .set(userData)
            .await() // Wait for the operation to complete
    }

    /**
     * Retrieves a user from the local cache by ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The user, or null if not found in the cache.
     */
    fun getCachedUserById(userId: String): User? {
        return userCache[userId]
    }

    /**
     * Clears the local user cache.
     */
    fun clearCache() {
        userCache.clear()
    }

    /**
     * Updates user data in Firestore.
     *
     * @param userId The ID of the user to update.
     * @param displayName The new display name for the user.
     * @param photoUri The new photo URI for the user, if any.
     */
    suspend fun updateUserInFirestore(userId: String, displayName: String, photoUri: Uri?) {
        val userData = hashMapOf<String, Any?>(
            "username" to displayName
        )

        if (photoUri != null) {
            userData["photoUrl"] = photoUri.toString()
            val storageRef = storageRef.child("users/${userId}.png")
            storageRef.putFile(photoUri).await()
        }

        usersCollection.document(userId)
            .update(userData)
            .await() // Wait for the operation to complete
    }

    /**
     * Retrieves the username of a user by ID.
     *
     * @param userId The ID of the user.
     * @return The username, or null if the user is not found.
     */
    fun getUsernameFromUserId(userId: String): String? {
        return userCache[userId]?.username
    }

    /**
     * Fetches the user's profile image from Firebase Storage.
     *
     * @param userId The ID of the user.
     * @return The local file containing the user's profile image, or null if not found.
     */
    private suspend fun fetchUserImageFromStorage(userId: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val storageReference =
                    storageRef.child("users/${userId}.png")
                val localFile = File(Environment.getExternalStorageDirectory().absolutePath + "/users/" + userId + ".png")

                if (!localFile.exists()) {
                    localFile.createNewFile()
                }

                storageReference.getFile(localFile).await()

                localFile // Return the local image file
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching image from Firebase Storage", e)
                null
            }
        }
    }
}
