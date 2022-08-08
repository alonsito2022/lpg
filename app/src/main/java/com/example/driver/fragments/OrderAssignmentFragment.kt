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
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.DatePickerFragment
import com.example.driver.R
import com.example.driver.activities.HomeActivity
import com.example.driver.adapter.DispatchDetailAdapter
import com.example.driver.adapter.DispatchPlacedAdapter
import com.example.driver.adapter.MethodPaymentAdapter
import com.example.driver.adapter.SaleProductAdapter
import com.example.driver.model.*
import com.example.driver.rest.ApiResponse
import com.example.driver.retrofit.ClientService
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class OrderAssignmentFragment : Fragment() {

    private var globalContext: Context? = null

    lateinit var database: FirebaseDatabase
    private lateinit var dispatchReference: DatabaseReference
    private lateinit var distributionReference: DatabaseReference
    private lateinit var vehicleReference: DatabaseReference
    private lateinit var paymentMethodReference: DatabaseReference
    private lateinit var driverReference: DatabaseReference

//    private var filteredFilledMap: Map<String, Vehicle.Stock.StockProduct> = mapOf()
//    private var filteredVoidMap: Map<String, Vehicle.Stock.StockProduct> = mapOf()
    private var filteredFilledMap: MutableList<Driver.Vehicle.StockRegular> = mutableListOf()
    private var filteredVoidMap: MutableList<Driver.Vehicle.StockRegular> = mutableListOf()

    private var dispatch: Dispatch = Dispatch()
    private var vehicle: Vehicle = Vehicle()
    private var driver : Driver = Driver()
    private var payment : Payment = Payment()
    private var distribution: Distribution = Distribution()

    private var vehicleKey: String = ""
    private var driverKey: String = ""
    private var driverID: Int = 0
    private var distributionID: Int = 0

    private lateinit var recyclerViewOrderAssignment: RecyclerView
    private lateinit var recyclerViewOrderOutForDelivery: RecyclerView
    private lateinit var recyclerViewOrderCompleted: RecyclerView
    private lateinit var recyclerViewOrderAnnulled: RecyclerView

    private lateinit var editTextSearchDate: TextInputEditText
    private lateinit var btnSearch: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity
        database = FirebaseDatabase.getInstance()
        dispatchReference = database.getReference("dispatches")
        distributionReference = database.getReference("distributions")
        vehicleReference = database.getReference("vehicles")
        paymentMethodReference = database.getReference("paymentMethods")
        driverReference = database.getReference("drivers")

        val bundle = arguments
        vehicleKey = bundle!!.getString("vehicleKey").toString()
        driverKey = bundle.getString("driverKey").toString()
        driverID = bundle.getInt("driverID")
        distributionID = bundle.getInt("distributionID")

        dispatch.driverID = bundle!!.getInt("driverID")
        getDriver(dispatch.driverID)

        // getStock(vehicleKey)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_assignment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewOrderAssignment = view.findViewById(R.id.recyclerViewOrderAssignment)
        recyclerViewOrderOutForDelivery = view.findViewById(R.id.recyclerViewOrderOutForDelivery)
        recyclerViewOrderCompleted = view.findViewById(R.id.recyclerViewOrderCompleted)
        recyclerViewOrderAnnulled = view.findViewById(R.id.recyclerViewOrderAnnulled)

        editTextSearchDate = view.findViewById(R.id.editTextSearchDate)
        val sdf2 = SimpleDateFormat("dd/MM/yyyy").format(Date())
        val sdf3 = SimpleDateFormat("yyyy-MM-dd").format(Date())
        dispatch.dispatchDate = sdf3
        editTextSearchDate.setText(sdf2)

        editTextSearchDate.setOnClickListener { showDatePickerDialog() }

        btnSearch = view.findViewById(R.id.btnSearch)
        btnSearch.setOnClickListener{
            if(editTextSearchDate.text.toString() != ""){
                loadDispatchesAssignment()
                loadDispatchesOutForDelivery()
                loadDispatchesCompleted()
                loadDispatchesAnnulled()
            }

            else
                Toast.makeText(globalContext, "Elija fecha.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun showDatePickerDialog(){
        val fm: FragmentManager = (activity as AppCompatActivity?)!!.supportFragmentManager
        val datePicker = DatePickerFragment {day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(fm, "datePicker")
    }

    @SuppressLint("SimpleDateFormat")
    private fun onDateSelected(day:Int, month:Int, year:Int){
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val sdf2 = SimpleDateFormat("dd/MM/yyyy").format(calendar.time)
        val sdf3 = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
        dispatch.dispatchDate = sdf3
        editTextSearchDate.setText(sdf2)
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

        var buttonTextOk : String = "Ok"
        when(d.status){
            "05" -> buttonTextOk = "ACEPTAR"
            "04" -> buttonTextOk = "FINALIZAR"
        }

        addDialog.setView(v)
        addDialog.setPositiveButton(buttonTextOk) { dialog, id ->

            when(d.status){
                "05" -> {  // ASSIGNMENT -> VALID STOCK | UPDATE DISPATCH

                    if(validateStock(d)){
                        d.status = "04"
//                        d.distributionID = driver.distributionID
                        updateRestDispatch(d)
                        btnSearch.callOnClick()
//                        val newIdentifier = "${d.identifier.subSequence(0,10)}04"
//                        dispatchReference.child(d.uid).child("distributionID").setValue(driver.distributionID)
//                        dispatchReference.child(d.uid).child("distributionKey").setValue(driver.distributionKey)
//                        dispatchReference.child(d.uid).child("identifier").setValue(newIdentifier)
//                        dispatchReference.child(d.uid).child("status").setValue("04")
                        Toast.makeText(globalContext, "Pedido en ruta", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(globalContext, "Stock insuficiente", Toast.LENGTH_SHORT).show()
                    }

                }
                "04" -> {  // EN RUTA -> DECREASE STOCK
                    if(validateStock(d)){
                        d.status = "02"
//                        d.dispatchType = "01"
//                        d.distributionID = distributionID
                        updateRestDispatch(d)
                        btnSearch.callOnClick()
//                        val newIdentifier = "${d.identifier.subSequence(0,8)}0102"

                        // Firebase update
//                        dispatchReference.child(d.uid).child("identifier").setValue(newIdentifier)
//                        dispatchReference.child(d.uid).child("dispatchType").setValue("01")
//                        dispatchReference.child(d.uid).child("status").setValue("02")

//                        updateFirebasePaymentMethod(d)
//                        decreaseFirebaseFilledStock(d)

                        Toast.makeText(globalContext, "Pedido completado", Toast.LENGTH_SHORT).show()
//                        (activity as HomeActivity).goToOrderPlacedFragment()
                    }
                    else{
                        Toast.makeText(globalContext, "Stock insuficiente", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            dialog.dismiss()
        }
        addDialog.setNegativeButton("RECHAZAR") { dialog, id ->
            when(d.status){
                "05" -> {
                    d.status = "03"
                    updateRestDispatch(d)
                    btnSearch.callOnClick()
//                    val newIdentifier = "${d.identifier.subSequence(0,10)}03"
//                    dispatchReference.child(d.uid).child("identifier").setValue(newIdentifier)
//                    dispatchReference.child(d.uid).child("status").setValue("03")
                    Toast.makeText(globalContext, "Pedido cancelado", Toast.LENGTH_SHORT).show()

                }
                "04" -> {
                    d.status = "03"
                    updateRestDispatch(d)
                    btnSearch.callOnClick()
//                    val newIdentifier = "${d.identifier.subSequence(0,10)}03"
//                    dispatchReference.child(d.uid).child("identifier").setValue(newIdentifier)
//                    dispatchReference.child(d.uid).child("status").setValue("03")
                    Toast.makeText(globalContext, "Pedido cancelado", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.dismiss()
        }
        addDialog.create()
        addDialog.show()
    }

    private fun validateStock(d: Dispatch): Boolean{
        var isValid: Boolean = true


        d.details.forEach { detail ->
            // && item.filledStock >= it.quantity

            val filledProductExists = driver.vehicle.stockRegular.filter { item2 -> item2.productID == detail.productID && item2.filledStock >= detail.quantity }
            // Log.d("MIKE", "detail productID: ${detail.productID}, ${detail.quantity}")

            if(filledProductExists.isEmpty()){
                isValid = false
            }
        }
        return isValid
    }


    private fun updateRestDispatch(d: Dispatch){

        val apiInterface = ClientService.create().updateDispatch(d)
        apiInterface.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                Log.d("MIKE", response.isSuccessful.toString())
                val responseDispatchDetail = response.body()!!
                Log.d("MIKE", responseDispatchDetail.status)
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.d("MIKE", "updateRestDispatch onFailure: " + t.message.toString())
            }

        })
    }

    private fun updateFirebasePaymentMethod(d: Dispatch){
        val paymentRef = paymentMethodReference.child("distributions").child(driver.distributionKey)

        val accumulatedDistYapeExists = payment.wayPays.filter { (key, _) -> key == "yape" }
        val accumulatedDistPlinExists = payment.wayPays.filter { (key, _) -> key == "plin" }
        val accumulatedDistCashExists = payment.wayPays.filter { (key, _) -> key == "cash" }
        var accumulatedDistYape = 0.0
        var accumulatedDistPlin = 0.0
        var accumulatedDistCash = 0.0
        if (accumulatedDistYapeExists.isNotEmpty()){accumulatedDistYape= payment.wayPays["yape"]!!}
        if (accumulatedDistPlinExists.isNotEmpty()){accumulatedDistPlin= payment.wayPays["plin"]!!}
        if (accumulatedDistCashExists.isNotEmpty()){accumulatedDistCash= payment.wayPays["cash"]!!}

        d.paymentMethods.forEach { (key, value) ->

            when(key){
                "yape" -> {paymentRef.child("wayPays").child(key).setValue(accumulatedDistYape + value)}
                "plin" -> {paymentRef.child("wayPays").child(key).setValue(accumulatedDistPlin + value)}
                "cash" -> {paymentRef.child("wayPays").child(key).setValue(accumulatedDistCash + value)}
            }

            paymentRef.child("total").setValue(payment.total + value)
        }

        paymentRef.child("dispatches").child(d.uid).child("clientName").setValue(d.clientName)
        paymentRef.child("dispatches").child(d.uid).child("dispatchDate").setValue(d.dispatchDate)
        paymentRef.child("dispatches").child(d.uid).child("total").setValue(d.totalPrice)
        paymentRef.child("dispatches").child(d.uid).child("amounts").setValue(d.paymentMethods)

    }

    private fun decreaseFirebaseFilledStock(d: Dispatch){

        val filledRef = vehicleReference.child(vehicleKey).child("stock").child("regular").child("filled")
        val voidRef = vehicleReference.child(vehicleKey).child("stock").child("regular").child("void")

        /*d.details.forEach {

            filledRef.child(it.productKey).child("quantity").setValue(filteredFilledMap[it.productKey]!!.quantity - it.quantity)

            when (it.returnability) {
                "R" -> {
                    var voidProduct : Vehicle.Stock.StockProduct = Vehicle.Stock.StockProduct()

                    val voidProductExists = filteredVoidMap.filter { (key, _) -> key == it.productKey }

                    if(voidProductExists.isEmpty()){
                        voidProduct.productKey = it.productKey
                        voidProduct.productName = filteredFilledMap[it.productKey]!!.productName
                        voidProduct.productPath = filteredFilledMap[it.productKey]!!.productPath
                        voidProduct.quantity = it.quantity
                        voidProduct.unit = "B"
                        voidRef.child(it.productKey).setValue(voidProduct)
                    }
                    else{
                        voidProduct = filteredVoidMap[it.productKey]!!
                        voidRef.child(it.productKey).child("quantity").setValue(voidProduct.quantity + it.quantity)
                    }

                }
            }
        }*/
    }

    private fun loadDispatchesAssignment() {
        dispatch.dispatchType = "04"
        dispatch.status = "05"

        val apiInterface = ClientService.create().getDispatchesByDate(dispatch)
        apiInterface.enqueue(object : Callback<ArrayList<Dispatch>> {
            override fun onResponse(
                call: Call<ArrayList<Dispatch>>,
                response: Response<ArrayList<Dispatch>>
            ) {
                var listDispatches = arrayListOf<Dispatch>()
                if (response.body() != null) {
                    listDispatches = response.body()!!
                    recyclerViewOrderAssignment.layoutManager = LinearLayoutManager(activity)
                    recyclerViewOrderAssignment.setHasFixedSize(true)
                    recyclerViewOrderAssignment.adapter = DispatchPlacedAdapter(
                        globalContext!!,
                        listDispatches,
                        object : DispatchPlacedAdapter.OnItemClickListener {
                            override fun onItemClick(model: Dispatch) {
                                addInfo(model)
                            }
                        }
                    )
                }
            }
            override fun onFailure(call: Call<ArrayList<Dispatch>>, t: Throwable) {
                Log.d("MIKE", "loadCashFlows. Algo salio mal..." + t.message.toString())
            }
        })

    }

    private fun loadDispatchesOutForDelivery() {
        dispatch.dispatchType = "04"
        dispatch.status = "04"

        val apiInterface = ClientService.create().getDispatchesByDate(dispatch)
        apiInterface.enqueue(object : Callback<ArrayList<Dispatch>> {
            override fun onResponse(
                call: Call<ArrayList<Dispatch>>,
                response: Response<ArrayList<Dispatch>>
            ) {
                var listDispatches = arrayListOf<Dispatch>()
                if (response.body() != null) {
                    listDispatches = response.body()!!
                    recyclerViewOrderOutForDelivery.layoutManager = LinearLayoutManager(activity)
                    recyclerViewOrderOutForDelivery.setHasFixedSize(true)
                    recyclerViewOrderOutForDelivery.adapter = DispatchPlacedAdapter(
                        globalContext!!,
                        listDispatches,
                        object : DispatchPlacedAdapter.OnItemClickListener {
                            override fun onItemClick(model: Dispatch) {
                                addInfo(model)
                            }
                        }
                    )
                }
            }
            override fun onFailure(call: Call<ArrayList<Dispatch>>, t: Throwable) {
                Log.d("MIKE", "loadCashFlows. Algo salio mal..." + t.message.toString())
            }
        })

    }

    private fun loadDispatchesCompleted() {

        dispatch.dispatchType = "04"
        dispatch.status = "02"

        val apiInterface = ClientService.create().getDispatchesByDate(dispatch)
        apiInterface.enqueue(object : Callback<ArrayList<Dispatch>> {
            override fun onResponse(
                call: Call<ArrayList<Dispatch>>,
                response: Response<ArrayList<Dispatch>>
            ) {
                var listDispatches = arrayListOf<Dispatch>()
                if (response.body() != null) {
                    listDispatches = response.body()!!
                    recyclerViewOrderCompleted.layoutManager = LinearLayoutManager(activity)
                    recyclerViewOrderCompleted.setHasFixedSize(true)
                    recyclerViewOrderCompleted.adapter = DispatchPlacedAdapter(
                        globalContext!!,
                        listDispatches,
                        object : DispatchPlacedAdapter.OnItemClickListener {
                            override fun onItemClick(model: Dispatch) {
                                addInfo(model)
                            }
                        }
                    )
                }
            }
            override fun onFailure(call: Call<ArrayList<Dispatch>>, t: Throwable) {
                Log.d("MIKE", "loadCashFlows. Algo salio mal..." + t.message.toString())
            }
        })


    }

    private fun loadDispatchesAnnulled() {

        dispatch.dispatchType = "04"
        dispatch.status = "03"

        val apiInterface = ClientService.create().getDispatchesByDate(dispatch)
        apiInterface.enqueue(object : Callback<ArrayList<Dispatch>> {
            override fun onResponse(
                call: Call<ArrayList<Dispatch>>,
                response: Response<ArrayList<Dispatch>>
            ) {
                var listDispatches = arrayListOf<Dispatch>()
                if (response.body() != null) {
                    listDispatches = response.body()!!
                    recyclerViewOrderAnnulled.layoutManager = LinearLayoutManager(activity)
                    recyclerViewOrderAnnulled.setHasFixedSize(true)
                    recyclerViewOrderAnnulled.adapter = DispatchPlacedAdapter(
                        globalContext!!,
                        listDispatches,
                        object : DispatchPlacedAdapter.OnItemClickListener {
                            override fun onItemClick(model: Dispatch) {
                                addInfo(model)
                            }
                        }
                    )
                }
            }
            override fun onFailure(call: Call<ArrayList<Dispatch>>, t: Throwable) {
                Log.d("MIKE", "loadCashFlows. Algo salio mal..." + t.message.toString())
            }
        })

    }

    /*private fun getStock(vehicleKey: String = ""){

        val vehicleRef = vehicleReference.orderByKey().equalTo(vehicleKey)
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

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }

        })
    }*/


    private fun getDriver(id: Int = 0){

        val apiInterface = ClientService.create().searchDriverByID(Driver(id))
        apiInterface.enqueue(object : Callback<Driver>{
            override fun onResponse(call: Call<Driver>, response: Response<Driver>) {
                if (response.body() != null) {
                    driver = response.body()!!

                    // val filterProductArrayList = driver.vehicle.stockRegular as MutableList<Driver.Vehicle.StockRegular>

                    // btnRegisterDispatch.isEnabled = driver.vehicle.distribution.distributionStatus == "P"
                }
            }

            override fun onFailure(call: Call<Driver>, t: Throwable) {
                Log.d("MIKE", "getDriver onFailure: " + t.message.toString())
            }

        })

    }

    /*private fun getDriver(driverKey: String = ""){

        val driverRef = driverReference.orderByKey().equalTo(driverKey)
        driverRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (driverSnapshot in snapshot.children){
                    driver = driverSnapshot.getValue(Driver::class.java)!!
                }

                if(driver.distributionKey.isNotEmpty()){
                    getDistribution(driver.distributionKey)
                    getDistributionPayment(driver.distributionKey)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }
        })
    }*/

    /*private fun getDistribution(distributionKey: String = ""){

        val distributionRef = distributionReference.orderByKey().equalTo(distributionKey)
        distributionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (distributionSnapshot in snapshot.children){
                    distribution = distributionSnapshot.getValue(Distribution::class.java)!!
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }
        })
    }*/

    /*private fun getDistributionPayment(distributionKey: String = ""){

        val distributionPaymentRef = paymentMethodReference.child("distributions").child(distributionKey)
        distributionPaymentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.value !== null){
                    payment = snapshot.getValue(Payment::class.java)!!

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }
        })
    }*/


}