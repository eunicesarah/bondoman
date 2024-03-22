package com.example.bondoman.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.*

@Dao
interface TransactionDao {
    @Insert
    suspend fun addTransaction(transaction: Transaction)
    @Query ("SELECT * FROM transactions WHERE id=:idTrans")
    suspend fun findIdTrans(idTrans: Int):Transaction?

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): List<Transaction>

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

}