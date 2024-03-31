package com.example.bondoman

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.bondoman.room.Transaction
import com.example.bondoman.room.TransactionDB
import com.example.bondoman.room.TransactionRepository
import com.example.bondoman.room.TransactionRepositoryImplement
import com.example.bondoman.utils.AuthManager
import com.example.bondoman.utils.RandomizeTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Base64
import kotlin.random.Random

class SettingsPage : Fragment() {
    val db by lazy { TransactionDB(requireContext()) }
    private lateinit var transactionRepository: TransactionRepository
    private val randomizeReceiver = RandomizeTransaction()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAction()
    }
    fun initAction(){
        val logout_button = view?.findViewById<Button>(R.id.logout_button)
        logout_button?.setOnClickListener {
            logout()
        }
        val save_button = view?.findViewById<Button>(R.id.save_button)
        save_button?.setOnClickListener {
            showExtDialog()
        }
        val send_button = view?.findViewById<Button>(R.id.send_button)
        send_button?.setOnClickListener {
            export(".xlsx", true)
        }

        val randomize_button = view?.findViewById<Button>(R.id.randomize_button)
        randomize_button?.setOnClickListener {
            randomizeTransaction()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(randomizeReceiver, IntentFilter("com.example.bondoman.RANDOMIZE_TRANSACTION"))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(randomizeReceiver)
    }


    private fun showExtDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_choose_extentions, null)

        val xlsButton = dialogView.findViewById<Button>(R.id.xlsButton)
        val xlsxButton = dialogView.findViewById<Button>(R.id.xlsxButton)

        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        xlsButton.setOnClickListener {
            export(".xls", false)
            dialog.dismiss()
        }

        xlsxButton.setOnClickListener {
            export(".xlsx", false)
            dialog.dismiss()
        }
    }

    private fun sendMail(fileUri: Uri) {
        val token = AuthManager.getToken(requireContext()).toString().split('.')
        Log.d("Send Mail", "token $token")
        if (token.size != 3) {
            Log.e("Send Mail", "Invalid token format")
            return
        }
        val payload = Base64.getUrlDecoder().decode(token[1])
        val payloadJson = JSONObject(String(payload, Charsets.UTF_8))
        val nim = payloadJson.optString("nim")
        Log.d("Send Mail", "nim $nim")
        val email = "${nim}@std.stei.itb.ac.id"
        Log.d("Send Mail", "email $email")

        val subject = "Berikut merupakan data transaksi terbaru hingga saat ini."

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, "Bondoman: Data Transaksi")
            putExtra(Intent.EXTRA_TEXT, subject)
            putExtra(Intent.EXTRA_STREAM, fileUri)
        }

        Log.d("Send Mail", "Intent: $intent")

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun export(format: String, send: Boolean){
        transactionRepository = TransactionRepositoryImplement(db.transactionDao(), requireContext())
        transactionRepository.setNIM()
        lifecycleScope.launch {
            val transactions = withContext(Dispatchers.IO) {
                transactionRepository.getAllTransactions()
            }
            saveTransactionsToExcel(transactions, requireContext(), format, send)

        }
    }

    private fun saveTransactionsToExcel(transactions: List<Transaction>, context: Context, format: String, send: Boolean) {
        Toast.makeText(context, "Memproses data transaksi...", Toast.LENGTH_SHORT).show()
        val workbook = XSSFWorkbook()
        val sheets = workbook.createSheet("Transactions")

        //Header
        val headerRow = sheets.createRow(0)
        headerRow.createCell(0).setCellValue("ID")
        headerRow.createCell(1).setCellValue("Added At")
        headerRow.createCell(2).setCellValue("Judul")
        headerRow.createCell(3).setCellValue("Nominal")
        headerRow.createCell(4).setCellValue("Kategori")
        headerRow.createCell(5).setCellValue("Lokasi")

        //Data rows
        var rowNum = 1
        for (transaction in transactions) {
            val row = sheets.createRow(rowNum++)
            row.createCell(0).setCellValue(transaction.id.toDouble())
            row.createCell(1).setCellValue(transaction.createdAt)
            row.createCell(2).setCellValue(transaction.field_judul)
            row.createCell(3).setCellValue(transaction.field_nominal)
            row.createCell(4).setCellValue(transaction.field_kategori)
            row.createCell(5).setCellValue(transaction.field_lokasi)
        }

        // Auto-size columns
        for (i in 0 until 6) {
            sheets.setColumnWidth(i, (sheets.getColumnWidth(i) + 1000).coerceAtMost(255 * 256))
        }

        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val documentsFolder = File(documentsDir, "Bondoman-Transaction")
        if (!documentsFolder.exists()) {
            documentsFolder.mkdirs()
        }

        var title = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-SSS").format(System.currentTimeMillis())
        title = "Transaksi $title$format"

        val file = File(documentsFolder, title)
        val fileOutputStream = FileOutputStream(file)
        workbook.write(fileOutputStream)
        workbook.close()
        fileOutputStream.close()

        if (send) {
            val fileUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
            Log.d("Send Mail", "File URI: $fileUri")
            sendMail(fileUri)
        } else {
            val path = "Documents/Bondoman-Transaction/$title"
            Toast.makeText(context, "Tersimpan di $path", Toast.LENGTH_LONG).show()
            Log.d("Save File", "Data berhasil disimpan di ${file.absolutePath}")
        }

    }

    fun logout() {
        lifecycleScope.launch {
            context?.let { AuthManager.deleteToken(it) }

            val intent = Intent(context, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    fun randomizeTransaction(){
        val randomPrice = Random.nextInt(10000)

        val intent = Intent("com.example.bondoman.RANDOMIZE_TRANSACTION").apply {
            putExtra("randomPrice", randomPrice)
        }

        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        replaceFragment(AddTransactionPage(), HeaderTransaction())
    }

    fun replaceFragment(fragment: Fragment, headerFragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()

        val headerTransaction = parentFragmentManager.beginTransaction()
        headerTransaction.replace(R.id.header_layout, headerFragment)
        headerTransaction.commit()
    }


}