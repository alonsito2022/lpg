package com.example.driver.Activity

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.driver.*
import com.example.driver.LocalDatabase.Preference
import com.google.android.material.navigation.NavigationView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driver.model.Coordinate
import com.example.driver.R
import com.example.driver.adapter.VehicleStockRegularFilledAdapter
import com.example.driver.adapter.VehicleStockRegularVoidAdapter
import com.example.driver.model.Driver
import com.example.driver.model.Vehicle
import com.google.android.gms.location.*
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_stock.*
import java.util.concurrent.TimeUnit


class HomeActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var navViewListener: NavigationView.OnNavigationItemSelectedListener

    private lateinit var preference: Preference

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    lateinit var database : FirebaseDatabase
    lateinit var databaseReference: DatabaseReference


    var driver : Driver = Driver()

    var filledStock: Map<String, Vehicle.Stock.StockProduct> = mapOf()
    var voidStock: Map<String, Vehicle.Stock.StockProduct> = mapOf()

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val LOCATION_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        preference = Preference(applicationContext)

//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

//        locationRequest = LocationRequest.create().apply {
//            interval = TimeUnit.SECONDS.toMillis(20)
//            fastestInterval = TimeUnit.SECONDS.toMillis(10)
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
////            maxWaitTime= TimeUnit.MINUTES.toMillis(2)
//        }

//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                super.onLocationResult(locationResult)
//                val currentLocation = locationResult.lastLocation
//
//                if(preference.getData("uid") != ""){
//                    database = FirebaseDatabase.getInstance()
//                    databaseReference = database.getReference().child("drivers").child(preference.getData("uid"))
//                    val locationLogging = Coordinate(currentLocation.latitude, currentLocation.longitude)
//                    databaseReference.child("coordinates").setValue(locationLogging)
//                        .addOnSuccessListener {
//                            Log.d("MIKE", "Locations written into the database: $locationLogging")
//                        }
//                        .addOnFailureListener {
//                            Toast.makeText(applicationContext, "Error occured while writing the locations", Toast.LENGTH_LONG).show()
//                        }
//                }
//
//            }
//        }

//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//
//            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
//            Log.d("MIKE", "if checkSelfPermission")
//            // finish()
//        }
//        else{
//            Log.d("MIKE", "else checkSelfPermission")
//        }

//        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val hView: View = navView.getHeaderView(0)
        val nameTv: TextView = hView.findViewById(R.id.nav_header_name_textView)
        val phoneTv: TextView = hView.findViewById(R.id.nav_header_phone_textView)
        val emailTv: TextView = hView.findViewById(R.id.nav_header_email_textView)
        val imgVw: ImageView = hView.findViewById(R.id.nav_header_imageView)

        val driverKey = preference.getData("uid")

        if(driverKey.isNotEmpty()){

            nameTv.setText(preference.getData("name"))
            phoneTv.setText(preference.getData("phone"))
            emailTv.setText(preference.getData("email"))
            Picasso.get().load(preference.getData("path")).into(imgVw)
        }
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getDriver(driverKey)

        navViewListener = NavigationView.OnNavigationItemSelectedListener {
            unCheckAllMenuItems()
            it.isChecked = true
            when(it.itemId){
                R.id.nav_item_sales -> replaceFragment(SaleFragment(), it.title.toString())
                R.id.nav_item_inventory -> replaceFragment(StockFragment(), it.title.toString())
                R.id.nav_item_recovery -> replaceFragment(RecoveryFragment(), it.title.toString())
                R.id.nav_item_payments -> replaceFragment(PaymentFragment(), it.title.toString())
                R.id.nav_item_order_assignment -> replaceFragment(OrderAssignmentFragment(), it.title.toString())
                R.id.nav_item_order_placed -> replaceFragment(OrderPlacedFragment(), it.title.toString())
                R.id.nav_item_profile -> replaceFragment(ProfileFragment(), it.title.toString())
                R.id.nav_item_exit -> {
                    preference.clearPreference()
                    finish()
                }
            }
            true
        }

        navView.setNavigationItemSelectedListener(navViewListener)

        val hasNotification: String = intent.extras?.get("notification").toString()

        Log.d("MIKE", "hasNotification: $hasNotification")

        if(hasNotification != "null"){
            goToOrderAssignmentFragment()
        }else if(savedInstanceState == null){
            val itemSelected = navView.getMenu().getItem(4).subMenu.getItem(0)
            navViewListener.onNavigationItemSelected(itemSelected)
        }

    }

    private fun unCheckAllMenuItems(){
        // val size = navView.menu.size()
        for (i in 0..4) {

            when (i) {
                in 0..2 -> navView.menu.getItem(i).isChecked = false
                3 -> {
                    for (j in 0..2){navView.menu.getItem(i).subMenu.getItem(j).isChecked = false}
                }
                4 -> {
                    for (k in 0..1){navView.menu.getItem(i).subMenu.getItem(k).isChecked = false}
                }
            }
        }
    }

    fun goToOrderAssignmentFragment(){
        val itemSelected = navView.getMenu().getItem(3).subMenu.getItem(1)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun goToOrderPlacedFragment(){
        val itemSelected = navView.getMenu().getItem(3).subMenu.getItem(2)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun goToSaleFragment(){
        val itemSelected = navView.getMenu().getItem(0)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun goToProfileFragment(){
        val itemSelected = navView.getMenu().getItem(4).subMenu.getItem(0)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun goToRecoveryFragment(){
        val itemSelected = navView.getMenu().getItem(2)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun replaceFragment(fragment: Fragment, title: String){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        // bundle.putSerializable("list", filledStock)
        bundle.putInt("driverID", driver.driverID)
        bundle.putInt("distributionID", driver.distributionID)
        bundle.putString("vehicleKey", preference.getData("vehicleKey"))
        bundle.putString("driverKey", preference.getData("uid"))
        Log.d("MIKE", driver.vehicleKey)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
        drawerLayout.closeDrawers()
        setTitle(title)
    }

    fun toggleDisableMenu(status: Boolean){
        navView.getMenu().getItem(0).isEnabled = status
        navView.getMenu().getItem(1).isEnabled = status
        navView.getMenu().getItem(2).isEnabled = status
        navView.getMenu().getItem(3).subMenu.getItem(0).isEnabled = status
        navView.getMenu().getItem(3).subMenu.getItem(1).isEnabled = status
        navView.getMenu().getItem(3).subMenu.getItem(2).isEnabled = status
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    private fun getDriver(driverKey: String = ""){

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("drivers")
        val driverRef = databaseReference.orderByKey().equalTo(driverKey)
        driverRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // driver = Driver()

                for (driverSnapshot in snapshot.children){
                    driver = driverSnapshot.getValue(Driver::class.java)!!
                    Log.d("MIKE", driver.toString())
                }

                if(driver.distributionID > 0){
                    if (driver.distributionStatusDisplay == "PROGRAMADO"){
                        toggleDisableMenu(true)
                    }else{
                        toggleDisableMenu(false)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }
        })
    }

}