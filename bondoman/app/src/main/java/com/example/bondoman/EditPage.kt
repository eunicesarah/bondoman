package com.example.bondoman

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.bondoman.room.TransactionDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditPage : Fragment(R.layout.fragment_edit_page)  {
    private lateinit var update_button: Button
    private lateinit var delete_button: Button
    private lateinit var field_judul: EditText
    private lateinit var field_nominal: EditText
    private lateinit var field_lokasi: EditText
    private lateinit var field_kategori: RadioGroup
    private lateinit var button_pemasukan: RadioButton
    private lateinit var button_pengeluaran: RadioButton
    private lateinit var back_edit: Button
//    private val db by lazy { TransactionDB(requireContext()) }
private lateinit var db: TransactionDB
    private var idTrans = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = TransactionDB(requireContext())
        Log.d("TransactionAdapter", "masuk ke edit")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_page, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        update_button = view.findViewById(R.id.update_button)
        delete_button = view.findViewById(R.id.delete_button)
        field_judul = view.findViewById(R.id.field_judul) as EditText
        field_nominal = view.findViewById(R.id.field_nominal) as EditText
        field_lokasi = view.findViewById(R.id.field_lokasi) as EditText
        button_pemasukan = view.findViewById(R.id.button_pemasukan)
        button_pengeluaran = view.findViewById(R.id.button_pengeluaran)
        field_kategori = view.findViewById(R.id.field_kategori)
        back_edit = view.findViewById(R.id.back_edit)

        // Retrieve transaction ID from arguments
        idTrans = requireArguments().getInt("transactionId", 0)
        Log.d("EditPage", "idTrans: $idTrans")

        // Fetch transaction details from the database
        CoroutineScope(Dispatchers.IO).launch {
            val transaction = db.transactionDao().findIdTrans(idTrans)
            transaction?.let {
                withContext(Dispatchers.Main) {
                    field_judul.setText(transaction.field_judul)
                    field_nominal.setText(transaction.field_nominal)
                    field_lokasi.setText(transaction.field_lokasi)

                    if (transaction.field_kategori == "Pengeluaran") {
                        button_pengeluaran.isChecked = true
                    } else {
                        button_pemasukan.isChecked = true
                    }

                    field_kategori.isEnabled = false
                }

            }
        }

        setUpListener()
    }


    fun setUpListener(){
        update_button.setOnClickListener {
            if (isNetworkAvailable()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val transaction = db.transactionDao().findIdTrans(idTrans)
                    if (transaction != null) {
                        // Update only the modified fields
                        transaction.field_judul = field_judul.text.toString()
                        // Update nominal only if the field is not empty
                        if (field_nominal.text.toString().isNotEmpty()) {
                            transaction.field_nominal = field_nominal.text.toString()
                        }
                        transaction.field_lokasi = field_lokasi.text.toString()

                        db.transactionDao().updateTransaction(transaction)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Tumbas wis diganti", Toast.LENGTH_SHORT).show()
                            navigateBackToTransactionPage()
                        }
                    } else {
                        Log.d("EditPage", "Transaction not found for id: $idTrans")
                    }
                }
            } else {
                Log.d("EditPage", "Network not available. Cannot update transaction.")
                Toast.makeText(requireContext(), "Prikso jaringan internet sampeyan", Toast.LENGTH_SHORT).show()
            }
        }


        delete_button.setOnClickListener {
            if (isNetworkAvailable()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val transaction = db.transactionDao().findIdTrans(idTrans)
                    if (transaction != null) {
                        db.transactionDao().deleteTransaction(transaction)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Tumbas wis dibusak", Toast.LENGTH_SHORT).show()
                            navigateBackToTransactionPage()
                        }
                    } else {
                        Log.d("EditPage", "Transaction not found for id: $idTrans")
                    }
                }
            } else {
                Log.d("EditPage", "Network not available. Cannot delete transaction.")
                Toast.makeText(requireContext(), "Prikso jaringan internet sampeyan", Toast.LENGTH_SHORT).show()
            }
        }

        back_edit.setOnClickListener {
            navigateBackToTransactionPage()
        }


    }
    private fun navigateBackToTransactionPage() {
        val transactionPageFragment = TransactionPage()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, transactionPageFragment)
            .commit()    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val networkInfo = connectivityManager.getNetworkCapabilities(networkCapabilities)
        return networkInfo != null && networkInfo.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }



}