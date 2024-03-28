package com.example.bondoman

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.room.Transaction
import com.example.bondoman.room.TransactionDB
import com.example.bondoman.utils.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsPage.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsPage : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val db by lazy { TransactionDB(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsPage.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsPage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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
            sendMail()
        }
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
            export(".xls")
            dialog.dismiss()
        }

        xlsxButton.setOnClickListener {
            export(".xlsx")
            dialog.dismiss()
        }
    }

    fun sendMail() {
        TODO("Not yet implemented")
    }

    fun export(format: String){
        lifecycleScope.launch {
            val transactions = withContext(Dispatchers.IO) {
                db.transactionDao().getAllTransactions()
            }
            saveTransactionsToExcel(transactions, requireContext(), format)

        }
    }

    fun saveTransactionsToExcel(transactions: List<Transaction>, context: Context, format: String) {
        Toast.makeText(context, "Menyimpan data...", Toast.LENGTH_SHORT).show()
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

        Toast.makeText(context, "Data berhasil disimpan di ${file.absolutePath}", Toast.LENGTH_LONG).show()
        Log.d("SettingsPage", "Data berhasil disimpan di ${file.absolutePath}")

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
}