package com.example.bondoman

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.bondoman.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment
import com.example.bondoman.room.TransactionDB
import com.example.bondoman.utils.CheckExpiryToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val db by lazy { TransactionDB(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serviceIntent = Intent(this, CheckExpiryToken::class.java)
        startService(serviceIntent)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.scan_cam -> replaceFragment(ScanPage(), HeaderScan())
                R.id.graph -> replaceFragment(GraphPage(), HeaderGraf())
                R.id.settings -> replaceFragment(SettingsPage(), HeaderSettings())
                R.id.bill -> replaceFragment(TransactionPage(), HeaderTransaction())
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

    private fun replaceFragment(fragment: Fragment, header: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()

        supportFragmentManager.beginTransaction()
            .replace(R.id.header_layout, header)
            .commit()
    }
}
