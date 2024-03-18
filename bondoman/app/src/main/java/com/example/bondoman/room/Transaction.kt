package com.example.bondoman.room

import android.provider.ContactsContract.CommonDataKinds.Email
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    val id: Int,
    @NonNull
    val email: String,
    @NonNull
    val field_judul: String,
    @NonNull
    val field_nominal: String,
    @NonNull
    val field_kategori: String,
    @NonNull
    val field_lokasi: String,
    @NonNull
    val createdAt: String
)