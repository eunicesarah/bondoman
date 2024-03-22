package com.example.bondoman.retrofit.endpoint

import com.example.bondoman.retrofit.data.ItemResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface EndpointScan {
    @POST("api/bill/upload")
    @Multipart
    suspend fun scan(
        @Header("Authorization") token: String,
        @Part file : MultipartBody.Part
    ): Response<ItemResponse>
}