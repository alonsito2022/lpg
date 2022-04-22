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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*


class ProfileFragment : Fragment() {

    private var globalContext: Context? = null

    lateinit var database: FirebaseDatabase
    private lateinit var driverReference: DatabaseReference

    private var driver : Driver = Driver()
    private var driverKey: String = ""

    private lateinit var nameProfile: TextInputEditText
    private lateinit var phoneProfile: TextInputEditText
    private lateinit var vehicleLicensePlate: TextInputEditText
    private lateinit var hasDistribution: TextInputEditText
    private lateinit var statusDistribution: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity
        database = FirebaseDatabase.getInstance()
        driverReference = database.getReference("drivers")

        val bundle = arguments
        driverKey = bundle!!.getString("driverKey").toString()

        getDriver(driverKey)

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
        hasDistribution = view.findViewById(R.id.hasDistribution)
        statusDistribution = view.findViewById(R.id.statusDistribution)

    }

    private fun getDriver(driverKey: String = ""){

        val driverRef = driverReference.orderByKey().equalTo(driverKey)
        driverRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                for (driverSnapshot in snapshot.children){
                    driver = driverSnapshot.getValue(Driver::class.java)!!
                }
                nameProfile.setText(driver.profile.name)
                phoneProfile.setText(driver.profile.phone)
                vehicleLicensePlate.setText(driver.vehicleLicensePlate)
                if(driver.distributionID > 0){
                    hasDistribution.setText("CREADO A LAS: " + driver.distributionDatetime.substring(0,10))
                    statusDistribution.setText(driver.distributionStatusDisplay + " | ID: " + driver.distributionID)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }
        })
    }
}