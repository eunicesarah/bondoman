package com.example.bondoman

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
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
import androidx.core.content.ContextCompat
import com.example.bondoman.room.TransactionDB
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


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
    private lateinit var curr_loc: Button

    //    private val db by lazy { TransactionDB(requireContext()) }
private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val permissionId = 123
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
        curr_loc = view.findViewById(R.id.curr_loc)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())


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
            val judul = field_judul.text.toString().trim()
            val nominal = field_nominal.text.toString().trim()
            val selectedRadioButtonId = field_kategori.checkedRadioButtonId
            val lokasi = field_lokasi.text.toString().trim()

            if (judul.isEmpty() || nominal.isEmpty() || selectedRadioButtonId == -1 || lokasi.isEmpty()) {
                // At least one field is empty
                Toast.makeText(requireContext(), "Isi kabeh formulir", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
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
        }


        delete_button.setOnClickListener {
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
            }

        back_edit.setOnClickListener {
            navigateBackToTransactionPage()
        }
        curr_loc.setOnClickListener {
            getLocation()
            Log.d("Button", "udah di klik")
        }



    }
    private fun navigateBackToTransactionPage() {
        val transactionPageFragment = TransactionPage()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, transactionPageFragment)
            .commit()    }


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
                                field_lokasi.setText("${address.latitude.toString()}, ${address.longitude.toString()}")
                                Log.d("Address", "Latitdue:  ${field_lokasi}")

                            }
                        } else {
//                            Log.d("Address", "gapunya permission ok")
                            Toast.makeText(requireContext(), "Address not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Log.d("Address", "chayon ok")
                Toast.makeText(requireContext(), "Mangga nguripake lokasi sampeyan", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            Log.d("Address", "clohk")
            field_lokasi.setText("-6.927530659352057, 107.76998310218032")
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



}