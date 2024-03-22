package com.example.bondoman.retrofit.data

data class Items(
    val items: List<Item>
)

data class Item(
    val name: String,
    val qty: Int,
    val price: Double
)