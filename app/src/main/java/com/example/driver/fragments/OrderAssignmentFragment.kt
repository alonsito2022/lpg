package com.example.driver.fragments

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
import com.example.driver.model.*
import com.example.driver.rest.ApiResponse
import com.example.driver.retrofit.ClientService
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

    private var filteredFilledMap: Map<String, Vehicle.Stock.StockProduct> = mapOf()
    private var filteredVoidMap: Map<String, Vehicle.Stock.StockProduct> = mapOf()

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

        getStock(vehicleKey)
        getDriver(driverKey)

        loadDispatchesAssignment()
        loadDispatchesOutForDelivery()
        loadDispatchesCompleted()
        loadDispatchesAnnulled()
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
                        d.distributionID = driver.distributionID
                        updateRestDispatch(d)
                        val newIdentifier = "${d.identifier.subSequence(0,10)}04"
                        dispatchReference.child(d.uid).child("distributionID").setValue(driver.distributionID)
                        dispatchReference.child(d.uid).child("distributionKey").setValue(driver.distributionKey)
                        dispatchReference.child(d.uid).child("identifier").setValue(newIdentifier)
                        dispatchReference.child(d.uid).child("status").setValue("04")
                        Toast.makeText(globalContext, "Pedido en ruta", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(globalContext, "Stock insuficiente", Toast.LENGTH_SHORT).show()
                    }

                }
                "04" -> {  // EN RUTA -> DECREASE STOCK
                    if(validateStock(d)){
                        d.status = "02"
                        d.dispatchType = "01"
                        d.distributionID = distributionID
                        updateRestDispatch(d)
                        val newIdentifier = "${d.identifier.subSequence(0,8)}0102"

                        // Firebase update
                        dispatchReference.child(d.uid).child("identifier").setValue(newIdentifier)
                        dispatchReference.child(d.uid).child("dispatchType").setValue("01")
                        dispatchReference.child(d.uid).child("status").setValue("02")

                        updateFirebasePaymentMethod(d)
                        decreaseFirebaseFilledStock(d)

                        Toast.makeText(globalContext, "Pedido completado", Toast.LENGTH_SHORT).show()
                        (activity as HomeActivity).goToOrderPlacedFragment()
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
                    val newIdentifier = "${d.identifier.subSequence(0,10)}03"
                    dispatchReference.child(d.uid).child("identifier").setValue(newIdentifier)
                    dispatchReference.child(d.uid).child("status").setValue("03")
                    Toast.makeText(globalContext, "Pedido cancelado", Toast.LENGTH_SHORT).show()

                }
                "04" -> {
                    d.status = "03"
                    updateRestDispatch(d)
                    val newIdentifier = "${d.identifier.subSequence(0,10)}03"
                    dispatchReference.child(d.uid).child("identifier").setValue(newIdentifier)
                    dispatchReference.child(d.uid).child("status").setValue("03")
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
        d.details.forEach {
            val filledProductExists = filteredFilledMap.filter { (_, value) -> value.productKey == it.productKey && value.quantity >= it.quantity }
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

        d.details.forEach {

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
        }
    }

    private fun loadDispatchesAssignment() {

        val sdf2 = SimpleDateFormat("dd/MM/yyyy")
        val currentDate2 = sdf2.format(Date())
        val identifier = currentDate2.replace("/", "") + "0405"

        dispatchReference.orderByChild("identifier").equalTo(identifier).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productArrayList = ArrayList<Dispatch>()

                for (productSnapshot in snapshot.children) {
                    val dispatchTemp = productSnapshot.getValue(Dispatch::class.java)!!
                    productArrayList.add(dispatchTemp)
                }
                val filterProductArrayList = productArrayList.filter { it -> it.driverID == driverID }
                Log.d("MIKE", "loadDispatchesAssignment: ${filterProductArrayList.size}")
                recyclerViewOrderAssignment.layoutManager = LinearLayoutManager(activity)
                recyclerViewOrderAssignment.setHasFixedSize(true)
                recyclerViewOrderAssignment.adapter = DispatchPlacedAdapter(
                    globalContext!!,
                    filterProductArrayList as ArrayList<Dispatch>,
                    object : DispatchPlacedAdapter.OnItemClickListener {
                        override fun onItemClick(model: Dispatch) {
                            addInfo(model)
                        }
                    }
                )


            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }

        })
    }

    private fun loadDispatchesOutForDelivery() {

        val sdf2 = SimpleDateFormat("dd/MM/yyyy")
        val currentDate2 = sdf2.format(Date())
        val identifier = currentDate2.replace("/", "") + "0404"

        dispatchReference.orderByChild("identifier").equalTo(identifier).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productArrayList = ArrayList<Dispatch>()

                for (productSnapshot in snapshot.children) {
                    val dispatchTemp = productSnapshot.getValue(Dispatch::class.java)!!
                    productArrayList.add(dispatchTemp)
                }
                val filterProductArrayList = productArrayList.filter { it -> it.driverID == driverID }
                Log.d("MIKE", "loadDispatchesOutForDelivery: ${filterProductArrayList.size}")
                recyclerViewOrderOutForDelivery.layoutManager = LinearLayoutManager(activity)
                recyclerViewOrderOutForDelivery.setHasFixedSize(true)
                recyclerViewOrderOutForDelivery.adapter = DispatchPlacedAdapter(
                    globalContext!!,
                    filterProductArrayList as ArrayList<Dispatch>,
                    object : DispatchPlacedAdapter.OnItemClickListener {
                        override fun onItemClick(model: Dispatch) {
                            addInfo(model)
                        }
                    }
                )


            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }

        })
    }

    private fun loadDispatchesCompleted() {

        val sdf2 = SimpleDateFormat("dd/MM/yyyy")
        val currentDate2 = sdf2.format(Date())
        val identifier = currentDate2.replace("/", "") + "0402"

        dispatchReference.orderByChild("identifier").equalTo(identifier).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productArrayList = ArrayList<Dispatch>()
                if (snapshot.exists()) {
                    for (productSnapshot in snapshot.children) {
                        val dispatchTemp = productSnapshot.getValue(Dispatch::class.java)!!
                        productArrayList.add(dispatchTemp)
                    }
                    val filterProductArrayList = productArrayList.filter { it -> it.driverID == driverID }
                    Log.d("MIKE", "loadDispatchesCompleted: ${filterProductArrayList.size}")
                    recyclerViewOrderCompleted.layoutManager = LinearLayoutManager(activity)
                    recyclerViewOrderCompleted.setHasFixedSize(true)
                    recyclerViewOrderCompleted.adapter = DispatchPlacedAdapter(
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

    private fun loadDispatchesAnnulled() {

        val sdf2 = SimpleDateFormat("dd/MM/yyyy")
        val currentDate2 = sdf2.format(Date())
        val identifier = currentDate2.replace("/", "") + "0403"

        dispatchReference.orderByChild("identifier").equalTo(identifier).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productArrayList = ArrayList<Dispatch>()
                if (snapshot.exists()) {
                    for (productSnapshot in snapshot.children) {
                        val dispatchTemp = productSnapshot.getValue(Dispatch::class.java)!!
                        productArrayList.add(dispatchTemp)
                    }
                    val filterProductArrayList = productArrayList.filter { it -> it.driverID == driverID }
                    Log.d("MIKE", "loadDispatchesCompleted: ${filterProductArrayList.size}")
                    recyclerViewOrderAnnulled.layoutManager = LinearLayoutManager(activity)
                    recyclerViewOrderAnnulled.setHasFixedSize(true)
                    recyclerViewOrderAnnulled.adapter = DispatchPlacedAdapter(
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

    private fun getStock(vehicleKey: String = ""){

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
    }

    private fun getDriver(driverKey: String = ""){

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
    }

    private fun getDistribution(distributionKey: String = ""){

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
    }

    private fun getDistributionPayment(distributionKey: String = ""){

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
    }


}