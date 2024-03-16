package com.example.bondoman

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.bondoman.utils.AuthManager

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler(Looper.getMainLooper()).postDelayed(
            {
                goToLoginPage()
            }, 3000L)
    }
    private fun goToLoginPage(){
        val token = AuthManager.getToken(this)
        val destination = if (token != null) MainActivity::class.java else LoginPage::class.java
        val intent = Intent(this, destination)
        startActivity(intent)
        finish()
    }
}