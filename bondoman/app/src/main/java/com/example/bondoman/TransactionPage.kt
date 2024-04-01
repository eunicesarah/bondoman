package com.example.bondoman

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.room.Transaction
import com.example.bondoman.room.TransactionDB
import com.example.bondoman.room.TransactionRepository
import com.example.bondoman.room.TransactionRepositoryImplement
import com.example.bondoman.utils.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Base64
import java.util.Locale


class TransactionPage : Fragment(R.layout.fragment_transaction_page), TransactionAdapter.EditTransactionListener {
    val db by lazy { TransactionDB(requireContext()) }
    private lateinit var transactionRepository: TransactionRepository


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        transactionRepository = TransactionRepositoryImplement(db.transactionDao(), requireContext())
        transactionRepository.setNIM()


        recyclerView.layoutParams.height = recyclerViewHeight
        recyclerView.requestLayout()

        lifecycleScope.launch {
                val transactions = withContext(Dispatchers.IO) {
                    transactionRepository.getAllTransactions()
                }

                val balance = saldo(transactions)
                val text: String = if (balance >= 0) {
                    "Rp${balance.convert()}"
                } else {
                    "-Rp${balance.convert().substring(1)}"
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
    fun saldo(list : List<Transaction>) : Double {
        var saldo: Double = 0.0
        for (i in list) {
            if (i.field_kategori == "Pemasukan") {
                saldo += i.field_nominal.toDouble()
            } else {
                saldo -= i.field_nominal.toDouble()
            }
        }
        return saldo
    }

    fun Double.convert(): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        val formatter = DecimalFormat("#,###.###", symbols)
        return formatter.format(this)
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