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

object UserRepository {

    // Local cache to store user data
    private val userCache: MutableMap<String, User> = mutableMapOf()
    private val users : HashMap<String, User> = HashMap()

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun fetchUserDataWithCache(userId: String): User? {
        // Check if user data is already available in the cache
        if (userCache.containsKey(userId)) {
            return userCache[userId]
        }

        // If user data is not in the cache, fetch it from Firestore
        return try {
            val documentSnapshot = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
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

    fun addUserToFirestore(user: FirebaseUser) {
        val userData = hashMapOf(
            "id" to user.uid,
            "username" to user.displayName,
            "photoUrl" to user.photoUrl.toString()
        )

        if (user.photoUrl != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("users/${user.uid}.png")
            storageRef.putFile(user.photoUrl!!)
        }

        usersCollection.document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                // User added successfully
            }
            .addOnFailureListener { e ->
                // Error adding user
            }
    }

    fun getCachedUserById(userId: String) : User? {
        return if (userCache.containsKey(userId)) userCache[userId] else null
    }

    fun clearCache() {
        userCache.clear()
    }

    fun getUserById(userId: String) {
        if (!users.containsKey(userId)) {
            // Query the Firestore collection to get the user document with the provided user ID
            usersCollection
                .document(userId)
                .get().addOnSuccessListener { document ->


                    if (document != null && document.exists()) {
                        // Document exists, you can access its data here
                        val user = document.toObject(User::class.java)
                        users[userId] = user!!
                        // Use the userName or other data as needed
                    } else {
                        // Document doesn't exist or is null
                    }
                }
                .addOnFailureListener { exception ->
                    {

                    }
                }
        }
    }

    fun updateUserInFirestore(userId: String, displayName: String, photoUri: Uri) {
        val userData = hashMapOf<String, Any?>(
            "username" to displayName,
            "photoUrl" to photoUri.toString()
        )

        if (photoUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("users/${userId}.png")
            storageRef.putFile(photoUri)
        }

        usersCollection.document(userId)
            .update(userData)
            .addOnSuccessListener {
                // User updated successfully
            }
            .addOnFailureListener { e ->
                // Error updating user
            }
    }

    // Function to get the username from Firestore given a user ID using coroutines
    fun getUsernameFromUserId(userId: String): String? {
        return if (userCache.containsKey(userId)) userCache[userId]!!.username else null
    }

    private suspend fun uploadUserImageToStorage(localImagePath: String) {
        try {
            val storageReference =
                FirebaseStorage.getInstance().reference.child("users/${File(localImagePath).name}")
            val uri = Uri.fromFile(File(localImagePath))
            storageReference.putFile(uri).await()
            Log.d(TAG, "Cover image uploaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading cover image to Firebase Storage", e)
            throw e
        }
    }

    private suspend fun fetchUserImageFromStorage(userId: String): File? {
        // Implement fetching image from Firebase Storage
        // Save the fetched image locally and return the file path
        // Use coroutine to perform asynchronous operations
        return withContext(Dispatchers.IO) {
            try {
                // Fetch image from Firebase Storage
                val storageReference =
                    FirebaseStorage.getInstance().reference.child("users/${userId}.png")
                val localFile = File(Environment.getExternalStorageDirectory().absolutePath + "/users/" + userId + ".png")

                if (!localFile.exists()) {
                    localFile.createNewFile()
                }

                storageReference.getFile(localFile).await()

                localFile // Return the local image file
            } catch (e: Exception) {
                Log.e("ReviewsViewModel", "Error fetching image from Firebase Storage", e)
                null
            }
        }
    }

}
