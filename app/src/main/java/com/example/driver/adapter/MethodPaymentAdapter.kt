package com.example.driver.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R

class MethodPaymentAdapter(val dataSet: MutableMap<String, Double>): RecyclerView.Adapter<MethodPaymentAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textViewPaymentMethodName: TextView = itemView.findViewById(R.id.textViewPaymentMethodName)
        val textViewPaymentMethodPrice: TextView = itemView.findViewById(R.id.textViewPaymentMethodPrice)
        val view = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_method_payment_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // val item = dataSet[position]
        val item = dataSet[dataSet.keys.elementAt(position)]

        when(dataSet.keys.elementAt(position).toString().uppercase()){
            "CASH" -> holder.textViewPaymentMethodName.text = "EFECTIVO"
            else -> holder.textViewPaymentMethodName.text = dataSet.keys.elementAt(position).toString().uppercase()
        }

        holder.textViewPaymentMethodPrice.text = item.toString()

    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}