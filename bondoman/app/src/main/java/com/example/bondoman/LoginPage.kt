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
import android.content.Intent
import android.view.textclassifier.ConversationActions.Message
import android.widget.Toast
import com.example.bondoman.utils.AuthManager
import com.google.gson.Gson
import org.json.JSONObject

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
                       AuthManager.saveToken(this@LoginPage, token)
                       val toMain = Intent(this@LoginPage, MainActivity::class.java)
                       startActivity(toMain)
                       finish()
                   }
               }
               else{
//                   Log.e("Login", "Error: ${response.code()}")
                   val errorBody = response.errorBody()?.string()
                   if(!errorBody.isNullOrEmpty()){
                       val errorMessage = JSONObject(errorBody).getString("Error")
                       showToast(errorMessage)
                   }
               }
           }
           catch (e:Exception){
               Log.e("Login", "Error: ${e.message}")
               showToast("An error occured: ${e.message}")
           }
       }

    }

    private fun showToast(message: String){
        Toast.makeText(this@LoginPage, message, Toast.LENGTH_SHORT).show()
    }
}