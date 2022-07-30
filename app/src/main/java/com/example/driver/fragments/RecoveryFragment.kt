package com.example.driver.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.activities.HomeActivity
import com.example.driver.adapter.*
import com.example.driver.model.*
import com.example.driver.rest.ApiResponse
import com.example.driver.retrofit.ClientService
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Double.parseDouble
import java.lang.Integer.parseInt
import java.text.SimpleDateFormat
import java.util.*


class RecoveryFragment : Fragment() {

    lateinit var database: FirebaseDatabase
    private lateinit var driverReference: DatabaseReference
    private lateinit var vehicleReference: DatabaseReference
    private lateinit var distributionReference: DatabaseReference
    private lateinit var paymentMethodReference: DatabaseReference
    private lateinit var dispatchReference: DatabaseReference
    private lateinit var recoveryReference: DatabaseReference

    private var vehicle: Vehicle = Vehicle()
    private var driver : Driver = Driver()
    private var productOwed: Debt.ProductOwed = Debt.ProductOwed()
    private var recoveryDetail: Recovery.RecoveryDetail = Recovery.RecoveryDetail()
    private var recovery: Recovery = Recovery()
    private var payment : Payment = Payment()


    private var vehicleKey: String = ""
    private var driverKey: String = ""
    private var driverID: Int = 0

    private var globalContext: Context? = null

    private lateinit var recyclerViewDebtor: RecyclerView
    private var paymentMethodList: MutableMap<String, Double> = mutableMapOf()
    private var filledStockMap: Map<String, Vehicle.Stock.StockProduct> = mapOf()
    private var voidStockMap: Map<String, Vehicle.Stock.StockProduct> = mapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity
        database = FirebaseDatabase.getInstance()
        driverReference = database.getReference("drivers")
        vehicleReference = database.getReference("vehicles")
        dispatchReference = database.getReference("dispatches")
        recoveryReference = database.getReference("recoveries")
        distributionReference = database.getReference("distributions")
        paymentMethodReference = database.getReference("paymentMethods")

        val bundle = arguments
        vehicleKey = bundle!!.getString("vehicleKey").toString()
        driverKey = bundle.getString("driverKey").toString()
        driverID = bundle.getInt("driverID")

