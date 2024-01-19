package com.example.booksreviews.view;

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.booksreviews.R
import com.example.booksreviews.model.Review
import com.example.booksreviews.viewmodel.ReviewsViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.loginFragment, R.id.homeFragment, R.id.myAccountFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        ViewModelProvider(this).get(ReviewsViewModel::class.java).addReview(Review(0,0, "keren", "keren", "keren", "keren"))
    }
}
