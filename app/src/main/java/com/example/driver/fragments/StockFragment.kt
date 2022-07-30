package com.example.driver.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.adapter.StockRegularAdapter
import com.example.driver.model.Driver
import com.example.driver.retrofit.ClientService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StockFragment : Fragment() {


    private var globalContext: Context? = null
    private var driverID: Int = 0
    private lateinit var recyclerViewMap: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity

        val bundle = arguments
        driverID = bundle!!.getInt("driverID")

        getDriver(driverID)

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
        recyclerViewMap = view.findViewById(R.id.recyclerViewMap)

    }

    private fun getDriver(id: Int = 0){

        val apiInterface = ClientService.create().searchDriverByID(Driver(id))
        apiInterface.enqueue(object : Callback<Driver> {
            override fun onResponse(call: Call<Driver>, response: Response<Driver>) {
                if (response.body() != null) {
                    val driver: Driver = response.body()!!
                    recyclerViewMap.layoutManager = LinearLayoutManager(activity)
                    recyclerViewMap.setHasFixedSize(true)
                    recyclerViewMap.adapter = StockRegularAdapter(driver.vehicle.stockRegular)
                }
            }

            override fun onFailure(call: Call<Driver>, t: Throwable) {
                Log.d("MIKE", "getDriver onFailure: " + t.message.toString())
            }

        })

    }
}