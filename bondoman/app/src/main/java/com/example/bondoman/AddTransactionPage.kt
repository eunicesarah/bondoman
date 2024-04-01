package com.example.bondoman

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.provider.Settings
import android.location.Location
import androidx.core.content.ContextCompat
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.bondoman.room.Transaction
import com.example.bondoman.room.TransactionDB
import com.example.bondoman.utils.AuthManager
import com.example.bondoman.utils.RandomizeTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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
    private lateinit var curr_loc: Button
    val db by lazy { TransactionDB(requireContext()) }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val permissionId = 123


    private val randomizeReceiver = RandomizeTransaction()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        add_button = view.findViewById(R.id.add_button)
        field_judul = view.findViewById(R.id.field_judul) as EditText
        field_nominal = view.findViewById(R.id.field_nominal) as EditText
        field_kategori = view.findViewById(R.id.field_kategori) as RadioGroup
        field_lokasi = view.findViewById(R.id.field_lokasi) as EditText
        back_add = view.findViewById(R.id.back_add)
        curr_loc = view.findViewById(R.id.curr_loc)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        requestLocationUpdates()

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

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            Log.d("Address", "check permission ok")
            if (isLocationEnabled()) {
                Log.d("Address", "location ok")
                fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
//                    Log.d("Address", "Task: ${task.result}")
                    if (location != null) {
//                        Log.d("Address", "check location not null ok")
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val list: List<Address>? =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (list != null && list.isNotEmpty()) {
//                            Log.d("Address", "check not empty ok")
                            val address = list[0]
                            view?.apply {
                                field_lokasi.setText(address.getAddressLine(0))
//                                Log.d("Address", "Latitdue:  ${address.latitude}")
                                Log.d("Address", "Lokasi:  ${address.getAddressLine(0)}")
                            }
                        } else {
//                            Log.d("Address", "gapunya permission ok")
                            Toast.makeText(requireContext(), "Address not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Log.d("Address", "chayon ok")
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            Log.d("Address", "clohk")
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000 // Update interval in milliseconds
            fastestInterval = 5000 // Fastest update interval in milliseconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // Handle location updates here
                    Log.d("Location", "New Location: ${location.latitude}, ${location.longitude}")
                }
            }
        }, null)
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
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

        curr_loc.setOnClickListener {
            getLocation()
            Log.d("Button", "udah di klik")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Ensure the receiver is unregistered when the fragment is destroyed
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(randomizeReceiver)
    }

}
