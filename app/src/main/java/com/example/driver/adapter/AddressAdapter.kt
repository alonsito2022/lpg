package com.example.driver.adapter

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.driver.R
import com.example.driver.model.Client.Address
import java.util.*
import kotlin.collections.ArrayList

class AddressAdapter(
    private val mContext: Context,
    private val mLayoutResourceId: Int,
    addresses: ArrayList<Address>,
    private val mListener: OnItemClickListener2?
) :
    ArrayAdapter<Address>(mContext, mLayoutResourceId, addresses) {
    interface OnItemClickListener2 {
        fun onItemClick(m: Address?)
    }

    private val address: MutableList<Address> = ArrayList(addresses)
    private var allClients: ArrayList<Address> = addresses

    override fun getCount(): Int {
        return address.size
    }
    override fun getItem(position: Int): Address {
        return address[position]
    }
    override fun getItemId(position: Int): Long {
        return address[position].id!!.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = (mContext as Activity).layoutInflater
            convertView = inflater.inflate(mLayoutResourceId, parent, false)

        }
        try {
            val a: Address = getItem(position)
            val autoCompleteTextViewItemReference = convertView!!.findViewById<View>(R.id.autoCompleteTextViewItemReference) as TextView
            val autoCompleteTextViewItemAddress = convertView!!.findViewById<View>(R.id.autoCompleteTextViewItemAddress) as TextView
            autoCompleteTextViewItemReference.text = "LAT: " + a.latitude.toString() + " LON: " + a.longitude.toString()
            autoCompleteTextViewItemAddress.text = a.address
            convertView.setOnClickListener {
                mListener!!.onItemClick(a)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun convertResultToString(resultValue: Any) :String {
                return (resultValue as Address).address
            }
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (constraint != null) {
                    val clientSuggestion: MutableList<Address> = ArrayList()
                    for (a in allClients) {
                        // Log.d("MIKE","performFiltering for :  ${client.toString()}")
                        if (a.address.lowercase(Locale.getDefault()).contains(constraint.toString().lowercase(
                                Locale.getDefault()))
                        ) {
                            clientSuggestion.add(a)
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
                address.clear()
                if (results.count > 0) {
                    for (result in results.values as List<*>) {
                        if (result is Address) {
                            address.add(result)
                        }
                    }
                    notifyDataSetChanged()
                } else if (constraint == null) {
                    address.addAll(allClients)
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}