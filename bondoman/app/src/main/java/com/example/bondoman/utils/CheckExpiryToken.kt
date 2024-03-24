package com.example.bondoman.utils

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.bondoman.LoginPage
import com.example.bondoman.retrofit.endpoint.EndpointCheckExpiry
import com.example.bondoman.retrofit.interceptor.AuthInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory

class CheckExpiryToken:Service() {
    private lateinit var handler: Handler

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val token = AuthManager.getToken(this) ?: ""

        handler = Handler()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val interceptor = AuthInterceptor(token)
                val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build()
                val retro = retrofit2.Retrofit.Builder()
                    .baseUrl("https://pbd-backend-2024.vercel.app/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val request = retro.create(EndpointCheckExpiry::class.java)
                val response = request.getExpiry()


                if (response.isSuccessful) {
                    val dataExpiry = response.body()
                    if (dataExpiry != null) {
                        val exp = dataExpiry.exp
                        val currentTime = System.currentTimeMillis()/1000
                        if (currentTime>exp){
                            Handler(Looper.getMainLooper()).post { showToast("Token is expired. Please log in.") }
                            AuthManager.deleteToken(this@CheckExpiryToken)
                            val intent = Intent(this@CheckExpiryToken, LoginPage::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                        }
                    }
                } else {
                    val intent = Intent(this@CheckExpiryToken, LoginPage::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    showToast("Token is expired. Please log in.")
                }
            }
            catch (e: Exception){
                Log.e("CheckExpiry", "Error loading transactions: ${e.message}")
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showToast(message: String){
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this@CheckExpiryToken, message, Toast.LENGTH_SHORT).show()
        }
    }
}
