package com.example.bondoman

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bondoman.databinding.FragmentTwibbonPageBinding

class TwibbonPage : Fragment() {
    
    private var _binding: FragmentTwibbonPageBinding? = null
    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null
    private var cameraOn = false
    private var currentCameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTwibbonPageBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.twibbonText.visibility = View.GONE
        if (allPermissionsGranted()) {
            startCamera()
            cameraOn = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, 10
            )
        }

        binding.flipCamera.setOnClickListener {
            currentCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCamera()
        }


        binding.backButton.setOnClickListener {
            val scanPage = ScanPage()
            val headerTwibbon = HeaderScan()
            val mainActivity = activity as? MainActivity
            if (mainActivity != null) {
                mainActivity.replaceFragment(scanPage, headerTwibbon)
            } else {
                Log.e("ScanPage", "Activity is null. Cannot cast to MainActivity.")
            }
        }

        binding.shutterButton.setOnClickListener {
            if (cameraOn){
                takePhoto()
            } else {
                cameraOn = true
                binding.hasilFoto.visibility = View.GONE
                binding.previewView.visibility = View.VISIBLE
                binding.twibbonText.visibility = View.GONE
                binding.flipCamera.visibility = View.VISIBLE
                Log.d("Camera", "visibility a: ${binding.hasilFoto.visibility}")
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == 10) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Log.d("Permission", "Permissions not granted by the user.")
                Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exception: ImageCaptureException) {
                    Log.e("Camera", "Photo capture failed: ${exception.message}", exception)
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.hasilFoto.setImageBitmap(bitmap)
                    binding.hasilFoto.visibility = View.VISIBLE
                    binding.previewView.visibility = View.GONE
                    binding.twibbonText.visibility = View.VISIBLE
                    binding.flipCamera.visibility = View.GONE
                    Log.d("Camera", "visibility b: ${binding.hasilFoto.visibility}")

                    if(currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA){
                        val rotationMat = Matrix().apply { postRotate(270f) }
                        val flipMat = Matrix().apply { postScale(-1f, 1f) }
                        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotationMat, true)
                        val flippedBitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.width, rotatedBitmap.height, flipMat, true)
                        binding.hasilFoto.setImageBitmap(flippedBitmap)
                    } else {
                        val rotationMat = Matrix().apply { postRotate(90f) }
                        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotationMat, true)
                        binding.hasilFoto.setImageBitmap(rotatedBitmap)
                    }

                    image.close()
                    cameraOn = false
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

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, currentCameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted()= REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
    }
}