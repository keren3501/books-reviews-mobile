package com.example.booksreviews.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class UserViewModel : ViewModel() {
    lateinit var user: FirebaseUser
}
