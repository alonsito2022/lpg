package com.example.driver.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.model.Debt

class ProductOwedAdapter(val dataSet: ArrayList<Debt>): RecyclerView.Adapter<ProductOwedAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val cardProductOwedCreatedAt: TextView = itemView.findViewById(R.id.cardProductOwedCreatedAt)
        val cardProductOwedProductName: TextView = itemView.findViewById(R.id.cardProductOwedProductName)
        val cardProductOwedUnitName: TextView = itemView.findViewById(R.id.cardProductOwedUnitName)
        val cardProductOwedQuantity: TextView = itemView.findViewById(R.id.cardProductOwedQuantity)
        val cardProductOwedAmount: TextView = itemView.findViewById(R.id.cardProductOwedAmount)
        val view = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_owed_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
//        holder.cardProductOwedCreatedAt.text = item.createdAt
//        holder.cardProductOwedProductName.text = item.productName
//        holder.cardProductOwedUnitName.text = item.unitName
//        holder.cardProductOwedQuantity.text = item.quantity.toString()
//        holder.cardProductOwedAmount.text = item.amount.toString()
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

}