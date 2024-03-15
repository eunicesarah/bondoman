package com.example.bondoman.retrofit.data

import com.google.gson.annotations.SerializedName

data class DataLogin(
    @SerializedName("token") val token: String
)
