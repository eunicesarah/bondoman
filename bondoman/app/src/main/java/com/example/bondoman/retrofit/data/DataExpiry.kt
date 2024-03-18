package com.example.bondoman.retrofit.data

import com.google.gson.annotations.SerializedName

data class DataExpiry(
    @SerializedName("nim") val nim: String,
    @SerializedName("iat") val iat: Long,
    @SerializedName("exp") val exp:Long
)
