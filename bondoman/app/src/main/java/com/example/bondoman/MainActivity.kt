package com.example.bondoman

import android.app.AlertDialog
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
import android.widget.Button
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

    private val networkStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!isNetworkAvailable()) {
                showLostConnectionDialog()
            }
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
                R.id.scan_cam -> {if(!isNetworkAvailable()){replaceFragment(NetworkLostPage(), HeaderScan())}
                            else{replaceFragment(ScanPage(), HeaderScan())}}
                R.id.graph -> replaceFragment(GraphPage(), HeaderGraf())
                R.id.settings -> replaceFragment(SettingsPage(), HeaderSettings())
                R.id.bill -> replaceFragment(TransactionPage(), HeaderTransaction())
                R.id.twibbon -> replaceFragment(TwibbonPage(), HeaderTwibbon())
            }
            true
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val networkInfo = connectivityManager.getNetworkCapabilities(networkCapabilities)
        return networkInfo != null && networkInfo.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(networkStatusReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkStatusReceiver)
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

    fun showLostConnectionDialog(){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_network_lost, null)

        val tryAgainButton = dialogView.findViewById<Button>(R.id.close_popup)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        tryAgainButton.setOnClickListener {
            dialog.dismiss()
        }
    }

}