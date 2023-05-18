package com.example.driver.fragments

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.driver.R
import com.example.driver.activities.HomeActivity
import com.example.driver.adapter.AddressAdapter
import com.example.driver.adapter.ClientAdapter
import com.example.driver.adapter.ZoneAdapter
import com.example.driver.model.Client
import com.example.driver.model.Driver
import com.example.driver.model.Zone
import com.example.driver.rest.ApiResponse
import com.example.driver.retrofit.ClientService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList


class ZoneFragment : Fragment() , OnMapReadyCallback {

    private var globalContext: Context? = null
    private var driver : Driver = Driver()
    private var client : Client = Client()

    private lateinit var btnSearchClient: Button
    private lateinit var clientAutoCompleteView: MaterialAutoCompleteTextView
    private lateinit var editTextClientPhone: TextInputEditText
    private lateinit var editTextClientName: TextInputEditText

    private lateinit var textViewLatitude: TextView
    private lateinit var textViewLongitude: TextView

    private lateinit var textInputLayoutNewAddress: TextInputLayout
    private lateinit var textInputLayoutChoiceAddress: TextInputLayout
    private lateinit var autoCompleteChoiceAddress: AutoCompleteTextView
    private lateinit var editTextNewAddress: TextInputEditText

    private lateinit var radioGroupClientCondition: RadioGroup
    private lateinit var radioGroupAddressStatus: RadioGroup
    private lateinit var radioButtonHasAddresses: RadioButton
    private lateinit var radioButtonNewAddress: RadioButton
    private lateinit var radioButtonClientExists: RadioButton
    private lateinit var radioButtonNewClient: RadioButton

    private lateinit var btnSaveAddress: Button
    private lateinit var btnSearchZone: Button
    private lateinit var zoneAutoCompleteView: MaterialAutoCompleteTextView

    private lateinit var mMap: GoogleMap
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity

