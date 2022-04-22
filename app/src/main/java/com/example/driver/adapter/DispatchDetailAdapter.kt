package com.example.driver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.model.Dispatch

class DispatchDetailAdapter(val dataSet: MutableList<Dispatch.DispatchDetail>): RecyclerView.Adapter<DispatchDetailAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textViewDispatchDetailProductName: TextView = itemView.findViewById(R.id.textViewDispatchDetailProductName)
        val textViewModality: TextView = itemView.findViewById(R.id.textViewModality)
        val textViewDispatchDetailPrice: TextView = itemView.findViewById(R.id.textViewDispatchDetailPrice)
        val textViewDispatchDetailQuantity: TextView = itemView.findViewById(R.id.textViewDispatchDetailQuantity)
        val textViewDispatchDetailSubtotal: TextView = itemView.findViewById(R.id.textViewDispatchDetailSubtotal)
        val view = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dispatch_detail_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = dataSet[position]
        // val item = dataSet[dataSet.keys.elementAt(position)]

        holder.textViewDispatchDetailProductName.text = item.productName
        holder.textViewModality.text = item.modality
        holder.textViewDispatchDetailPrice.text = item.price.toString()
        holder.textViewDispatchDetailQuantity.text = item.quantity.toString()
        holder.textViewDispatchDetailSubtotal.text = item.subtotal.toString()
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}
