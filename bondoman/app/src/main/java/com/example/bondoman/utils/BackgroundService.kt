package com.example.bondoman.utils

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import com.example.bondoman.LoginPage
import com.example.bondoman.retrofit.Retrofit
import com.example.bondoman.retrofit.endpoint.EndpointCheckExpiry
import com.example.bondoman.retrofit.request.CheckExpiryRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant

class BackgroundService : Service() {
    private val handler = Handler()
    private val delayMillis = 30000L
    private val currentTime = Instant.now().epochSecond

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            // Check token expiry logic using Retrofit
            GlobalScope.launch(Dispatchers.IO) {
                val token = AuthManager.getToken(this@BackgroundService)
                val prefixedToken = "Bearer $token"
                val request = CheckExpiryRequest(prefixedToken)
                val retro = Retrofit.getInstance().create(EndpointCheckExpiry::class.java)
                val response = retro.getExpiry(request)
                if (response.isSuccessful) {
                    val dataExpiry = response.body()
                    if (dataExpiry != null) {
                        // Handle token expiry logic here
                        val exp = dataExpiry.exp
                        if (currentTime > exp) {
                            AuthManager.deleteToken(this@BackgroundService)
                            val intent = Intent(this@BackgroundService, LoginPage::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            showToast("Token has expired")
                        }
                    }
                } else {
                    // Handle unsuccessful response (e.g., token expired)
                    // For demonstration, we'll just show a toast
                    showToast("Token is expired")
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the runnable when the service starts
        handler.post(runnable)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove any pending callbacks to prevent memory leaks
        handler.removeCallbacks(runnable)
    }
    private fun showToast(message: String){
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}