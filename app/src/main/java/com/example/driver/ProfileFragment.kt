package com.example.driver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.driver.Activity.HomeActivity
import com.example.driver.model.Driver
import com.example.driver.retrofit.ClientService
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private var globalContext: Context? = null
    private var driverID: Int = 0

    private lateinit var nameProfile: TextInputEditText
    private lateinit var phoneProfile: TextInputEditText
    private lateinit var vehicleLicensePlate: TextInputEditText

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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameProfile = view.findViewById(R.id.nameProfile)
        phoneProfile = view.findViewById(R.id.phoneProfile)
        vehicleLicensePlate = view.findViewById(R.id.vehicleLicensePlate)

    }

    private fun getDriver(id: Int = 0){

        val apiInterface = ClientService.create().searchDriverByID(Driver(id))
        apiInterface.enqueue(object : Callback<Driver>{
            override fun onResponse(call: Call<Driver>, response: Response<Driver>) {
                if (response.body() != null) {
                    val driver: Driver = response.body()!!
                    nameProfile.setText(driver.names)
                    phoneProfile.setText(driver.phone)
                    vehicleLicensePlate.setText(driver.vehicle.licensePlate)
                }
            }

            override fun onFailure(call: Call<Driver>, t: Throwable) {
                Log.d("MIKE", "getDriver onFailure: " + t.message.toString())
            }

        })

    }
}