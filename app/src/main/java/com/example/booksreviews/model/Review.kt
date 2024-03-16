package com.example.booksreviews.model

data class Review(
    var id: String,
    var userId: String,
    var bookCoverUrl: String,
    var bookTitle: String,
    var authorName: String,
    var reviewText: String,
    var timestamp: Long
) {
    // Empty constructor (default constructor)
    constructor() : this("", "", "", "", "", "", 0)

    // Copy constructor
    constructor(review: Review) : this(
        review.id,
        review.userId,
        review.bookCoverUrl,
        review.bookTitle,
        review.authorName,
        review.reviewText,
        review.timestamp
    )
}
