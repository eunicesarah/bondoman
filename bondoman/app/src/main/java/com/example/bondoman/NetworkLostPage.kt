package com.example.bondoman

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class NetworkLostPage : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var back_transc: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_network_lost_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back_transc = view.findViewById(R.id.back_transc)

        setUpListener()
    }

    fun setUpListener(){
        back_transc.setOnClickListener {
            val transactionPageFragment = TransactionPage()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, transactionPageFragment)
                .commit()
        }
    }


}