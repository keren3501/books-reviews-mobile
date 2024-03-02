package com.example.booksreviews.model

class User (val id: String,
            val displayName: String,
            val photoUrl: String) {
    // Empty constructor (default constructor)
    constructor() : this("", "", "")

    // Copy constructor
    constructor(user: User) : this(
        user.id,
        user.displayName,
        user.photoUrl)
}

