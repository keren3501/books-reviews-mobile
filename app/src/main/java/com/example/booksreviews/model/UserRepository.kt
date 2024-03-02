package com.example.booksreviews.model

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object UserRepository {

    private val users : HashMap<String, User> = HashMap()

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    fun addUserToFirestore(user: FirebaseUser) {
        val userData = hashMapOf(
            "id" to user.uid,
            "displayName" to user.displayName,
            "photoUrl" to user.photoUrl.toString()
        )

        usersCollection.document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                // User added successfully
            }
            .addOnFailureListener { e ->
                // Error adding user
            }
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

    fun updateUserInFirestore(user: FirebaseUser) {
        val userData = hashMapOf<String, Any?>(
            "displayName" to user.displayName,
            "photoUrl" to user.photoUrl.toString()
        )

        usersCollection.document(user.uid)
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
        return if (users.containsKey(userId)) users[userId]!!.displayName else null
    }
}
