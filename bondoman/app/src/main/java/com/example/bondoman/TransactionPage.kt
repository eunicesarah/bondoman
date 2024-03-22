package com.example.bondoman

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.room.Transaction
import com.example.bondoman.room.TransactionDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TransactionPage : Fragment(R.layout.fragment_transaction_page), TransactionAdapter.EditTransactionListener {
    val db by lazy { TransactionDB(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button: Button = view.findViewById(R.id.button_add_transaction)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewTransaction)
        val saldoText : TextView = view.findViewById<TextView>(R.id.duit)

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels

        val recyclerViewHeight = (0.6 * height).toInt()

        recyclerView.layoutParams.height = recyclerViewHeight
        recyclerView.requestLayout()


        lifecycleScope.launch {
            val transactions = withContext(Dispatchers.IO) {
                db.transactionDao().getAllTransactions()
            }

            val balance = saldo(transactions)
            val text: String
            if (balance >= 0) {
                text = "Rp${balance.convert()}"
            } else {
                text = "-Rp${balance.convert().substring(1)}"
            }

            saldoText.text = text

            Log.d("TransactionPage", "transactions: $transactions")

            recyclerView.adapter = TransactionAdapter(transactions, this@TransactionPage)
            Log.d("TransactionPage", "recyclerView: ${recyclerView.adapter}")
        }

        button.setOnClickListener {
            val fragment = AddTransactionPage()
            val transaction : FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.frame_layout, fragment).commit()

        }
    }

    fun saldo(list : List<Transaction>) : Int {
        var saldo = 0
        for (i in list) {
            if (i.field_kategori == "Pemasukan") {
                saldo += i.field_nominal.toInt()
            } else {
                saldo -= i.field_nominal.toInt()
            }
        }
        return saldo
    }

    fun Int.convert(): String {
        val str = this.toString()
        val regex = "(\\d)(?=(\\d{3})+\$)".toRegex()
        return str.replace(regex, "\$1.")
    }

    override fun onEditTransaction(transactionId: Int) {
        // Handle the edit transaction action here
        // For example, navigate to the EditPage fragment passing the transactionId
        val fragment = EditPage()

        // Pass transactionId to EditPage fragment
        val bundle = Bundle().apply {
            putInt("transactionId", transactionId)
        }
        fragment.arguments = bundle

        // Perform fragment transaction
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }
}