package com.example.bondoman

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class HeaderTransaction : Fragment() {
   override fun onCreate(savedInstanceState: Bundle?){
       super.onCreate(savedInstanceState)
   }

    override fun onCreateView(inflater: LayoutInflater, container:ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater.inflate(R.layout.fragment_header_transaction, container, false)
    }
}