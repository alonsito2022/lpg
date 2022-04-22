package com.example.driver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.model.Dispatch
import com.example.driver.model.Payment

class PaymentAdapter(val dataSet: MutableMap<String, Payment.PaymentDispatch>, private val mListener: OnItemClickListener?): RecyclerView.Adapter<PaymentAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(model: Payment.PaymentDispatch)
    }
    private var context: Context? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val cardDispatchPaymentDate: TextView
        val cardDispatchPaymentClient: TextView
        val cardDispatchPaymentTotal: TextView
        val cardDispatchPaymentType: TextView
        val view: View

        init {
            cardDispatchPaymentType = itemView.findViewById(R.id.cardDispatchPaymentType)
            cardDispatchPaymentDate = itemView.findViewById(R.id.cardDispatchPaymentDate)
            cardDispatchPaymentClient = itemView.findViewById(R.id.cardDispatchPaymentClient)
            cardDispatchPaymentTotal = itemView.findViewById(R.id.cardDispatchPaymentTotal)
            view = itemView

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dispatch_payment_view, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //val item = dataSet[position]
        val item = dataSet[dataSet.keys.elementAt(position)]!!

        holder.cardDispatchPaymentType.text = "D"
        holder.cardDispatchPaymentDate.text = item.dispatchDate
        holder.cardDispatchPaymentClient.text = item.clientName
        holder.cardDispatchPaymentTotal.text = item.total.toString()
        holder.view.setOnClickListener {
            mListener!!.onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}