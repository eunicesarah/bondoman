package com.example.bondoman.room

interface TransactionRepository {
    suspend fun getAllTransactions(): List<Transaction>
    fun setNIM()
}