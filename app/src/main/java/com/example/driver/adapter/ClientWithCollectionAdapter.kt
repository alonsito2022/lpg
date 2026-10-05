package com.example.driver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.rest.ClientWithCollection

class ClientWithCollectionAdapter(
    val dataSet: ArrayList<ClientWithCollection>,
    private val mListener: OnItemClickListener?
) : RecyclerView.Adapter<ClientWithCollectionAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(model: ClientWithCollection)
    }

    private var context: Context? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewClientName: TextView
        val textViewClientPhone: TextView
        val view: View

        init {
            textViewClientName = itemView.findViewById(R.id.textViewClientName)
            textViewClientPhone = itemView.findViewById(R.id.textViewClientPhone)
            view = itemView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_client_collection_view, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
        holder.textViewClientName.text = item.clientName
        holder.textViewClientPhone.text = item.clientPhone
        holder.view.setOnClickListener {
            mListener?.onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}

