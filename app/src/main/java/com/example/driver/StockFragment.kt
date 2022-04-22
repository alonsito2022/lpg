package com.example.driver

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.driver.Activity.HomeActivity
import com.example.driver.LocalDatabase.Preference
import com.example.driver.adapter.VehicleStockRegularFilledAdapter
import com.example.driver.adapter.VehicleStockRegularVoidAdapter
import com.example.driver.model.Vehicle
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_stock.*

class StockFragment : Fragment() {

    lateinit var database : FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private var globalContext: Context? = null

    private var filteredFilledMap: Map<String, Vehicle.Stock.StockProduct> = mapOf()
    private var filteredVoidMap: Map<String, Vehicle.Stock.StockProduct> = mapOf()

    private var vehicle : Vehicle = Vehicle()
    private var vehicleKey : String = ""

    private lateinit var recyclerViewFilledMap: RecyclerView
    private lateinit var recyclerViewVoidMap: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity
        val bundle = arguments
        vehicleKey = bundle!!.getString("vehicleKey").toString()
        Log.d("MIKE", "onCreateView")
        Log.d("MIKE", vehicleKey)
        getStock(vehicleKey)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stock, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataSet: Array<String> = arrayOf("First", "Second", "Third", "Fourth")
        // filteredVoidMap = (activity as HomeActivity).voidStock
        recyclerViewFilledMap = view.findViewById(R.id.recyclerViewFilledMap)
        recyclerViewVoidMap = view.findViewById(R.id.recyclerViewVoidMap)

    }

    private fun getStockData(){

        recyclerViewFilledMap.layoutManager = GridLayoutManager(globalContext, 2)
        recyclerViewFilledMap.setHasFixedSize(true)
        recyclerViewFilledMap.adapter = VehicleStockRegularFilledAdapter(filteredFilledMap)

        recyclerViewVoidMap.layoutManager = GridLayoutManager(globalContext, 2)
        recyclerViewVoidMap.setHasFixedSize(true)
        recyclerViewVoidMap.adapter = VehicleStockRegularVoidAdapter(filteredVoidMap)

    }

    private fun getStock(vehicleKey: String = ""){
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("vehicles")
        val vehicleRef = databaseReference.orderByKey().equalTo(vehicleKey)
        vehicleRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    for (vehicleSnapshot in snapshot.children){
                        vehicle = vehicleSnapshot.getValue(Vehicle::class.java)!!
                    }
                    val filledMap = vehicle.stock.regular.filled
                    filteredFilledMap = filledMap.filter { (_, value) -> value.quantity > 0}

                    val voidMap = vehicle.stock.regular.void
                    filteredVoidMap = voidMap.filter { (_, value) -> value.quantity > 0}

                    getStockData()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }

        })
    }
}