        getFilledAndVoidStock(vehicleKey)
        getDriver(driverKey)
        loadRestDebtors(driverID)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recovery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewDebtor = view.findViewById(R.id.recyclerViewDebtor)


    }

    private fun loadRestDebtors(id: Int) {

        val apiInterface = ClientService.create().getDebtorsWithDebt(id)
        apiInterface.enqueue(object : Callback<ArrayList<Debt>> {
            override fun onResponse(
                call: Call<ArrayList<Debt>>,
                response: Response<ArrayList<Debt>>
            ) {
                var list = arrayListOf<Debt>()

                if (response.body() != null) {

                    list = response.body()!!

                    recyclerViewDebtor.layoutManager = LinearLayoutManager(globalContext)
                    recyclerViewDebtor.setHasFixedSize(true)
                    recyclerViewDebtor.adapter= DebtorAdapter(list, object : DebtorAdapter.OnItemClickListener {
                        override fun onItemClick(model: Debt) {
                            openModal(model)
                            recovery.clientID = model.id!!
                            recovery.clientName = model.names

                        }
                    })

                }
            }

            override fun onFailure(call: Call<ArrayList<Debt>>, t: Throwable) {
                Log.d("MIKE", "loadRestDebtors. Algo salio mal..." + t!!.message.toString())
            }
        })
    }



    private fun openModal(d: Debt) {
        val inflater = LayoutInflater.from(globalContext)
        val v = inflater.inflate(R.layout.debt_detail, null)
        val recyclerViewDebtDetail = v.findViewById<RecyclerView>(R.id.recyclerViewDebtDetail)
        val chipGroupConceptRecovery = v.findViewById<ChipGroup>(R.id.chipGroupConceptRecovery)
        val chipConceptRecovery1 = v.findViewById<Chip>(R.id.chipConceptRecovery1)
        val chipConceptRecovery2 = v.findViewById<Chip>(R.id.chipConceptRecovery2)
        val editTextRecoveryPrice = v.findViewById<TextInputEditText>(R.id.editTextRecoveryPrice)
        val editTextRecoveryQuantity = v.findViewById<TextInputEditText>(R.id.editTextRecoveryQuantity)
        val editTextRecoverySubtotal = v.findViewById<TextInputEditText>(R.id.editTextRecoverySubtotal)
        val buttonSaveRecovery = v.findViewById<Button>(R.id.buttonSaveRecovery)
        val textViewModalityRecovery = v.findViewById<TextView>(R.id.textViewModalityRecovery)
        val textViewSelectedProductName = v.findViewById<TextView>(R.id.textViewSelectedProductName)
        val textViewSelectedUnitName = v.findViewById<TextView>(R.id.textViewSelectedUnitName)
        val textViewTitleProductRecovery = v.findViewById<TextView>(R.id.textViewTitleProductRecovery)
        val boxRecoveryMethodName = v.findViewById<TextInputLayout>(R.id.boxRecoveryMethodName)
        val boxRecoveryPrice = v.findViewById<TextInputLayout>(R.id.boxRecoveryPrice)
        val boxRecoveryQuantity = v.findViewById<TextInputLayout>(R.id.boxRecoveryQuantity)
        val boxRecoverySubtotal = v.findViewById<TextInputLayout>(R.id.boxRecoverySubtotal)
        val autoCompleteRecoveryMethodName = v.findViewById<AutoCompleteTextView>(R.id.autoCompleteRecoveryMethodName)

        textViewModalityRecovery.visibility = View.GONE
        chipGroupConceptRecovery.visibility = View.GONE
        textViewTitleProductRecovery.visibility = View.GONE
        textViewSelectedProductName.visibility = View.GONE
        textViewSelectedUnitName.visibility = View.GONE
        boxRecoveryQuantity.visibility = View.GONE
        boxRecoveryMethodName.visibility = View.GONE
        boxRecoveryPrice.visibility = View.GONE
        boxRecoverySubtotal.visibility = View.GONE
        buttonSaveRecovery.visibility = View.GONE

        val listMethod = listOf("EFECTIVO", "YAPE", "PLIN")
        val adapter = ArrayAdapter(
            globalContext!!,
            android.R.layout.simple_spinner_dropdown_item,
            listMethod
        )
        autoCompleteRecoveryMethodName.keyListener = null
        autoCompleteRecoveryMethodName.setAdapter(adapter)
        autoCompleteRecoveryMethodName.setText(
            autoCompleteRecoveryMethodName.adapter.getItem(0).toString(),
            false
        )


        chipGroupConceptRecovery.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipConceptRecovery1 -> {
                    boxRecoveryMethodName.visibility = View.VISIBLE
                    boxRecoveryPrice.visibility = View.VISIBLE
                    boxRecoverySubtotal.visibility = View.VISIBLE
                    recovery.concept = "03"  // PAYED
                }
                R.id.chipConceptRecovery2 -> {
                    boxRecoveryMethodName.visibility = View.GONE
                    boxRecoveryPrice.visibility = View.GONE
                    boxRecoverySubtotal.visibility = View.GONE
                    recovery.concept = "02"  // RECOVERY
                }
            }
        }

        editTextRecoveryQuantity.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(cs: CharSequence, s: Int, b: Int, c: Int) {
                if (cs.count() > 0 && editTextRecoveryPrice.text.toString().count() > 0){
                    val recoverySubtotal = recalculate(editTextRecoveryPrice.text.toString(), editTextRecoveryQuantity.text.toString())
                    editTextRecoverySubtotal.setText(recoverySubtotal.toString())
                }
            }
            override fun afterTextChanged(editable: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, i: Int, j: Int, k: Int) {}

        })

        editTextRecoveryPrice.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(cs: CharSequence, s: Int, b: Int, c: Int) {
                if (cs.count() > 0 && editTextRecoveryQuantity.text.toString().count() > 0){
                    val recoverySubtotal = recalculate(editTextRecoveryPrice.text.toString(), editTextRecoveryQuantity.text.toString())
                    editTextRecoverySubtotal.setText(recoverySubtotal.toString())
                }
            }
            override fun afterTextChanged(editable: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, i: Int, j: Int, k: Int) {}

        })

        recyclerViewDebtDetail.layoutManager = LinearLayoutManager(activity)
        recyclerViewDebtDetail.setHasFixedSize(true)
        recyclerViewDebtDetail.adapter = DebtDetailAdapter(d.debt, object : DebtDetailAdapter.OnItemClickListener {
            override fun onItemClick(model: Debt.ProductOwed, unitID: Int) {

                textViewModalityRecovery.visibility = View.VISIBLE
                chipGroupConceptRecovery.visibility = View.VISIBLE

                textViewTitleProductRecovery.visibility = View.VISIBLE
                textViewSelectedProductName.visibility = View.VISIBLE
                textViewSelectedUnitName.visibility = View.VISIBLE

                boxRecoveryQuantity.visibility = View.VISIBLE
                boxRecoveryMethodName.visibility = View.VISIBLE
                boxRecoveryPrice.visibility = View.VISIBLE
                boxRecoverySubtotal.visibility = View.VISIBLE
                buttonSaveRecovery.visibility = View.VISIBLE

                productOwed = model

                val recoveryPrice = productOwed.units[unitID]!!.unitPrice
                val recoveryQuantity = productOwed.units[unitID]!!.remainingQuantity
                val recoverySubtotal = recoveryPrice * recoveryQuantity

                editTextRecoveryPrice.setText(recoveryPrice.toString())
                editTextRecoveryQuantity.setText(recoveryQuantity.toString())
                editTextRecoverySubtotal.setText(recoverySubtotal.toString())

                chipConceptRecovery1.isChecked = true

                when (unitID){
                    1 -> {
                        textViewSelectedUnitName.text = "COMPLETO"
                        chipConceptRecovery2.isEnabled = false
                    }
                    2 -> {
                        textViewSelectedUnitName.text = "FIERRO"
                        chipConceptRecovery2.isEnabled = true
                    }
                    3 -> {
                        textViewSelectedUnitName.text = "LIQUIDO"
                        chipConceptRecovery2.isEnabled = false
                    }
                }

                textViewSelectedProductName.text = productOwed.productName
                buttonSaveRecovery.isEnabled = true

                recoveryDetail.productID = model.productID
                recoveryDetail.productKey = model.productKey
                recoveryDetail.productName = model.productName
                recoveryDetail.unitID = unitID
                recoveryDetail.quantity = recoveryQuantity
                recoveryDetail.price = recoveryPrice
                recoveryDetail.subtotal = recoverySubtotal

            }
        })

        val addDialog = AlertDialog.Builder(globalContext)
        addDialog.setView(v)
