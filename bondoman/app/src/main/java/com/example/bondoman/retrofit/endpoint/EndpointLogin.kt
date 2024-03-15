package com.example.bondoman.retrofit.endpoint

import com.example.bondoman.retrofit.data.DataLogin
import com.example.bondoman.retrofit.request.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EndpointLogin {
    @POST("api/auth/login")
    suspend fun getLogin(@Body request: LoginRequest) : Response<DataLogin>
}