package com.example.bondoman.room

import android.content.Context
import android.util.Log
import com.example.bondoman.utils.AuthManager
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Base64

class TransactionRepositoryImplement(private val transactionDao: TransactionDao, private val context: Context): TransactionRepository {
    private var nimUser: String =""

    override fun setNIM() {
        nimUser = getUserNIMFromToken()
    }
    override suspend fun getAllTransactions(): List<Transaction> {
        return transactionDao.getAllTransactions().filter { it.email == nimUser }
    }
    private fun getUserNIMFromToken(): String {
        val token = AuthManager.getToken(context).toString().split('.')
        if (token.size != 3) {
            Log.e("TransactionRepository", "Invalid token format")
            return ""
        }

        val payload = Base64.getUrlDecoder().decode(token[1])
        val payloadJson = JSONObject(String(payload, StandardCharsets.UTF_8))
        return  payloadJson.optString("nim")
    }
}