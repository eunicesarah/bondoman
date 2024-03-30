package com.example.bondoman

import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.room.TransactionDB
import com.example.bondoman.room.TransactionRepository
import com.example.bondoman.room.TransactionRepositoryImplement
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GraphPage : Fragment(R.layout.fragment_graph_page) {
    val db by lazy { TransactionDB(requireContext()) }

    lateinit var PieChart: GraphPage
    private lateinit var transactionRepository: TransactionRepository


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_graph_page, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pieChart = view.findViewById<PieChart>(R.id.pie_chart)
        val progressBarTumbas = view.findViewById<ProgressBar>(R.id.progress_tumbas)
        val progressBarUpah = view.findViewById<ProgressBar>(R.id.progress_upah)

        pieChart.setUsePercentValues(true)
        pieChart.legend.isEnabled = false

        progressBarTumbas.max = 100
        transactionRepository = TransactionRepositoryImplement(db.transactionDao(), requireContext())
        transactionRepository.setNIM()

        lifecycleScope.launch{
            val transaction = withContext(Dispatchers.IO){
                transactionRepository.getAllTransactions()
            }
            val entries = ArrayList<PieEntry>()
            transaction.forEach{
                transaction ->
                entries.add(PieEntry(transaction.field_nominal.toFloat(), transaction.field_kategori))
            }

            val dataSet = PieDataSet(entries, "Transaction Categories")
            val categoryColors = mapOf(
                "Pengeluaran" to ContextCompat.getColor(requireContext(), R.color.tumbas),
                "Pemasukan" to ContextCompat.getColor(requireContext(), R.color.upah)
            )
            dataSet.colors = entries.map { entry->
                categoryColors[entry.label] ?: R.color.white
            }
            dataSet.sliceSpace = 3f
            dataSet.selectionShift = 5f

            val data = PieData(dataSet)
            data.setValueTextSize(10f)
            data.setValueTextColor(R.color.white)

            pieChart.data = data
            pieChart.animateXY(1000, 1000)

            pieChart.invalidate()

            val totalPengeluaran = transaction.filter { it.field_kategori == "Pengeluaran" }
                .sumByDouble { it.field_nominal.toDouble() }
            val totalPemasukan = transaction.filter { it.field_kategori == "Pemasukan" }
                .sumByDouble { it.field_nominal.toDouble() }
            val totalTransactions = transaction.sumByDouble { it.field_nominal.toDouble() }
            val pemasukanPrecentage = (totalPemasukan/totalTransactions * 100).toInt()
            val pengeluaranPrecentage = (totalPengeluaran/totalTransactions * 100).toInt()

            ObjectAnimator.ofInt(progressBarTumbas, "progress", pengeluaranPrecentage).apply {
                duration = 1000
                start()
            }

            ObjectAnimator.ofInt(progressBarUpah, "progress", pemasukanPrecentage).apply {
                duration = 1000
                start()
            }


        }

    }



}