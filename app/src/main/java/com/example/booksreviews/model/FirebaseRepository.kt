package com.example.booksreviews.model

import android.net.Uri
import android.os.Environment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getReviewById(reviewId: String): Revieww? {
        return withContext(Dispatchers.IO) {
            val documentSnapshot = firestore.collection("reviews").document(reviewId).get().await()
            documentSnapshot.toObject(Revieww::class.java)
        }
    }

    suspend fun getUserById(userId: String): Userr? {
        return withContext(Dispatchers.IO) {
            val documentSnapshot = firestore.collection("users").document(userId).get().await()
            val user = documentSnapshot.toObject(Userr::class.java)
            user?.let {
                // Check if user image exists locally
                val localImageFile =
                    File(Environment.getExternalStorageDirectory(), "users/$userId.jpg")
                if (!localImageFile.exists()) {
                    // Download user image from Firebase Storage
                    val imageUrl = user.imageUrl
                    if (imageUrl != null) {
                        downloadImageToLocal(imageUrl, localImageFile)
                    }
                }
            }
            user
        }
    }

    suspend fun getBookById(bookId: String): Bookk? {
        return withContext(Dispatchers.IO) {
            val documentSnapshot = firestore.collection("books").document(bookId).get().await()
            val book = documentSnapshot.toObject(Bookk::class.java)
            book?.let {
                // Check if book image exists locally
                val localImageFile =
                    File(Environment.getExternalStorageDirectory(), "books/$bookId.jpg")
                if (!localImageFile.exists()) {
                    // Download book image from Firebase Storage
                    val imageUrl = book.imageUrl
                    if (imageUrl != null) {
                        downloadImageToLocal(imageUrl, localImageFile)
                    }
                }
            }
            book
        }
    }

    suspend fun downloadImageToLocal(imageUrl: String, localFile: File) {
        withContext(Dispatchers.IO) {
            try {
                val storageRef = storage.getReferenceFromUrl(imageUrl)
                val stream = storageRef.stream.await()
                val outputStream = FileOutputStream(localFile)
                stream.stream.copyTo(outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
