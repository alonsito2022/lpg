package com.example.driver.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.model.Vehicle
import com.squareup.picasso.Picasso


//class ItemAdapter(val dataSet: Array<String>): RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
class VehicleStockRegularFilledAdapter(val dataSet: Map<String, Vehicle.Stock.StockProduct>): RecyclerView.Adapter<VehicleStockRegularFilledAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val cardSubtitle: TextView = itemView.findViewById(R.id.cardSubtitle)
        val cardCircle: ImageView = itemView.findViewById(R.id.cardCircle)
        val cardQuantity: TextView = itemView.findViewById(R.id.cardQuantity)
        val cardTitle: TextView = itemView.findViewById(R.id.cardTitle)
        val view = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock_regular_filled_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[dataSet.keys.elementAt(position)]

        holder.cardSubtitle.text = item!!.productKey
        holder.cardQuantity.text = item.quantity.toString()
        holder.cardTitle.text = item.productName

        Picasso.get().load(item.productPath).into(holder.cardCircle)

        holder.view.setOnClickListener{
            Toast.makeText(holder.view.context, "Item clicked -> " + dataSet.keys.elementAt(position), Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

}