package com.example.driver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.model.Product
import com.squareup.picasso.Picasso
import androidx.core.content.ContextCompat

class ProductAdapter(val dataSet: ArrayList<Product>, private val mListener: OnItemClickListener?): RecyclerView.Adapter<ProductAdapter.ViewHolder>()  {
//class ProductAdapter(val dataSet: MutableMap<String, Product>): RecyclerView.Adapter<ProductAdapter.ViewHolder>()  {
    //var onItemClicked: ((Product) -> Unit)? = null

    interface OnItemClickListener {
        fun onItemClick(model: Product)
    }

    private var context: Context? = null
    //lateinit var onItemClicked: (Product) -> Unit

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val cardUnitName: TextView = itemView.findViewById(R.id.cardUnitName)
        val cardPath: ImageView = itemView.findViewById(R.id.cardPath)
        val cardPrice: TextView = itemView.findViewById(R.id.cardPrice)
        val cardStock: TextView = itemView.findViewById(R.id.cardStock)
        val cardProductName: TextView = itemView.findViewById(R.id.cardProductName)
        val view = itemView

        /*init {
            itemView.setOnClickListener {
                onItemClick.invoke(dataSet[adapterPosition])
            }
        }*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_view, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
        //clickListener = listener


        if(item.presentation[0].quantityFilled == 0){
            holder.cardStock.setTextColor(ContextCompat.getColor(context!!, R.color.orange_700))
        }

        when(item.modality){
            "Refill" ->{
                holder.cardUnitName.text = item.presentation[2].unit
                holder.cardPrice.text = """S/ ${item.presentation[2].price}"""
                holder.cardStock.text = """STOCK: ${item.presentation[2].quantityFilled}"""
            }
            "Full" ->{
                holder.cardUnitName.text = item.presentation[0].unit
                holder.cardPrice.text = """S/ ${item.presentation[0].price}"""
                holder.cardStock.text = """STOCK: ${item.presentation[0].quantityFilled}"""
            }
            "BorrowedG" ->{
                holder.cardUnitName.text = item.presentation[2].unit
                holder.cardPrice.text = """S/ ${item.presentation[2].price}"""
                holder.cardStock.text = """STOCK: ${item.presentation[2].quantityFilled}"""
            }
            "BorrowedB" ->{
                holder.cardUnitName.text = item.presentation[2].unit
                holder.cardPrice.text = """S/ ${item.presentation[2].price}"""
                holder.cardStock.text = """STOCK: ${item.presentation[2].quantityFilled}"""
            }
            "BorrowedBG" ->{
                holder.cardUnitName.text = item.presentation[0].unit
                holder.cardPrice.text = """S/ ${item.presentation[0].price}"""
                holder.cardStock.text = """STOCK: ${item.presentation[0].quantityFilled}"""
            }

        }
        //holder.cardPath.text = item.path
        Picasso.get().load(item.path).into(holder.cardPath)
        holder.cardProductName.text = item.name


        /*holder.cardUnitName.text = dataSet[dataSet.keys.elementAt(position)]!!.presentation[0].unit
        holder.cardPrice.text = dataSet[dataSet.keys.elementAt(position)]!!.presentation[0].price.toString()
        holder.cardProductName.text = dataSet[dataSet.keys.elementAt(position)]!!.name
        holder.cardPath.text = dataSet[dataSet.keys.elementAt(position)]!!.path*/

        /*holder.view.setOnClickListener{
            Toast.makeText(holder.view.context, "Item clicked -> " + item.presentation[2].unit, Toast.LENGTH_SHORT).show()
            holder.cardUnitName.text = item.presentation[2].unit
            holder.cardPrice.text = """S/ ${item.presentation[2].price}"""

        }*/
        /*holder.view.setOnClickListener {
            itemClick(dataSet[position])
        }*/

        // holder.view.setOnClickListener { onItemClicked(item) }
        holder.view.setOnClickListener {
            mListener!!.onItemClick(item)
        }


    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}