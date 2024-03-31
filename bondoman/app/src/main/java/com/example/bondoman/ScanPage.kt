package com.example.bondoman

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.databinding.FragmentScanPageBinding
import com.example.bondoman.retrofit.Retrofit
import com.example.bondoman.retrofit.data.Item
import com.example.bondoman.retrofit.endpoint.EndpointScan
import com.example.bondoman.room.Transaction
import com.example.bondoman.room.TransactionDB
import com.example.bondoman.utils.AuthManager
import com.example.bondoman.utils.AuthManager.getToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Locale

class ScanPage : Fragment() {

    private var _binding: FragmentScanPageBinding? = null
    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null
    private val PICK_IMAGE_REQUEST = 2

    val db by lazy { TransactionDB(requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, 10
            )
        }

        binding.shutterButton.setOnClickListener {
            takePhoto()
        }

        binding.selectButton.setOnClickListener {
            selectPhoto()
        }

    }

    private fun selectPhoto() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Toast.makeText(requireContext(), "Lagi diamot...", Toast.LENGTH_SHORT).show()

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            Log.d("Scan - select photo", "Selected Image URI: $selectedImageUri")
            val imagePath = selectedImageUri?.let { getPathFromUri(it) }
            Log.d("Scan - select photo", "Image Path: $imagePath")
            if (imagePath != null) {
                sendImageToAPI(imagePath)
            }
        }
    }

    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireContext().contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            return it.getString(columnIndex)
        }
        return null
    }

    private fun takePhoto(): Unit {
        val imageCapture = imageCapture ?: return

        val title = SimpleDateFormat(FILENAME_FORMAT, Locale("in", "ID")).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, title)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Bondoman-Scan")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("Camera", "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Jepret foto gagal: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(requireContext(), "Lagi diamot...", Toast.LENGTH_SHORT).show()
                    val savedUri = output.savedUri ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    if (savedUri != MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                        val contentResolver = requireContext().contentResolver
                        val cursor = contentResolver.query(savedUri, null, null, null, null)
                        cursor?.use {
                            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                            if (it.moveToFirst()) {
                                val path = it.getString(dataIndex)
                                Log.d("Camera", "File path: $path")
                                sendImageToAPI(path)
                            }
                        }
                        cursor?.close()
                    } else {
                        Log.d("Camera", "Saved URI is not a content URI")
                    }
                }

            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == 10) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Log.d("Permission", "Permissions not granted by the user.")
                Toast.makeText(requireContext(), "Idin ora diwenehake dening pangguna.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendImageToAPI(pathname: String) {
        val token = getToken(requireContext())
        Log.d("Scan", "Token: $token")

        val retro = Retrofit.getInstance().create(EndpointScan::class.java)
        Log.d("Scan", "pathname: ${pathname}")


        val file = File(pathname)
        val requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file)
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)
        Log.d("Scan", "filePart: ${filePart}")

        lifecycleScope.launch {
            try {
                val response = retro.scan("Bearer $token", filePart)
                Log.d("Scan", "Response: ${response}")
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
//                        Log.d("Scan", "Success: ${data.items}")
//                        Log.d("Scan", "Success: ${data}")

                        val items = data.items.items
//                        val singleItem = listOf(items)
                        val cleanedItems = mutableListOf<Item>()
                        for (item in items) {
                            val name = item.name
//                            Log.d("Scan", "Name: $name")
                            val price = item.price
//                            Log.d("Scan", "Price: $price")
                            val qty = item.qty
//                            Log.d("Scan", "Qty: $qty")

                            val newItem = Item(name, qty, price)
                            cleanedItems.add(newItem)
                        }
                        addToDatabase(cleanedItems)
                }
                }
                else {
                    val errorBody = response.errorBody()?.string()
                    if (!errorBody.isNullOrEmpty()) {
                        val errorMessage = errorBody
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

            }
            catch (e: java.lang.Exception){
                Log.e("Scan", "Error: ${e.message}")
                Toast.makeText(requireContext(), "Ana sing salah: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun addToDatabase(data: MutableList<Item>) {
        CoroutineScope(lifecycleScope.coroutineContext).launch {
            val token = AuthManager.getToken(requireContext()).toString().split('.')
//            Log.d("Scan - to db", "token ${token}")

            if (token.size != 3) {
                Log.e("Scan - to db", "Invalid token format")
                return@launch
            }

            val payload = Base64.getUrlDecoder().decode(token[1])
//            Log.d("Scan - to db", "payload ${String(payload, StandardCharsets.UTF_8)}")

            val payloadJson = JSONObject(String(payload, StandardCharsets.UTF_8))
//            Log.d("Scan - to db", "payloadJson ${payloadJson}")

            var count = 0
            for (item in data) {
                db.transactionDao().addTransaction(
                    Transaction(
                        0,
                        payloadJson.optString("nim"),
                        item.name,
                        item.price.toString(),
                        "Pengeluaran",
                        "",
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
                    )
                )

//                Log.d("Scan - to db", "Added to db: ${item.name}, ${item.price}, ${item.qty}")
                count++
            }

            Toast.makeText(requireContext(), "Wis isa nambah ${count} tumbas", Toast.LENGTH_SHORT).show()

            val transactionPage = TransactionPage()
            val headerTransaction = HeaderTransaction()

            val mainActivity = activity as? MainActivity
            if (mainActivity != null) {
                mainActivity.binding.bottomNavigationView.selectedItemId = R.id.bill
                mainActivity.replaceFragment(transactionPage, headerTransaction)
            } else {
                Log.e("ScanPage", "Activity is null. Cannot cast to MainActivity.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        private const val FILENAME_FORMAT = "dd-MM-yyyy-HH-mm-ss-SSS"
    }
}