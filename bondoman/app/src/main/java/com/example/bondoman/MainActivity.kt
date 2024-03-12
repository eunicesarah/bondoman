package com.example.bondoman

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bondoman.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
               R.id.scan_cam ->replaceFragment(ScanPage())
                R.id.graph ->replaceFragment(GraphPage())
                R.id.settings ->replaceFragment(SettingsPage())
                R.id.bill ->replaceFragment(TransactionPage())

            }
            true
        }

    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}