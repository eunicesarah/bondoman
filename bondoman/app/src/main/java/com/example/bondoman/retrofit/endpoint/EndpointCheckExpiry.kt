package com.example.bondoman.retrofit.endpoint


import com.example.bondoman.retrofit.data.DataExpiry
import retrofit2.Response
import retrofit2.http.POST

interface EndpointCheckExpiry {
    @POST("api/auth/token")
    suspend fun getExpiry(): Response<DataExpiry>
}