        val bundle = arguments
        driver.driverID = bundle!!.getInt("driverID")
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(globalContext!!)
    }
    private fun fetchLocation() {
        val task= fusedLocationProviderClient.lastLocation
        if(ActivityCompat.checkSelfPermission(globalContext!!, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(globalContext!!, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(globalContext as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        task.addOnSuccessListener {
            if (it!=null){
                Toast.makeText(globalContext, "lat ${it.latitude} ${it.longitude}", Toast.LENGTH_SHORT).show()
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
                textViewLatitude.text = it.latitude.toString()
                textViewLongitude.text = it.longitude.toString()
            }
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_zone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        zoneAutoCompleteView = view.findViewById(R.id.zoneAutoCompleteView)
        clientAutoCompleteView = view.findViewById(R.id.clientAutoCompleteView)
        editTextClientPhone = view.findViewById(R.id.editTextClientPhone)
        editTextClientName = view.findViewById(R.id.editTextClientName)
        btnSearchClient = view.findViewById(R.id.btnSearchClient)
        btnSearchClient.setOnClickListener{loadRestClient()}
        btnSearchZone = view.findViewById(R.id.btnSearchZone)
        btnSearchZone.setOnClickListener{loadRestZone()}

        textViewLatitude = view.findViewById(R.id.textViewLatitude)
        textViewLongitude = view.findViewById(R.id.textViewLongitude)
        textInputLayoutNewAddress = view.findViewById(R.id.textInputLayoutNewAddress)
        textInputLayoutChoiceAddress = view.findViewById(R.id.textInputLayoutChoiceAddress)
        autoCompleteChoiceAddress = view.findViewById(R.id.autoCompleteChoiceAddress)
        editTextNewAddress = view.findViewById(R.id.editTextNewAddress)
        radioGroupAddressStatus = view.findViewById(R.id.radioGroupAddressStatus)
        radioButtonHasAddresses = view.findViewById(R.id.radioButtonHasAddresses)
        radioButtonNewAddress = view.findViewById(R.id.radioButtonNewAddress)
        radioGroupClientCondition = view.findViewById(R.id.radioGroupClientCondition)
        radioButtonClientExists = view.findViewById(R.id.radioButtonClientExists)
        radioButtonNewClient = view.findViewById(R.id.radioButtonNewClient)
        btnSaveAddress = view.findViewById(R.id.btnSaveAddress)
        btnSaveAddress.setOnClickListener{saveAddress()}

        radioGroupAddressStatus.setOnCheckedChangeListener{ _, checkedId ->
            when (checkedId) {
                R.id.radioButtonHasAddresses -> {
                    textInputLayoutChoiceAddress.visibility = View.VISIBLE
                    textInputLayoutNewAddress.visibility = View.GONE
                    client.createOrUpdate = "U"
                }
                R.id.radioButtonNewAddress -> {
                    textInputLayoutNewAddress.visibility = View.VISIBLE
                    textInputLayoutChoiceAddress.visibility = View.GONE
                    client.createOrUpdate = "C"
                }
                else -> {}
            }
        }
        radioGroupClientCondition.setOnCheckedChangeListener{ _, checkedId ->
            when (checkedId) {
                R.id.radioButtonClientExists -> {
                    client.createOrUpdate = "U"
                }
                R.id.radioButtonNewClient -> {
                    client.id = null
                    client.createOrUpdate = "C"
                    radioButtonNewAddress.isChecked = true
                }
                else -> {}
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.frg) as SupportMapFragment?
        //use SupportMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment!!.getMapAsync(this)
    }


    private fun loadRestClient() {
        if(clientAutoCompleteView.text.isNotEmpty() && clientAutoCompleteView.text.toString().count() >=3){

            val apiInterface = ClientService.create().searchClientsAndAddresses(clientAutoCompleteView.text.toString())
            apiInterface.enqueue(object : Callback<ArrayList<Client>> {

                override fun onResponse(
                    call: Call<ArrayList<Client>>?,
                    response: Response<ArrayList<Client>>?
                ) {

                    val list: ArrayList<Client>
                    if (response?.body() != null) {
                        list = response.body()!!
                        Toast.makeText(globalContext, "Se encontraron ${list.size} resultado(s)", Toast.LENGTH_SHORT).show()


                        val clientAdapter = ClientAdapter(globalContext!!,
                            R.layout.default_layout, list, object : ClientAdapter.OnItemClickListener2 {
                                override fun onItemClick(model: Client?) {
                                    clientAutoCompleteView.setText(model!!.names)
                                    clientAutoCompleteView.dismissDropDown()
                                    val inputManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    clientAutoCompleteView.closeKeyBoard(inputManager)
                                    clientAutoCompleteView.clearFocus()

                                    client.id = model.id
                                    client.names = model.names
                                    client.phone = model.phone

                                    editTextClientPhone.setText(model.phone)
                                    editTextClientName.setText(model.names)

                                    Toast.makeText(globalContext, "Se encontraron ${model.addresses.size} direccion(es)", Toast.LENGTH_SHORT).show()

                                    if(model.addresses.size > 0){

                                        client.addresses = model.addresses

                                        radioButtonHasAddresses.isChecked = true

                                        val addressAdapter = AddressAdapter(globalContext!!,R.layout.address_default_layout, model.addresses, object : AddressAdapter.OnItemClickListener2{
                                            override fun onItemClick(m: Client.Address?) {
                                                autoCompleteChoiceAddress.setText(m!!.address)
                                                autoCompleteChoiceAddress.dismissDropDown()
                                                val inputManager2 = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                autoCompleteChoiceAddress.closeKeyBoard(inputManager2)
                                                autoCompleteChoiceAddress.clearFocus()

                                                addMarkerInMap(LatLng(m.latitude, m.longitude), m.address)
                                                client.createOrUpdateAddress = m
                                                client.createOrUpdate = "U"
                                            }
                                        })
                                        autoCompleteChoiceAddress.setAdapter(addressAdapter)
                                        autoCompleteChoiceAddress.showDropDown()


                                    }

                                }
                            })
                        clientAutoCompleteView.setAdapter(clientAdapter)
                        clientAutoCompleteView.showDropDown()

                    }
                }

                override fun onFailure(
                    call: Call<ArrayList<Client>>?,
                    t: Throwable?
                ) {
                    Log.d("MIKE", "Algo salio mal..." + t!!.message.toString())
                }
            })
        }else{
            Toast.makeText(globalContext, "Minimo 3 caracteres", Toast.LENGTH_SHORT).show()
        }

    }


    private fun loadRestZone() {
        if(zoneAutoCompleteView.text.isNotEmpty() && zoneAutoCompleteView.text.toString().count() >=3){

            val apiInterface = ClientService.create().searchZones(zoneAutoCompleteView.text.toString())
            apiInterface.enqueue(object : Callback<ArrayList<Zone>> {

                override fun onResponse(
                    call: Call<ArrayList<Zone>>?,
                    response: Response<ArrayList<Zone>>?
                ) {

                    val list: ArrayList<Zone>
                    if (response?.body() != null) {
                        list = response.body()!!
                        Toast.makeText(globalContext, "Se encontraron ${list.size} resultado(s)", Toast.LENGTH_SHORT).show()

                        val zoneAdapter = ZoneAdapter(globalContext!!,
                            R.layout.zone_default_layout, list, object : ZoneAdapter.OnItemClickListener2 {
                                override fun onItemClick(model: Zone?) {
                                    zoneAutoCompleteView.setText(model!!.address)
                                    zoneAutoCompleteView.dismissDropDown()
                                    val inputManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    zoneAutoCompleteView.closeKeyBoard(inputManager)
                                    zoneAutoCompleteView.clearFocus()

                                    radioButtonNewAddress.isChecked = true

                                    client.zone = model
                                    client.createOrUpdate = "C"
                                    editTextNewAddress.setText(model.address)
                                    client.createOrUpdateAddress.address = model.address
                                    client.createOrUpdateAddress.latitude = model.latitude
                                    client.createOrUpdateAddress.longitude = model.longitude
                                    addMarkerInMap(LatLng(model.latitude, model.longitude), model.address)

                                }
                            })
                        zoneAutoCompleteView.setAdapter(zoneAdapter)
                        zoneAutoCompleteView.showDropDown()

                    }
                }

                override fun onFailure(
                    call: Call<ArrayList<Zone>>?,
                    t: Throwable?
                ) {
                    Log.d("MIKE", "Algo salio mal..." + t!!.message.toString())
                }
            })
        }else{
            Toast.makeText(globalContext, "Minimo 3 caracteres", Toast.LENGTH_SHORT).show()
        }

    }

    private fun saveAddress(){
        if(client.createOrUpdate == "C")
            client.createOrUpdateAddress.address = editTextNewAddress.text.toString()
        else
            client.createOrUpdateAddress.address = autoCompleteChoiceAddress.text.toString()

        client.names = editTextClientName.text.toString()
        client.phone = editTextClientPhone.text.toString()

//        if (client.id != null){
            val apiInterface = ClientService.create().registerClientAddress(client)
            apiInterface.enqueue(object : Callback<ApiResponse>{
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    (activity as HomeActivity).goToZoneFragment()
                    Toast.makeText(globalContext, "Direccion registrada.", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.d("MIKE", "createRestDispatchDetail onFailure: " + t.message.toString())
                }

            })
//        }
    }

    private fun View.closeKeyBoard(inputMethodManager: InputMethodManager) {
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int, color: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
//        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val latLng = LatLng(-16.393409, -71.520795)
        addMarkerInMap(latLng, "P.t Miraflores Parcela F")
        fetchLocation()
    }

    private fun addMarkerInMap(latLng: LatLng, titleText: String){

        val color = ContextCompat.getColor(globalContext!!,
            android.R.color.holo_red_dark
        )
        val markerMap =  MarkerOptions().position(latLng)
            .title(titleText)
            .icon(bitmapDescriptorFromVector(globalContext!!,
                R.drawable.ic_location, color)).draggable(true)
        mMap.clear()
        mMap.addMarker(markerMap)
        mMap.moveCamera(newLatLngZoom(latLng, 18.0f))
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
            override fun onMarkerDrag(marker: Marker) {}

            override fun onMarkerDragEnd(marker: Marker) {
                var lat = marker.position.latitude
                var lon = marker.position.longitude
                textViewLatitude.text = lat.toString()
                textViewLongitude.text = lon.toString()
                client.createOrUpdateAddress.latitude = marker.position.latitude
                client.createOrUpdateAddress.longitude = marker.position.longitude
                Toast.makeText(globalContext, "POSICION FINAL "+"Marker " + marker.getId() + " Draggable" + marker.getPosition(), Toast.LENGTH_SHORT).show()
            }

            override fun onMarkerDragStart(marker: Marker) {}
        })
        mMap.setOnMapClickListener { nLatLng ->
            mMap.clear()
            mMap.addMarker(
                MarkerOptions()
                    .position(nLatLng)
                    .title("No hay direccion").draggable(true)
            )
            textViewLatitude.text=nLatLng.latitude.toString()
            textViewLongitude.text=nLatLng.longitude.toString()
            client.createOrUpdateAddress.latitude = nLatLng.latitude
            client.createOrUpdateAddress.longitude = nLatLng.longitude
        }
        textViewLatitude.text = latLng.latitude.toString()
        textViewLongitude.text = latLng.longitude.toString()


    }

}