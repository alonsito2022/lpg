package com.example.driver.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.model.Driver
import com.example.driver.R
import com.squareup.picasso.Picasso

class StockRegularAdapter(val dataSet: MutableList<Driver.Vehicle.StockRegular> = mutableListOf()): RecyclerView.Adapter<StockRegularAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val cardProductTitle: TextView = itemView.findViewById(R.id.cardProductTitle)
        val cardCircle: ImageView = itemView.findViewById(R.id.cardCircle)
        val cardVoidStock: TextView = itemView.findViewById(R.id.cardVoidStock)
        val cardFilledStock: TextView = itemView.findViewById(R.id.cardFilledStock)
        val view = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock_regular_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]

        holder.cardProductTitle.text = item.productName
        holder.cardVoidStock.text = item.voidStock.toString()
        holder.cardFilledStock.text = item.filledStock.toString()

        Picasso.get().load(item.productPath).into(holder.cardCircle)

        holder.view.setOnClickListener{
            Toast.makeText(holder.view.context, "Item -> " + item.productName, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}