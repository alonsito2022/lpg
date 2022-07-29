package com.example.driver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.model.Driver
import com.squareup.picasso.Picasso

class SaleProductAdapter (var dataSet: MutableList<Driver.Vehicle.StockRegular> = mutableListOf(), private val mListener: OnItemClickListener?): RecyclerView.Adapter<SaleProductAdapter.ViewHolder>(){
    private var context: Context? = null

    interface OnItemClickListener {
        fun onItemClick(model: Driver.Vehicle.StockRegular)
    }

    fun updateDataSet(filterDataSet: MutableList<Driver.Vehicle.StockRegular>){
        dataSet = filterDataSet
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val cardUnitName: TextView = itemView.findViewById(R.id.cardUnitName)
        val cardPath: ImageView = itemView.findViewById(R.id.cardPath)
        val cardPrice: TextView = itemView.findViewById(R.id.cardPrice)
        val cardStock: TextView = itemView.findViewById(R.id.cardStock)
        val cardProductName: TextView = itemView.findViewById(R.id.cardProductName)
        val view = itemView
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sale_product_view, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]

        Picasso.get().load(item.productPath).into(holder.cardPath)
        holder.cardProductName.text = item.productName
        holder.cardStock.text = "STOCK: ${item.filledStock}"

        if(item.filledStock == 0)
            holder.cardStock.setTextColor(ContextCompat.getColor(context!!, R.color.orange_700))


        when(item.returnability){
            "R", "PG", "PB" ->{
                holder.cardUnitName.text = item.tariff[2].unitName
                holder.cardPrice.text = "S/ ${item.tariff[2].price}"

            }
            "F", "PBG" ->{
                holder.cardUnitName.text = item.tariff[0].unitName
                holder.cardPrice.text = "S/ ${item.tariff[0].price}"
            }
        }

        holder.view.setOnClickListener {
            mListener!!.onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

}