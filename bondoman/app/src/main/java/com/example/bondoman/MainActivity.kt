package com.example.bondoman

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.bondoman.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment
import com.example.bondoman.room.TransactionDB
import com.example.bondoman.room.TransactionRepository
import com.example.bondoman.room.TransactionRepositoryImplement
import com.example.bondoman.utils.AuthManager
import com.example.bondoman.utils.CheckExpiryToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Base64

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val db by lazy { TransactionDB(this) }
    private lateinit var transactionRepository: TransactionRepository

    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkNetworkAndLoadTransactions()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serviceIntent = Intent(this, CheckExpiryToken::class.java)
        startService(serviceIntent)

        replaceFragment(TransactionPage(), HeaderTransaction())

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
        registerNetworkChangeReceiver()
        checkNetworkAndLoadTransactions()
    }

    override fun onStop() {
        super.onStop()
        unregisterNetworkChangeReceiver()
    }

    private fun registerNetworkChangeReceiver() {
        registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    private fun unregisterNetworkChangeReceiver() {
        unregisterReceiver(networkChangeReceiver)
    }

    private fun checkNetworkAndLoadTransactions() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return
        val networkInfo = connectivityManager.getNetworkCapabilities(networkCapabilities)

        if (networkInfo != null && networkInfo.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            loadTransactions()
        } else {
            Toast.makeText(this, "Your connection is lost. Showing cached transactions.", Toast.LENGTH_SHORT).show()
            loadCachedTransactions()
        }
    }

    private fun loadTransactions() {
        transactionRepository = TransactionRepositoryImplement(db.transactionDao(), this)
        transactionRepository.setNIM()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val transc = transactionRepository.getAllTransactions()
                Log.d("MainActivity", "dbResponse: $transc")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading transactions: ${e.message}")
            }
        }
    }

    private fun loadCachedTransactions() {
        transactionRepository = TransactionRepositoryImplement(db.transactionDao(), this)
        transactionRepository.setNIM()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val transc = transactionRepository.getAllTransactions()
                Log.d("MainActivity", "Cached transactions: $transc")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading cached transactions: ${e.message}")
            }
        }
    }

    fun replaceFragment(fragment: Fragment, header: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()

        supportFragmentManager.beginTransaction()
            .replace(R.id.header_layout, header)
            .commit()

        supportFragmentManager.executePendingTransactions()
    }
}