package com.example.bondoman

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.retrofit.Retrofit
import com.example.bondoman.retrofit.endpoint.EndpointLogin
import com.example.bondoman.retrofit.request.LoginRequest
import android.widget.Button
import android.widget.EditText
import com.example.bondoman.retrofit.data.DataLogin
import kotlinx.coroutines.launch
import java.lang.Exception

class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        initAction()
    }

    fun initAction(){
        val button_login = findViewById<Button>(R.id.button_login)
        button_login.setOnClickListener{
            login()
        }
    }

   fun login(){
       val email_field = findViewById<EditText>(R.id.email_field)
       val password_field = findViewById<EditText>(R.id.password_field)
       val request = LoginRequest(email_field.text.toString().trim(), password_field.text.toString().trim())
       val retro = Retrofit.getInstance().create(EndpointLogin::class.java)
       lifecycleScope.launch {
           try {
               val response = retro.getLogin(request)
               if (response.isSuccessful){
                   val dataLogin = response.body()
                   if(dataLogin != null)
                   {
                       val token = dataLogin.token
                       Log.d("Login", "Token: $token")
                   }
               }
               else{
                   Log.e("Login", "Failed: ${response.code()}")
               }
           }
           catch (e:Exception){
               Log.e("Login", "Error: ${e.message}")
           }
       }

    }
}