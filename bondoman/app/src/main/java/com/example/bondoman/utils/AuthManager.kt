package com.example.bondoman.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.nio.charset.StandardCharsets

object AuthManager {
    private const val PREFS_NAME = "MyPreferences"
    private const val TOKEN_KEY = "token"

    fun getSharedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFS_NAME, masterKeyAlias, context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(context: Context, token: String){
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    fun getToken(context: Context): String? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    fun deleteToken(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.remove(TOKEN_KEY)
        editor.apply()
    }
    fun getTokenDec(context: Context): String? {
        val sharedPreferences = getSharedPreferences(context)
        val encryptedToken = sharedPreferences.getString(TOKEN_KEY, null)
        // Decrypt the token if it's not null
        return encryptedToken?.let { decryptToken(it) }
    }

    private fun decryptToken(encryptedToken: String): String? {
        // Decrypt the token using your decryption logic
        // For example, if your token is Base64 encoded, you can decode it
        return try {
            val decodedBytes = android.util.Base64.decode(encryptedToken, android.util.Base64.DEFAULT)
            String(decodedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

}
