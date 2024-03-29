package com.example.bondoman

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.room.Transaction
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class TransactionAdapter(
    private val list : List<Transaction>,
    private val editTransactionListener: EditTransactionListener
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TransactionAdapter.ViewHolder, position: Int) {
        val transaction = list[position]

        val nominal = transaction.field_nominal.toDouble()

        holder.description.text = transaction.field_judul
        var text: String
        if (nominal >= 0){
            text = "Rp${nominal.convert()}"
        } else {
            text = "-Rp${nominal.convert().substring(1)}"
        }
        holder.price.text = text
        holder.date.text = transaction.createdAt
        holder.location.text = transaction.field_lokasi
        if (transaction.field_kategori == "Pemasukan") holder.icon.setImageResource(R.drawable.income_logo)
        else holder.icon.setImageResource(R.drawable.expenses_logo)

        if (transaction.field_kategori == "Pemasukan") holder.category.text = "UPAH"
        else holder.category.text = "TUMBAS"

        if (transaction.field_lokasi == "") {
            holder.locLayout.visibility = View.GONE
        } else {
            holder.locLayout.visibility = View.VISIBLE
            holder.location.text = transaction.field_lokasi
        }

        holder.button.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${transaction.field_lokasi}"))
            holder.itemView.context.startActivity(intent)
            Log.d("TransactionAdapter", "location: ${transaction.field_lokasi}")
        }

        holder.editButton.setOnClickListener {
            editTransactionListener.onEditTransaction(transaction.id)

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder( view: View) : RecyclerView.ViewHolder( view ) {
        val icon = view.findViewById<ImageView>(R.id.image_transaction)
        val category = view.findViewById<TextView>(R.id.cat_transaction)
        val description = view.findViewById<TextView>(R.id.description_transaction)
        val price = view.findViewById<TextView>(R.id.price_transaction)
        val date = view.findViewById<TextView>(R.id.date_transaction)
        val location = view.findViewById<TextView>(R.id.location_transaction)
        val button = view.findViewById<LinearLayout>(R.id.linear_layout_transaction)
        val locLayout = view.findViewById<LinearLayout>(R.id.linear_layout_transaction)
        val editButton = view.findViewById<Button>(R.id.edit_button)
    }

    fun Double.convert(): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        val formatter = DecimalFormat("#,###.###", symbols)
        return formatter.format(this)
    }
    interface EditTransactionListener {
        fun onEditTransaction(transactionId: Int)
    }


}