package com.example.driver.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.driver.LocalDatabase.Preference
import com.google.android.material.navigation.NavigationView
import android.widget.TextView
import com.example.driver.R
import com.example.driver.fragments.*
import com.example.driver.model.Driver


class HomeActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var navViewListener: NavigationView.OnNavigationItemSelectedListener

    private lateinit var preference: Preference

    var driver : Driver = Driver()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        preference = Preference(applicationContext)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val hView: View = navView.getHeaderView(0)
        val nameTv: TextView = hView.findViewById(R.id.nav_header_name_textView)
        val phoneTv: TextView = hView.findViewById(R.id.nav_header_phone_textView)
        val emailTv: TextView = hView.findViewById(R.id.nav_header_email_textView)
        val licensePlateTv: TextView = hView.findViewById(R.id.nav_header_license_plate_textView)
//        val imgVw: ImageView = hView.findViewById(R.id.nav_header_imageView)

        val driverID = preference.getData("driverID")

        if(driverID.isNotEmpty()){

            nameTv.text = preference.getData("names")
            phoneTv.text = preference.getData("phone")
            emailTv.text = preference.getData("email")
            licensePlateTv.text = preference.getData("licensePlate")
//            Picasso.get().load(preference.getData("path")).into(imgVw)
        }
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
                R.id.nav_item_cashes -> replaceFragment(CashFragment(), it.title.toString())
                R.id.nav_item_locations -> replaceFragment(ZoneFragment(), it.title.toString())
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

        if(hasNotification != "null"){
            goToOrderAssignmentFragment()
        }else if(savedInstanceState == null){
            val itemSelected = navView.menu.getItem(4).subMenu!!.getItem(0)
            navViewListener.onNavigationItemSelected(itemSelected)
        }

    }

    private fun unCheckAllMenuItems(){
        // val size = navView.menu.size()
        for (i in 0..4) {

            when (i) {
                in 0..2 -> navView.menu.getItem(i).isChecked = false
                3 -> {
                    for (j in 0..4){navView.menu.getItem(i).subMenu!!.getItem(j).isChecked = false}
                }
                4 -> {
                    for (k in 0..1){navView.menu.getItem(i).subMenu!!.getItem(k).isChecked = false}
                }
            }
        }
    }

    fun goToOrderAssignmentFragment(){
        val itemSelected = navView.menu.getItem(3).subMenu!!.getItem(1)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun goToOrderPlacedFragment(){
        val itemSelected = navView.menu.getItem(3).subMenu!!.getItem(2)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun goToCashFragment(){
        val itemSelected = navView.menu.getItem(3).subMenu!!.getItem(3)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun goToZoneFragment(){
        val itemSelected = navView.menu.getItem(3).subMenu!!.getItem(4)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun goToSaleFragment(){
        val itemSelected = navView.menu.getItem(0)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun goToProfileFragment(){
        val itemSelected = navView.menu.getItem(4).subMenu!!.getItem(0)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun goToRecoveryFragment(){
        val itemSelected = navView.menu.getItem(2)
        navViewListener.onNavigationItemSelected(itemSelected)
    }

    fun replaceFragment(fragment: Fragment, title: String){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        // bundle.putSerializable("list", filledStock)
        bundle.putInt("driverID", preference.getData("driverID").toInt())
//        bundle.putInt("distributionID", driver.distributionID)
//        bundle.putString("vehicleKey", preference.getData("vehicleKey"))
//        bundle.putString("driverKey", preference.getData("uid"))
//        Log.d("MIKE", driver.vehicleKey)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
        drawerLayout.closeDrawers()
        setTitle(title)
    }

    fun toggleDisableMenu(status: Boolean){
        navView.menu.getItem(0).isEnabled = status
        navView.menu.getItem(1).isEnabled = status
        navView.menu.getItem(2).isEnabled = status
        navView.menu.getItem(3).subMenu!!.getItem(0).isEnabled = status
        navView.menu.getItem(3).subMenu!!.getItem(1).isEnabled = status
        navView.menu.getItem(3).subMenu!!.getItem(2).isEnabled = status
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}