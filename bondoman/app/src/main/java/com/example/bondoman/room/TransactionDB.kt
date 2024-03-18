package com.example.bondoman.room

import android.content.Context
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import com.example.bondoman.AddTransactionPage


@Database(
    entities = [Transaction::class],
    version = 1
)

abstract class TransactionDB : RoomDatabase(){
    abstract fun transactionDao() : TransactionDao
    companion object{
        @Volatile
        private var instance: TransactionDB? = null
        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock){
            instance ?: createDatabase(context).also{
                instance = it
            }
        }
        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            TransactionDB::class.java, "bondoman"
        ).build()
    }
}