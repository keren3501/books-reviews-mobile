package com.example.booksreviews.model

import android.net.Uri

data class Review(
    var id: Int,
    var userId: String,
    var bookCoverUri: Uri,
    var bookTitle: String,
    var authorName: String,
    var reviewText: String
) {
    // Empty constructor (default constructor)
    constructor() : this(-1, "", Uri.EMPTY, "", "", "")

    // Copy constructor
    constructor(review: Review) : this(
        review.id,
        review.userId,
        review.bookCoverUri,
        review.bookTitle,
        review.authorName,
        review.reviewText
    )
}