//        addDialog.create()
//        addDialog.show()

        val dialog: AlertDialog = addDialog.create()
        dialog.show()

        buttonSaveRecovery.setOnClickListener{


            if (recovery.concept == "03"){
                if (editTextRecoveryPrice.text.toString().toDouble() > 0){

                    addPaymentMethod(autoCompleteRecoveryMethodName.text.toString(), editTextRecoveryPrice.text.toString().toDouble())

                    if(editTextRecoveryQuantity.text.toString().toInt() > 0 && editTextRecoveryQuantity.text.toString().toInt() <= recoveryDetail.quantity ){
                        saveRecovery()
                        saveFirebasePaymentMethod()
                        dialog.dismiss()
                    }
                }
            }else if (recovery.concept == "02"){
                if(editTextRecoveryQuantity.text.toString().toInt() > 0 && editTextRecoveryQuantity.text.toString().toInt() <= recoveryDetail.quantity){
                    saveRecovery()
                    increaseFirebaseVoidStock()
                    dialog.dismiss()
                }
            }

            val v2 = View.inflate(globalContext, R.layout.dialog_view, null)
            val buttonConfirm = v2.findViewById<Button>(R.id.buttonConfirm)

            val builder2 = AlertDialog.Builder(globalContext)
            builder2.setView(v2)

            val confirmDialog = builder2.create()
            confirmDialog.show()
            confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            confirmDialog.setCancelable(false)

            buttonConfirm.setOnClickListener {
                (activity as HomeActivity).goToRecoveryFragment()
                confirmDialog.dismiss()
            }


        }

    }

    private fun recalculate(a: String, b: String): Double{
        var numericA = true
        var numericB = true
        var numA: Double = 0.0
        var numB: Int = 0
        var numC: Double = 0.0

        try {
            numA = parseDouble(a)
        } catch (e: NumberFormatException) {
            numericA = false
        }

        try {
            numB = parseInt(b)
        } catch (e: NumberFormatException) {
            numericB = false
        }

        if (!numericA){
            Toast.makeText(globalContext, "Verificar Precio", Toast.LENGTH_LONG).show()
        }
        else if (!numericB){
            Toast.makeText(globalContext, "Verificar Cantidad", Toast.LENGTH_LONG).show()
        }else if (numericA && numericB){
            numC = numA * numB
            recoveryDetail.quantity = numB
            recoveryDetail.price = numA
            recoveryDetail.subtotal = numC
        }
        return numC
    }

    private fun addPaymentMethod(value: String, amount: Double) {

        var selectedItem = "cash"
        when (value) {
            "EFECTIVO" -> {selectedItem = "cash"}
            "YAPE" -> {selectedItem = "yape"}
            "PLIN" -> {selectedItem = "plin"}
        }
        paymentMethodList[selectedItem] = amount
        recovery.paymentMethods = paymentMethodList
        recovery.totalPrice = amount
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveRecovery(){
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDate = sdf.format(Date())
        recovery.recoveryDate = currentDate
        recovery.details.add(recoveryDetail)
        recovery.uid = recoveryReference.push().key!!
        recoveryReference.child(recovery.uid).setValue(recovery)
        saveRestRecovery()
    }

    private fun saveRestRecovery(){
        val apiInterface = ClientService.create().registerRecoveryFromCreditor(recovery)
        apiInterface.enqueue(object : Callback<ApiResponse>{
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                val responseRecovery = response.body()!!
                Log.d("MIKE", responseRecovery.status)
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.d("MIKE", "registerRecoveryFromCreditor onFailure: " + t.message.toString())
            }
        })
    }

    private fun increaseFirebaseVoidStock(){

        val voidRef = vehicleReference.child(vehicleKey).child("stock").child("regular").child("void")

        recovery.details.forEach {

            var voidProduct : Vehicle.Stock.StockProduct = Vehicle.Stock.StockProduct()

            val voidProductExists = voidStockMap.filter { (key, _) -> key == it.productKey }

            if(voidProductExists.isEmpty()){
                voidProduct.productKey = it.productKey
                voidProduct.productName = filledStockMap[it.productKey]!!.productName
                voidProduct.productPath = filledStockMap[it.productKey]!!.productPath
                voidProduct.quantity = it.quantity
                voidProduct.unit = "B"
                voidRef.child(it.productKey).setValue(voidProduct)
            }
            else{
                voidProduct = voidStockMap[it.productKey]!!
                voidRef.child(it.productKey).child("quantity").setValue(voidProduct.quantity + it.quantity)
            }
        }
    }

    private fun saveFirebasePaymentMethod(){
        val paymentRef = paymentMethodReference.child("distributions").child(recovery.distributionKey)

        val accumulatedDistYapeExists = payment.wayPays.filter { (key, _) -> key == "yape" }
        val accumulatedDistPlinExists = payment.wayPays.filter { (key, _) -> key == "plin" }
        val accumulatedDistCashExists = payment.wayPays.filter { (key, _) -> key == "cash" }
        val accumulatedDistCreditExists = payment.wayPays.filter { (key, _) -> key == "credit" }
        var accumulatedDistYape = 0.0
        var accumulatedDistPlin = 0.0
        var accumulatedDistCash = 0.0
        var accumulatedDistCredit = 0.0
        if (accumulatedDistYapeExists.isNotEmpty()){accumulatedDistYape= payment.wayPays["yape"]!!}
        if (accumulatedDistPlinExists.isNotEmpty()){accumulatedDistPlin= payment.wayPays["plin"]!!}
        if (accumulatedDistCashExists.isNotEmpty()){accumulatedDistCash= payment.wayPays["cash"]!!}
        if (accumulatedDistCreditExists.isNotEmpty()){accumulatedDistCredit= payment.wayPays["credit"]!!}

        paymentMethodList.forEach { (key, value) ->

            when(key){
                "yape" -> {paymentRef.child("wayPays").child(key).setValue(accumulatedDistYape + value)}
                "plin" -> {paymentRef.child("wayPays").child(key).setValue(accumulatedDistPlin + value)}
                "cash" -> {paymentRef.child("wayPays").child(key).setValue(accumulatedDistCash + value)}
                "credit" -> {paymentRef.child("wayPays").child(key).setValue(accumulatedDistCredit + value)}
            }

            paymentRef.child("total").setValue(payment.total + value)
        }

        paymentRef.child("recoveries").child(recovery.uid).child("clientName").setValue(recovery.clientName)
        paymentRef.child("recoveries").child(recovery.uid).child("recoveryDate").setValue(recovery.recoveryDate)
        paymentRef.child("recoveries").child(recovery.uid).child("total").setValue(recovery.totalPrice)
        paymentRef.child("recoveries").child(recovery.uid).child("amounts").setValue(recovery.paymentMethods)

    }

    private fun getFilledAndVoidStock(vehicleKey: String = "") {

        val vehicleRef = vehicleReference.orderByKey().equalTo(vehicleKey)
        vehicleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    for (vehicleSnapshot in snapshot.children) {
                        vehicle = vehicleSnapshot.getValue(Vehicle::class.java)!!
                    }

                    val filledMap = vehicle.stock.regular.filled
                    filledStockMap = filledMap.filter { (_, value) -> value.quantity > 0 }

                    val voidMap = vehicle.stock.regular.void
                    voidStockMap = voidMap.filter { (_, value) -> value.quantity >= 0 }

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
                    recovery.driverID = driver.driverID
                    recovery.distributionID = driver.distributionID
                    recovery.distributionKey = driver.distributionKey
                    getDistributionPayment(recovery.distributionKey)
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