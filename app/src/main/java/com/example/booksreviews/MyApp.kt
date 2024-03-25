package com.example.booksreviews

import android.app.Application
import com.example.booksreviews.model.ImageCacheDatabase

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ImageCacheDatabase.getDatabase(this)
    }
}
