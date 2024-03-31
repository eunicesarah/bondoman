package com.example.bondoman.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RandomizeTransaction : BroadcastReceiver() {

    companion object {
        var shouldRandomizePrice = false
        var randomPrice:Int? = null
    }
    override fun onReceive(context: Context, intent: Intent) {
        randomPrice = intent.getIntExtra("randomPrice", 0)
        shouldRandomizePrice = true
    }
}