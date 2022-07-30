package com.example.driver.adapter

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.driver.R
import com.example.driver.model.Zone
import java.util.*
import kotlin.collections.ArrayList

class ZoneAdapter(
    private val mContext: Context,
    private val mLayoutResourceId: Int,
    zones: ArrayList<Zone>,
    private val mListener: OnItemClickListener2?
) :
    ArrayAdapter<Zone>(mContext, mLayoutResourceId, zones) {

    interface OnItemClickListener2 {
        fun onItemClick(model: Zone?)
    }

    private val zone: MutableList<Zone> = ArrayList(zones)
    private var allZones: ArrayList<Zone> = zones

    override fun getCount(): Int {
        return zone.size
    }
    override fun getItem(position: Int): Zone {
        return zone[position]
    }
    override fun getItemId(position: Int): Long {
        return zone[position].id!!.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = (mContext as Activity).layoutInflater
            convertView = inflater.inflate(mLayoutResourceId, parent, false)

        }
        try {
            val zone: Zone = getItem(position)
            val autoCompleteTextViewItemReference = convertView!!.findViewById<View>(R.id.autoCompleteTextViewItemReference) as TextView
            val autoCompleteTextViewItemAddress = convertView!!.findViewById<View>(R.id.autoCompleteTextViewItemAddress) as TextView
            autoCompleteTextViewItemReference.text = zone.reference
            autoCompleteTextViewItemAddress.text = zone.address
            convertView.setOnClickListener {
                mListener!!.onItemClick(zone)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun convertResultToString(resultValue: Any) :String {
                return (resultValue as Zone).address
            }
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (constraint != null) {
                    val zoneSuggestion: MutableList<Zone> = ArrayList()
                    for (zone in allZones) {
                        // Log.d("MIKE","performFiltering for :  ${zone.toString()}")
                        if (zone.address.lowercase(Locale.getDefault()).contains(constraint.toString().lowercase(
                                Locale.getDefault()))
                        ) {
                            zoneSuggestion.add(zone)
                        }
                    }
                    filterResults.values = zoneSuggestion
                    filterResults.count = zoneSuggestion.size
                }
                return filterResults
            }
            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults
            ) {
                zone.clear()
                if (results.count > 0) {
                    for (result in results.values as List<*>) {
                        if (result is Zone) {
                            zone.add(result)
                        }
                    }
                    notifyDataSetChanged()
                } else if (constraint == null) {
                    zone.addAll(allZones)
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}