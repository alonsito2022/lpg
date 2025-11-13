package com.example.driver.adapter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.model.Dispatch
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
//import kotlinx.android.synthetic.main.order_map.*

class DispatchPlacedAdapter(val c: Context, val dataSet: ArrayList<Dispatch>, private val mListener: OnItemClickListener?): RecyclerView.Adapter<DispatchPlacedAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(model: Dispatch)
    }
    private var context: Context? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val cardDispatchDate: TextView
        val cardDispatchClient: TextView
        val cardDispatchDistribution: TextView
        val cardDispatchAddress: TextView
        val cardDispatchTotal: TextView
        val showMenu: ImageView
        val view: View

        init {
            cardDispatchDate = itemView.findViewById(R.id.cardDispatchDate)
            cardDispatchClient = itemView.findViewById(R.id.cardDispatchClient)
            cardDispatchDistribution = itemView.findViewById(R.id.cardDispatchDistribution)
            cardDispatchAddress = itemView.findViewById(R.id.cardDispatchAddress)
            cardDispatchTotal = itemView.findViewById(R.id.cardDispatchTotal)
            showMenu = itemView.findViewById(R.id.showMenu)
            showMenu.setOnClickListener { popupMenus(itemView) }
            view = itemView

        }
        private fun bitMapFromVector(vectorResID:Int):BitmapDescriptor {
            val vectorDrawable=ContextCompat.getDrawable(context!!,vectorResID)
            vectorDrawable!!.setBounds(0,0,vectorDrawable!!.intrinsicWidth,vectorDrawable.intrinsicHeight)
            val bitmap= Bitmap.createBitmap(vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight,Bitmap.Config.ARGB_8888)
            val canvas= Canvas(bitmap)
            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
        private fun popupMenus(v:View){
            val position = dataSet[adapterPosition]
            Log.e("MIKE", "popupMenus position: ${position.status}")
            // Initialize a new popup menu instance
            val popupMenus = PopupMenu(c, v)
            // Inflate the popup menu
            popupMenus.inflate(R.menu.dispatch_menu)
            // Set popup menu item click listener
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.showDetails->{
                        val v2 = LayoutInflater.from(c).inflate(R.layout.order_detail, null)
                        val recyclerViewDispatchDetail = v2.findViewById<RecyclerView>(R.id.recyclerViewDispatchDetail)
                        val recyclerViewDispatchMethodPayment = v2.findViewById<RecyclerView>(R.id.recyclerViewDispatchMethodPayment)
                        recyclerViewDispatchDetail.layoutManager = LinearLayoutManager(c)
                        recyclerViewDispatchDetail.setHasFixedSize(true)
                        recyclerViewDispatchDetail.adapter = DispatchDetailAdapter(position.details)

                        recyclerViewDispatchMethodPayment.layoutManager = LinearLayoutManager(c)
                        recyclerViewDispatchMethodPayment.setHasFixedSize(true)
                        recyclerViewDispatchMethodPayment.adapter = MethodPaymentAdapter(position.paymentMethods)
                        AlertDialog.Builder(c)
                            .setView(v2)
                            .setPositiveButton("CERRAR"){
                                dialog,_ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()


                        true
                    }
                    R.id.showAddress->{
                        val v3 = LayoutInflater.from(c).inflate(R.layout.order_map, null)
                        val googleMap = v3.findViewById<MapView>(R.id.googleMap)
                        MapsInitializer.initialize(c)
                        val builder: AlertDialog.Builder = AlertDialog.Builder(c)
                        builder.setView(v3)
                        builder.setTitle("Direccion del cliente")

                        val dialog: Dialog = builder.create()
                        builder.show()

                        googleMap.onCreate(dialog.onSaveInstanceState())
                        googleMap.onResume()
                        googleMap.getMapAsync { map ->
                            map.setMinZoomPreference(12F)
                            var ny: LatLng = LatLng(42.6021767, 23.4674597)
                            var title = "No hay direccion"
                            var snippet = "Ni celular"
                            if(position.addressID > 0){
                                ny = LatLng(position.addressLatitude, position.addressLongitude)
                                title = "Dirección: ${ position.addressName }"
                                snippet = "Celular: ${ position.clientPhone }"
                            }


                            map.addMarker(MarkerOptions()
                                .position(ny)
                                .title(title)
//                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_delivery_truck))
//                                .icon(bitMapFromVector(R.drawable.ic_baseline_my_location_24))
                                .snippet(snippet)
                            )
                            map.moveCamera(CameraUpdateFactory.newLatLng(ny))
                            map.uiSettings.isZoomControlsEnabled = true
                            map.animateCamera(CameraUpdateFactory.zoomTo(15F), 2000, null)
                        }

                        Toast.makeText(c, "showDetails is clicked.", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else-> true
                }


            }

            // Display the popup menu with icons
            try {
                val popup = PopupMenu::class.java.getDeclaredField("mPopup")
                popup.isAccessible = true
                val menu = popup.get(popupMenus)
                menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(menu, true)
            }catch (e:Exception){
                Log.e("Mike", "Error showing menu icons.", e)
            }finally {
                popupMenus.show()
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dispatch, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
        holder.cardDispatchDate.text = item.dispatchDate
        holder.cardDispatchClient.text = item.clientName
        if(item.driverID > 0){

            if (item.distributionID > 0){
                holder.cardDispatchDistribution.text = "${item.driverName} - DIST: ${item.distributionID}"
            }
            else{
                when(item.status){
                    "01" -> {
                        holder.cardDispatchDistribution.text = "${item.driverName} - ESTADO: PENDIENTE"
                        holder.cardDispatchDistribution.setTextColor(ContextCompat.getColor(context!!,
                            android.R.color.holo_blue_bright
                        ))
                    }
                    "02" -> {
                        holder.cardDispatchDistribution.text = "${item.driverName} - ESTADO: COMPLETADO"
                        holder.cardDispatchDistribution.setTextColor(ContextCompat.getColor(context!!,
                            android.R.color.holo_green_dark
                        ))
                    }
                    "03" -> {
                        holder.cardDispatchDistribution.text = "${item.driverName} - ESTADO: ANULADO"
                        holder.cardDispatchDistribution.setTextColor(ContextCompat.getColor(context!!,
                            android.R.color.background_dark
                        ))
                    }
                    "04" -> {
                        holder.cardDispatchDistribution.text = "${item.driverName} - ESTADO: EN RUTA"
                        holder.cardDispatchDistribution.setTextColor(ContextCompat.getColor(context!!,
                            android.R.color.holo_orange_dark
                        ))
                    }
                    "05" -> {
                        holder.cardDispatchDistribution.text = "${item.driverName} - ESTADO: ASIGNADO"
                        holder.cardDispatchDistribution.setTextColor(ContextCompat.getColor(context!!,
                            android.R.color.holo_red_dark
                        ))
                    }
                }

            }
        }else{
            holder.cardDispatchDistribution.text = "Pedido sin asignar"
            holder.cardDispatchDistribution.setTextColor(ContextCompat.getColor(context!!,
                R.color.orange_700
            ))
        }

        holder.cardDispatchAddress.text = item.addressName
        holder.cardDispatchTotal.text = item.totalPrice.toString()
        holder.cardDispatchTotal.setOnClickListener {
            mListener!!.onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}