package com.example.bondoman

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.bondoman.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bondoman.retrofit.Retrofit
import com.example.bondoman.retrofit.endpoint.EndpointCheckExpiry
import com.example.bondoman.retrofit.request.CheckExpiryRequest
import com.example.bondoman.room.Transaction
import com.example.bondoman.room.TransactionDB
import com.example.bondoman.room.TransactionDao
import com.example.bondoman.utils.AuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val db by lazy { TransactionDB(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.scan_cam -> replaceFragment(ScanPage())
                R.id.graph -> replaceFragment(GraphPage())
                R.id.settings -> replaceFragment(SettingsPage())
                R.id.bill -> replaceFragment(AddTransactionPage())
            }
            true
        }
    }

    override fun onStart() {
        super.onStart()
        loadTransactions()
    }

    private fun loadTransactions() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val transc = db.transactionDao().getAllTransactions()
                Log.d("MainActivity", "dbResponse: $transc")

            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading transactions: ${e.message}")
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}
