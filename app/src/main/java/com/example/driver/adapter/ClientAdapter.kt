package com.example.driver.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.driver.R
import com.example.driver.model.Client
import java.util.*
import kotlin.collections.ArrayList

class ClientAdapter(
    private val mContext: Context,
    private val mLayoutResourceId: Int,
    clients: ArrayList<Client>,
    private val mListener: OnItemClickListener2?
) :
    ArrayAdapter<Client>(mContext, mLayoutResourceId, clients) {

    interface OnItemClickListener2 {
        fun onItemClick(model: Client?)
    }

    private val client: MutableList<Client> = ArrayList(clients)
    private var allClients: ArrayList<Client> = clients

    override fun getCount(): Int {
        return client.size
    }
    override fun getItem(position: Int): Client {
        return client[position]
    }
    override fun getItemId(position: Int): Long {
        return client[position].id!!.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = (mContext as Activity).layoutInflater
            convertView = inflater.inflate(mLayoutResourceId, parent, false)

        }
        try {
            val client: Client = getItem(position)
            val clientAutoCompleteViewPhone = convertView!!.findViewById<View>(R.id.autoCompleteTextViewItemPhone) as TextView
            val clientAutoCompleteViewName = convertView!!.findViewById<View>(R.id.autoCompleteTextViewItemName) as TextView
            clientAutoCompleteViewPhone.text = client.phone
            clientAutoCompleteViewName.text = client.names
            convertView.setOnClickListener {
                mListener!!.onItemClick(client)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun convertResultToString(resultValue: Any) :String {
                return (resultValue as Client).names
            }
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (constraint != null) {
                    val clientSuggestion: MutableList<Client> = ArrayList()
                    for (client in allClients) {
                        // Log.d("MIKE","performFiltering for :  ${client.toString()}")
                        if (client.names.lowercase(Locale.getDefault()).contains(constraint.toString().lowercase(Locale.getDefault()))
                        ) {
                            clientSuggestion.add(client)
                        }
                    }
                    filterResults.values = clientSuggestion
                    filterResults.count = clientSuggestion.size
                }
                return filterResults
            }
            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults
            ) {
                client.clear()
                if (results.count > 0) {
                    for (result in results.values as List<*>) {
                        if (result is Client) {
                            client.add(result)
                        }
                    }
                    notifyDataSetChanged()
                } else if (constraint == null) {
                    client.addAll(allClients)
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}
