package com.example.booksreviews.model

class User (val id: String,
            val username: String,
            val imageUrl: String) {
    // Empty constructor (default constructor)
    constructor() : this("", "", "")

    // Copy constructor
    constructor(user: User) : this(
        user.id,
        user.username,
        user.imageUrl)
}

