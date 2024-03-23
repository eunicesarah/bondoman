package com.example.bondoman

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.bondoman.room.TransactionDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EditPage : Fragment(R.layout.fragment_edit_page)  {
    private lateinit var update_button: Button
    private lateinit var delete_button: Button
    private lateinit var field_judul: EditText
    private lateinit var field_nominal: EditText
    private lateinit var field_lokasi: EditText
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

        // Retrieve transaction ID from arguments
        idTrans = requireArguments().getInt("transactionId", 0)
        Log.d("EditPage", "idTrans: $idTrans")

        // Fetch transaction details from the database
        CoroutineScope(Dispatchers.IO).launch {
            val transaction = db.transactionDao().findIdTrans(idTrans)
            if (transaction != null) {
                // Update UI elements with transaction data
                field_judul.setText(transaction.field_judul)
                field_nominal.setText(transaction.field_nominal)
                field_lokasi.setText(transaction.field_lokasi)
            } else {
                // Handle case where transaction is not found
                Log.d("EditPage", "Transaction not found for ID: $idTrans")
            }
        }

        setUpListener()
    }


    fun setUpListener(){
        update_button.setOnClickListener {
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
                } else {
                    // Handle case where transaction is not found
                    Log.d("EditPage", "Transaction not found for id: $idTrans")
                }
            }
        }


        delete_button.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {
                val transaction = db.transactionDao().findIdTrans(idTrans)
                if (transaction != null) {
                    db.transactionDao().deleteTransaction(transaction)
                } else {
                    // Handle case where transaction is null
                }
            }
        }
    }



}