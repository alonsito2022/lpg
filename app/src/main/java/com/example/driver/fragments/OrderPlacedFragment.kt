package com.example.driver.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.activities.HomeActivity
import com.example.driver.adapter.DispatchDetailAdapter
import com.example.driver.adapter.DispatchPlacedAdapter
import com.example.driver.adapter.MethodPaymentAdapter
import com.example.driver.model.Dispatch
import com.example.driver.model.Driver
import com.example.driver.model.Vehicle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class OrderPlacedFragment : Fragment() {

    private var globalContext: Context? = null

    lateinit var database: FirebaseDatabase
    private lateinit var dispatchReference: DatabaseReference
    private lateinit var distributionReference: DatabaseReference

    private var dispatch: Dispatch = Dispatch()
    private var vehicle: Vehicle = Vehicle()
    private var driver : Driver = Driver()
    private var vehicleKey: String = ""
    private var driverKey: String = ""
    private var driverID: Int = 0

    private lateinit var recyclerViewOrderPlaced: RecyclerView
    private lateinit var recyclerViewDispatchMethodPayment: RecyclerView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity
        database = FirebaseDatabase.getInstance()
        dispatchReference = database.getReference("dispatches")
        distributionReference = database.getReference("distributions")

        val bundle = arguments
        vehicleKey = bundle!!.getString("vehicleKey").toString()
        driverKey = bundle.getString("driverKey").toString()
        driverID = bundle.getInt("driverID")

        loadDispatches()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_placed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewOrderPlaced = view.findViewById(R.id.recyclerViewOrderPlaced)

        fab = view.findViewById(R.id.floatingActionButtonNewDispatch)
        fab.setOnClickListener { goToFragment() }
    }

    private fun goToFragment(){
        Log.d("MIKE", "go to fragment")
        (activity as HomeActivity).goToSaleFragment()
    }

    private fun addInfo(d: Dispatch) {
        val inflater = LayoutInflater.from(globalContext)
        val v = inflater.inflate(R.layout.order_detail, null)
        val recyclerViewDispatchDetail = v.findViewById<RecyclerView>(R.id.recyclerViewDispatchDetail)
        val recyclerViewDispatchMethodPayment = v.findViewById<RecyclerView>(R.id.recyclerViewDispatchMethodPayment)

        recyclerViewDispatchDetail.layoutManager = LinearLayoutManager(activity)
        recyclerViewDispatchDetail.setHasFixedSize(true)
        recyclerViewDispatchDetail.adapter = DispatchDetailAdapter(d.details)

        recyclerViewDispatchMethodPayment.layoutManager = LinearLayoutManager(activity)
        recyclerViewDispatchMethodPayment.setHasFixedSize(true)
        recyclerViewDispatchMethodPayment.adapter = MethodPaymentAdapter(d.paymentMethods)

        val addDialog = AlertDialog.Builder(globalContext)
        addDialog.setView(v)
        addDialog.setPositiveButton("OK") { dialog, _ ->
            Toast.makeText(globalContext, "OK", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        addDialog.create()
        addDialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun loadDispatches() {
        val sdf2 = SimpleDateFormat("dd/MM/yyyy")
        val currentDate2 = sdf2.format(Date())
        val identifier = currentDate2.replace("/", "") + "0102"
        dispatchReference.orderByChild("identifier").equalTo(identifier).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productArrayList = ArrayList<Dispatch>()

                for (productSnapshot in snapshot.children) {
                    val dispatchTemp = productSnapshot.getValue(Dispatch::class.java)!!
                    productArrayList.add(dispatchTemp)
                }
                val filterProductArrayList = productArrayList.filter { it.driverID == driverID }
                Log.d("MIKE", "loadDispatches: ${filterProductArrayList.size}")
                if(productArrayList.isNotEmpty()){
                    recyclerViewOrderPlaced.layoutManager = LinearLayoutManager(activity)
                    recyclerViewOrderPlaced.setHasFixedSize(true)
                    recyclerViewOrderPlaced.adapter = DispatchPlacedAdapter(
                        globalContext!!,
                        filterProductArrayList as ArrayList<Dispatch>,
                        object : DispatchPlacedAdapter.OnItemClickListener {
                            override fun onItemClick(model: Dispatch) {
                                addInfo(model)
                            }
                        }
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }

        })
    }
}