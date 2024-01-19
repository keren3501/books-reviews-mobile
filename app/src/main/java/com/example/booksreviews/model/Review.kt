package com.example.booksreviews.model

data class Review(
    val id: Int,
    val userId: Int,
    val bookCoverPath: String,
    val bookTitle: String,
    val authorName: String,
    val reviewText: String
)
