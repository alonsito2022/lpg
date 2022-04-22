package com.example.driver.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.model.Debt

class DebtDetailAdapter(val dataSet: MutableMap<Int, Debt.ProductOwed>, private val mListener: OnItemClickListener?): RecyclerView.Adapter<DebtDetailAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(model: Debt.ProductOwed, unitID: Int)
    }
    private var context: Context? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textViewDebtDetailProductName: TextView = itemView.findViewById(R.id.textViewDebtDetailProductName)
        val btnDebtDetailPendingBG: Button = itemView.findViewById(R.id.btnDebtDetailPendingBG)
        val btnDebtDetailPendingB: Button = itemView.findViewById(R.id.btnDebtDetailPendingB)
        val btnDebtDetailPendingG: Button = itemView.findViewById(R.id.btnDebtDetailPendingG)
        val view = itemView
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_debt_detail_view, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = dataSet[position]
        val item = dataSet[dataSet.keys.elementAt(position)]
        // val item = dataSet[dataSet.keys.elementAt(position)]

        holder.textViewDebtDetailProductName.text = item!!.productName

        holder.btnDebtDetailPendingBG.text = item.units[1]!!.remainingQuantity!!.toInt().toString()
        holder.btnDebtDetailPendingB.text = item.units[2]!!.remainingQuantity!!.toInt().toString()
        holder.btnDebtDetailPendingG.text = item.units[3]!!.remainingQuantity!!.toInt().toString()

        if(item.units[1]!!.remainingQuantity!! > 0){
            holder.btnDebtDetailPendingBG.setOnClickListener {
                mListener!!.onItemClick(item, 1)
            }
        }
        else{
            holder.btnDebtDetailPendingBG.isEnabled = false
            holder.btnDebtDetailPendingBG.setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
            holder.btnDebtDetailPendingBG.setTextColor(ContextCompat.getColor(context!!, R.color.black))
        }

        if(item.units[2]!!.remainingQuantity!! > 0){
            holder.btnDebtDetailPendingB.setOnClickListener {
                mListener!!.onItemClick(item, 2)
            }
        }
        else{
            holder.btnDebtDetailPendingB.isEnabled = false
            holder.btnDebtDetailPendingB.setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
            holder.btnDebtDetailPendingB.setTextColor(ContextCompat.getColor(context!!, R.color.black))
        }

        if(item.units[3]!!.remainingQuantity!! > 0){
            holder.btnDebtDetailPendingG.setOnClickListener {
                mListener!!.onItemClick(item, 3)
            }
        }
        else{
            holder.btnDebtDetailPendingG.isEnabled = false
            holder.btnDebtDetailPendingG.setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
            holder.btnDebtDetailPendingG.setTextColor(ContextCompat.getColor(context!!, R.color.black))
        }

    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

}