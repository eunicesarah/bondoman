package com.example.bondoman

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.bondoman.retrofit.Retrofit
import com.example.bondoman.retrofit.endpoint.EndpointCheckExpiry
import com.example.bondoman.retrofit.request.CheckExpiryRequest
import com.example.bondoman.room.Transaction
import com.example.bondoman.room.TransactionDB
import com.example.bondoman.utils.AuthManager
import com.example.bondoman.utils.RandomizeTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okio.ByteString.decodeBase64
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddTransactionPage.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddTransactionPage : Fragment(R.layout.fragment_add_transaction) {

    private lateinit var add_button: Button
    private lateinit var field_judul: EditText
    private lateinit var field_nominal: EditText
    private lateinit var field_kategori: RadioGroup
    private lateinit var field_lokasi: EditText
    private lateinit var back_add: Button
    val db by lazy { TransactionDB(requireContext()) }

    private val randomizeReceiver = RandomizeTransaction()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        add_button = view.findViewById(R.id.add_button)
        field_judul = view.findViewById(R.id.field_judul) as EditText
        field_nominal = view.findViewById(R.id.field_nominal) as EditText
        field_kategori = view.findViewById(R.id.field_kategori) as RadioGroup
        field_lokasi = view.findViewById(R.id.field_lokasi) as EditText
        back_add = view.findViewById(R.id.back_add)

        if (RandomizeTransaction.shouldRandomizePrice) field_nominal.setText(RandomizeTransaction.randomPrice.toString())
        setUpListener()
        RandomizeTransaction.shouldRandomizePrice = false
    }
//    fun showToast(message: String) {
//        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(randomizeReceiver, IntentFilter("com.example.bondoman.RANDOMIZE_TRANSACTION"))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(randomizeReceiver)
    }



    private fun setUpListener() {
        add_button.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val token = AuthManager.getToken(requireContext()).toString().split('.')
                    Log.d("Main Activity", "token ${token}")

                    if (token.size != 3) {
                        Log.e("Main Activity", "Invalid token format")
                        return@launch
                    }

                    val payload = Base64.getUrlDecoder().decode(token[1])
                    Log.d("Main Activity", "payload ${String(payload, StandardCharsets.UTF_8)}")

                    val payloadJson = JSONObject(String(payload, StandardCharsets.UTF_8))
                    Log.d("Main Activity", "payloadJson ${payloadJson}")

                    db.transactionDao().addTransaction(
                        Transaction(
                            0,
                            payloadJson.optString("nim"),
                            field_judul.text.toString(),
                            field_nominal.text.toString(),
                            (view?.findViewById(field_kategori.checkedRadioButtonId) as RadioButton).text.toString(),
                            field_lokasi.text.toString(),
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        )
                    )

                    val fragment = TransactionPage()
                    val transaction : FragmentTransaction = requireFragmentManager().beginTransaction()
                    transaction.replace(R.id.frame_layout, fragment).commit()

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        back_add.setOnClickListener {
            val transactionPageFragment = TransactionPage()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, transactionPageFragment)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Ensure the receiver is unregistered when the fragment is destroyed
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(randomizeReceiver)
    }

}
