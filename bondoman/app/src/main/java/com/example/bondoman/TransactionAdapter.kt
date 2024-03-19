package com.example.bondoman

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.room.Transaction

class TransactionAdapter(
    private val list : List<Transaction>
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

        val nominal = transaction.field_nominal.toInt()

        holder.description.text = transaction.field_judul
        holder.price.text = nominal.convert()
        holder.date.text = transaction.createdAt
        holder.location.text = transaction.field_lokasi
        if (transaction.field_kategori == "Pemasukan") holder.icon.setImageResource(R.drawable.income_logo)
        else holder.icon.setImageResource(R.drawable.expenses_logo)

        if (transaction.field_kategori == "Pemasukan") holder.category.text = "UPAH"
        else holder.category.text = "TUMBAS"

        holder.button.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${transaction.field_lokasi}"))
            holder.itemView.context.startActivity(intent)
            Log.d("TransactionAdapter", "location: ${transaction.field_lokasi}")
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
    }

    fun Int.convert(): String {
        val str = this.toString()
        val regex = "(\\d)(?=(\\d{3})+\$)".toRegex()
        return str.replace(regex, "\$1.")
    }

}