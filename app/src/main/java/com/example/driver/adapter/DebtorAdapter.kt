package com.example.driver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.model.Debt

class DebtorAdapter(val dataSet: ArrayList<Debt>, private val mListener: DebtorAdapter.OnItemClickListener?) : RecyclerView.Adapter<DebtorAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(model: Debt)
    }

    private var context: Context? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val cardDebtorClient: TextView
        val cardDebtorQuantity: TextView
        val view: View
        init {
            cardDebtorClient = itemView.findViewById(R.id.cardDebtorClient)
            cardDebtorQuantity = itemView.findViewById(R.id.cardDebtorQuantity)
            view = itemView

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtorAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_debtor_view, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DebtorAdapter.ViewHolder, position: Int) {
        val item = dataSet[position]
        holder.cardDebtorClient.text = item.names
        holder.cardDebtorQuantity.text = item.totalDebt.toString()
        holder.view.setOnClickListener {
            mListener!!.onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}