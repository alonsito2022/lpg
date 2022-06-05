package com.example.driver

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.Activity.HomeActivity
import com.example.driver.adapter.ClientAdapter
import com.example.driver.adapter.ProductAdapter
import com.example.driver.adapter.ProductAdapter.*
import com.example.driver.model.*
import com.example.driver.rest.ApiResponse
import com.example.driver.retrofit.ClientService
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class SaleFragment : Fragment() {

    lateinit var database: FirebaseDatabase
    private lateinit var vehicleReference: DatabaseReference
    private lateinit var driverReference: DatabaseReference
    private lateinit var clientReference: DatabaseReference
    private lateinit var productReference: DatabaseReference
    private lateinit var dispatchReference: DatabaseReference
    private lateinit var distributionReference: DatabaseReference
    private lateinit var creditorReference: DatabaseReference
    private lateinit var paymentMethodReference: DatabaseReference

    private var filledStockMap: Map<String, Vehicle.Stock.StockProduct> = mapOf()
    private var voidStockMap: Map<String, Vehicle.Stock.StockProduct> = mapOf()
    private var dispatchDetailMap: MutableList<Dispatch.DispatchDetail> = mutableListOf()
    private var paymentMethodList: MutableMap<String, Double> = mutableMapOf()

    private var distribution: Distribution = Distribution()
    private var creditor: DriverAccount = DriverAccount()
    private var dispatch: Dispatch = Dispatch()
    private var payment : Payment = Payment()
    private var vehicle: Vehicle = Vehicle()
    private var driver : Driver = Driver()
    private var vehicleKey: String = ""
    private var driverKey: String = ""

    private var globalContext: Context? = null
    private lateinit var btnRegisterDispatch: Button
    private lateinit var btnAddPayment: Button
    private lateinit var layoutListItem: LinearLayout
    private lateinit var layoutPaymentList: LinearLayout
    private lateinit var chipGroupBrand: ChipGroup
    private lateinit var chipGroupModality: ChipGroup

    private lateinit var chipRefill: Chip
    private lateinit var chipFull: Chip
    private lateinit var chipBorrowedG: Chip
    private lateinit var chipBorrowedB: Chip
    private lateinit var chipBorrowedBG: Chip

    private lateinit var chipGroupQuantity: ChipGroup
    private lateinit var textViewSubtotal: TextView
    private lateinit var textViewTotal: TextView
    private lateinit var textViewItems: TextView
    private lateinit var clientAutoCompleteView: MaterialAutoCompleteTextView
    private lateinit var autoCompleteMethodName: AutoCompleteTextView
    private lateinit var productListRecyclerView: RecyclerView

    private lateinit var editTextMethodPrice: TextInputEditText
    private lateinit var editTextPaymentDate: TextInputEditText

    private var brandSelected: String = "C"
    private var modalitySelected: String = "Refill"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity

        database = FirebaseDatabase.getInstance()
        vehicleReference = database.getReference("vehicles")
        driverReference = database.getReference("drivers")
        clientReference = database.getReference("clients")
        productReference = database.getReference("products")
        dispatchReference = database.getReference("dispatches")
        distributionReference = database.getReference("distributions")
        creditorReference = database.getReference("creditors")
        paymentMethodReference = database.getReference("paymentMethods")

        val bundle = arguments
        vehicleKey = bundle!!.getString("vehicleKey").toString()
        driverKey = bundle.getString("driverKey").toString()

        getFilledAndVoidStock(vehicleKey)
        getDriver(driverKey)

        // loadRestClient()


        /*floatingActionButtonNewDispatch.setOnClickListener { view ->
            Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sale, container, false)
    }

    private fun loadWayPay(){

        val listMethod = listOf("EFECTIVO", "YAPE", "PLIN", "CREDITO")

        /*when(modalitySelected){
            "Refill"-> {listMethod = listOf("EFECTIVO", "YAPE", "PLIN") }
            "Full"-> {listMethod = listOf("EFECTIVO", "YAPE", "PLIN") }
            "BorrowedG"-> {listMethod = listOf("CREDITO") }
            "BorrowedB"-> {listMethod = listOf("EFECTIVO", "YAPE", "PLIN") }
            "BorrowedBG"-> {listMethod = listOf("CREDITO") }
        }*/

        val adapter = ArrayAdapter(
            globalContext!!,
            android.R.layout.simple_spinner_dropdown_item,
            listMethod
        )
        autoCompleteMethodName.keyListener = null
        autoCompleteMethodName.setAdapter(adapter)
        autoCompleteMethodName.setText(
            autoCompleteMethodName.adapter.getItem(0).toString(),
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutListItem = view.findViewById(R.id.layoutList)
        layoutPaymentList = view.findViewById(R.id.layoutPaymentList)
        btnAddPayment = view.findViewById(R.id.buttonAddPayment)
        btnAddPayment.setOnClickListener { addPaymentMethod() }
        btnRegisterDispatch = view.findViewById(R.id.buttonRegister)
        btnRegisterDispatch.setOnClickListener { saveDispatch() }

        textViewSubtotal = view.findViewById(R.id.textViewSubtotal)
        textViewTotal = view.findViewById(R.id.textViewTotal)
        textViewItems = view.findViewById(R.id.textViewItems)

        productListRecyclerView = view.findViewById(R.id.productListRecyclerView)

        editTextMethodPrice = view.findViewById(R.id.editTextMethodPrice)
        clientAutoCompleteView = view.findViewById(R.id.clientAutoCompleteView)
        autoCompleteMethodName = view.findViewById(R.id.autoCompleteMethodName)
        editTextPaymentDate = view.findViewById(R.id.editTextPaymentDate)
        editTextPaymentDate.setOnClickListener { showDatePickerDialog() }
        loadWayPay()

        chipGroupBrand = view.findViewById(R.id.chipGroupBrand)
        chipGroupBrand.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipBrand1 -> {
                    brandSelected = "C"
                }
                R.id.chipBrand2 -> {
                    brandSelected = "Z"
                }
            }
            loadProducts()

        }
        chipGroupModality = view.findViewById(R.id.chipGroupModality)
        chipRefill = view.findViewById(R.id.chipRefill)
        chipFull = view.findViewById(R.id.chipFull)
        chipBorrowedG = view.findViewById(R.id.chipBorrowedG)
        chipBorrowedB = view.findViewById(R.id.chipBorrowedB)
        chipBorrowedBG = view.findViewById(R.id.chipBorrowedBG)

        chipRefill.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
            globalContext!!, android.R.color.holo_green_dark
        ))
        chipRefill.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
            globalContext!!, R.color.white
        )))

        chipGroupModality.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipRefill -> {
                    modalitySelected = "Refill"
                    clearChipModality()
                    chipRefill.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, android.R.color.holo_green_dark
                    ))
                    chipRefill.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, R.color.white
                    )))
                }
                R.id.chipFull -> {
                    modalitySelected = "Full"
                    clearChipModality()
                    chipFull.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, android.R.color.holo_green_dark
                    ))
                    chipFull.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, R.color.white
                    )))
                }
                R.id.chipBorrowedG -> {
                    modalitySelected = "BorrowedG"
                    clearChipModality()
                    chipBorrowedG.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, android.R.color.holo_red_dark
                    ))
                    chipBorrowedG.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, R.color.white
                    )))
                }
                R.id.chipBorrowedB -> {
                    modalitySelected = "BorrowedB"
                    clearChipModality()
                    chipBorrowedB.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, android.R.color.holo_red_dark
                    ))
                    chipBorrowedB.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, R.color.white
                    )))
                }
                R.id.chipBorrowedBG -> {
                    modalitySelected = "BorrowedBG"
                    clearChipModality()
                    chipBorrowedBG.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, android.R.color.holo_red_dark
                    ))
                    chipBorrowedBG.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, R.color.white
                    )))
                }
            }
            loadProducts()

        }

        clientAutoCompleteView.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(cs: CharSequence, s: Int, b: Int, c: Int) {

                if (cs.count() >= 3){
                    Log.d("MIKE", "clientAutoCompleteView")
                    loadRestClient(cs.toString())
                }

            }

            override fun afterTextChanged(editable: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, i: Int, j: Int, k: Int) {}
        })


// chipGroupQuantity = view.findViewById(R.id.chipGroupQuantity)

/*for (i in 1..20 step 1 ){
chipMaking(i.toString())
}*/

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
        dispatch.paymentDate = sdf3
        editTextPaymentDate.setText(sdf2)
    }

    private fun clearChipModality(){
        chipGroupModality.children.forEach {
            val chip = it as Chip
            chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
                globalContext!!, R.color.gray_100
            ))
            chip.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
                globalContext!!, R.color.black
            )))
        }
    }

    private fun registerRestDispatch(){
        Log.d("MIKE", "registerRestDispatch")

        val apiInterface = ClientService.create().registerDispatch(dispatch)
        apiInterface.enqueue(object : Callback<ApiResponse>{
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                Log.d("MIKE", response.isSuccessful.toString())
                val responseDispatchDetail = response.body()!!
                Log.d("MIKE", responseDispatchDetail.status)
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.d("MIKE", "createRestDispatchDetail onFailure: " + t.message.toString())
            }

        })


    }

    private fun loadRestClient(search_text: String) {

        val apiInterface = ClientService.create().searchClientsAndAddresses(search_text)
        apiInterface.enqueue(object : Callback<ArrayList<Client>> {

            override fun onResponse(
                call: Call<ArrayList<Client>>?,
                response: Response<ArrayList<Client>>?
            ) {
//                clientAutoCompleteView.isEnabled = true
                val list: ArrayList<Client>
                if (response?.body() != null) {
                    list = response.body()!!
                    Log.d("MIKE","size list:  ${list.size}")

                    val clientAdapter = ClientAdapter(globalContext!!, R.layout.default_layout, list, object : ClientAdapter.OnItemClickListener2 {
                        override fun onItemClick(model: Client?) {
                            Log.d("MIKE", "client address ${model!!.addresses.size}")
                            clientAutoCompleteView.setText(model.names)
                            clientAutoCompleteView.dismissDropDown()
                            val inputManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            clientAutoCompleteView.closeKeyBoard(inputManager)
                            clientAutoCompleteView.clearFocus()

                            dispatch.clientID = model.id!!
                            dispatch.clientName = model.names
                            dispatch.clientPhone = model.phone

                            if(model.addresses.size > 0){
                                dispatch.addressID = model.addresses[0].id!!
                                dispatch.addressName = model.addresses[0].address
                                dispatch.addressLatitude = model.addresses[0].latitude
                                dispatch.addressLongitude = model.addresses[0].longitude
                            }


//                            requestClient = model

                        }
                    })
                    clientAutoCompleteView.setAdapter(clientAdapter)

                }
            }

            override fun onFailure(
                call: Call<ArrayList<Client>>?,
                t: Throwable?
            ) {
                Log.d("MIKE", "Algo salio mal..." + t!!.message.toString())
            }
        })
    }

    private fun loadProducts() {

        productReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productArrayList = ArrayList<Product>()


                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)!!
                    val searchFilledStock = filledStockMap[product.uid]
                    val searchVoidStock = voidStockMap[product.uid]
                    var filledStock = 0
                    var voidStock = 0

                    if (searchFilledStock !== null) {
                        filledStock = searchFilledStock.quantity
                    }
                    if (searchVoidStock !== null) {
                        voidStock = searchVoidStock.quantity
                    }
                    product.presentation[0].quantityFilled = filledStock
                    product.presentation[1].quantityFilled = filledStock
                    product.presentation[2].quantityFilled = filledStock

                    product.presentation[0].quantityVoid = voidStock
                    product.presentation[1].quantityVoid = voidStock
                    product.presentation[2].quantityVoid = voidStock

                    product.modality = modalitySelected
                    productArrayList.add(product)
                }

                productListRecyclerView.layoutManager = GridLayoutManager(activity, 1, RecyclerView.HORIZONTAL, false)
                productListRecyclerView.setHasFixedSize(true)
                val filterProductArrayList = productArrayList.filter { it.available && it.brand == brandSelected && it.modality == modalitySelected }
                productListRecyclerView.adapter = ProductAdapter(
                    filterProductArrayList as ArrayList<Product>,
                    object : OnItemClickListener {
                        override fun onItemClick(model: Product) {
                            var unitID = 0
                            var quantityMax = 0
                            var quantityVoid = 0
                            var price = 0.0
                            var modality = ""
                            var returnability = ""
                            var unit = ""
//                            var filledProductStoreID = 0
//                            var voidProductStoreID = 0

                            when (model.modality) {
                                "Refill" -> {
                                    price = model.presentation[2].price
                                    quantityMax = model.presentation[2].quantityFilled
                                    quantityVoid = model.presentation[2].quantityVoid
                                    unitID = model.presentation[2].unitID
//                                    filledProductStoreID = model.presentation[2].filledProductStoreID
//                                    voidProductStoreID = model.presentation[2].voidProductStoreID
                                    modality = "Recarga"
                                    returnability = "R"
                                    unit = "G"
                                }
                                "Full" -> {
                                    price = model.presentation[0].price
                                    quantityMax = model.presentation[0].quantityFilled
                                    quantityVoid = model.presentation[0].quantityVoid
                                    unitID = model.presentation[0].unitID
//                                    filledProductStoreID = model.presentation[0].filledProductStoreID
//                                    voidProductStoreID = model.presentation[0].voidProductStoreID
                                    modality = "Completo"
                                    returnability = "F"
                                    unit = "BG"
                                }
                                "BorrowedG" -> {
                                    price = model.presentation[2].price
                                    quantityMax = model.presentation[2].quantityFilled
                                    quantityVoid = model.presentation[2].quantityVoid
                                    unitID = model.presentation[2].unitID
//                                    filledProductStoreID = model.presentation[2].filledProductStoreID
//                                    voidProductStoreID = model.presentation[2].voidProductStoreID
                                    modality = "Liquido Prestado"
                                    returnability = "PG"
                                    unit = "G" // credit G and receive B
                                }
                                "BorrowedB" -> {
                                    price = model.presentation[2].price
                                    quantityMax = model.presentation[2].quantityFilled
                                    quantityVoid = model.presentation[2].quantityVoid
                                    unitID = model.presentation[2].unitID
//                                    filledProductStoreID = model.presentation[2].filledProductStoreID
//                                    voidProductStoreID = model.presentation[2].voidProductStoreID
                                    modality = "Fierro Prestado"
                                    returnability = "PB"
                                    unit = "G" // credit B
                                }
                                "BorrowedBG" -> {
                                    price = model.presentation[0].price
                                    quantityMax = model.presentation[0].quantityFilled
                                    quantityVoid = model.presentation[0].quantityVoid
                                    unitID = model.presentation[0].unitID
//                                    filledProductStoreID = model.presentation[0].filledProductStoreID
//                                    voidProductStoreID = model.presentation[0].voidProductStoreID
                                    modality = "Completo Prestado"
                                    returnability = "PBG"
                                    unit = "BG" // credit BG
                                }
                            }

                            var searchDetail = dispatchDetailMap.filter { it.productKey == model.uid && it.returnability == returnability }

                            if (searchDetail.isEmpty()) {

                                var totalQuantity = 0
                                searchDetail = dispatchDetailMap.filter { it.productKey == model.uid && it.quantity > 0 }
                                searchDetail.forEach {totalQuantity += it.quantity}

                                if (quantityMax > 0 && totalQuantity < quantityMax) {
                                    val newDetail = Dispatch.DispatchDetail()
                                    newDetail.productID = model.productID
//                                    newDetail.filledProductStoreID = filledProductStoreID
//                                    newDetail.voidProductStoreID = voidProductStoreID
                                    newDetail.unitID = unitID
                                    newDetail.productKey = model.uid
                                    newDetail.productName = model.name
                                    newDetail.returnability = returnability
                                    newDetail.modality = modality
                                    newDetail.quantityMax = quantityMax - totalQuantity
                                    newDetail.oldQuantityFilled = quantityMax - totalQuantity
                                    newDetail.oldQuantityVoid = quantityVoid
                                    newDetail.quantity = 1
                                    newDetail.price = price
                                    newDetail.subtotal = price
                                    newDetail.unit = unit

                                    dispatchDetailMap.add(newDetail)

                                    addItemInLinearLayout(newDetail)
                                    updateTotal()
                                } else {
                                    Toast.makeText(globalContext,"Stock insuficiente", Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Toast.makeText(globalContext, "Item ya agregado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }

        })
    }

    private fun addItemInLinearLayout(d: Dispatch.DispatchDetail) {

        val inflater = LayoutInflater.from(globalContext)
        val v = inflater.inflate(R.layout.add_order_detail, null)
        val btnRemove = v.findViewById<Button>(R.id.buttonRemoveItem)
        val btnPlus = v.findViewById<Button>(R.id.buttonPlus)
        val btnMinus = v.findViewById<Button>(R.id.buttonMinus)
        val textViewProductName = v.findViewById<TextView>(R.id.textViewProductName)
        val textViewQuantity = v.findViewById<TextView>(R.id.textViewQuantity)
        val textViewModality = v.findViewById<TextView>(R.id.textViewModality)
        val editTextPrice = v.findViewById<EditText>(R.id.editTextPrice)

        textViewProductName.text = d.productName
        editTextPrice?.setText(d.price.toString())
        textViewModality.text = d.modality

        var quantity = 1

        editTextPrice.setOnKeyListener(View.OnKeyListener {v, keycode, event ->
            if (event.action == KeyEvent.ACTION_UP){
                    Log.d("MIKE", keycode.toString())
                    Log.d("MIKE", editTextPrice.text.toString())
                val textPrice = editTextPrice.text.toString()
                if (textPrice.isNotEmpty() && textPrice.toDouble() > 0){
                    d.price = textPrice.toDouble()
                    dispatchDetailMap.remove(d)
                    d.subtotal = d.quantity * d.price
                    dispatchDetailMap.add(d)
                    updateTotal()
                }

            }
            false
        })

        btnRemove.setOnClickListener {
            removeItemInLinearLayout(v)
            dispatchDetailMap.remove(d)
            updateTotal()
        }

        btnPlus.setOnClickListener {
            if (quantity < d.quantityMax) {
                quantity += 1
                textViewQuantity.text = quantity.toString()
                dispatchDetailMap.remove(d)
                d.quantity = quantity
                d.subtotal = quantity.toDouble() * d.price
                dispatchDetailMap.add(d)
                updateTotal()
            }

        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity -= 1
                textViewQuantity.text = quantity.toString()
                dispatchDetailMap.remove(d)
                d.quantity = quantity
                d.subtotal = quantity.toDouble() * d.price
                dispatchDetailMap.add(d)
                updateTotal()
            }

        }

        layoutListItem.addView(v)
    }

    private fun removeItemInLinearLayout(view: View) {
        layoutListItem.removeView(view)
    }

    private fun updateTotal() {
        var counter = 0
        var total = 0.0
        dispatchDetailMap.forEach {
            counter += it.quantity
            total += it.subtotal
        }
        textViewTotal.text = total.toString()
        textViewItems.text = counter.toString()
        editTextMethodPrice.setText(total.toString())
    }

    @SuppressLint("SetTextI18n")
    private fun addPaymentMethod() {

        if (editTextMethodPrice.text.toString().isNotEmpty()) {

            var selectedItem = "cash"
            val value = autoCompleteMethodName.text.toString()
            val amount = editTextMethodPrice.text.toString().toDouble()

            when (value) {
                "EFECTIVO" -> {selectedItem = "cash"}
                "YAPE" -> {selectedItem = "yape"}
                "PLIN" -> {selectedItem = "plin"}
                "CREDITO" -> {selectedItem = "credit"}
            }

            val searchPaymentMethod = paymentMethodList.filter { (key, _) -> key == selectedItem }
            if (searchPaymentMethod.isEmpty()) {

                val inflater = LayoutInflater.from(globalContext)
                val v = inflater.inflate(R.layout.add_payment_method, null)

                val textViewPaymentMethodName =
                    v.findViewById<TextView>(R.id.textViewPaymentMethodName)
                val textViewPaymentMethodPrice =
                    v.findViewById<TextView>(R.id.textViewPaymentMethodPrice)
                val btnRemove = v.findViewById<Button>(R.id.buttonRemovePaymentMethodItem)

                textViewPaymentMethodName.text = value
                textViewPaymentMethodPrice.text = "S/ $amount"

                paymentMethodList[selectedItem] = amount
                updatePaymentMethodTotal()

                btnRemove.setOnClickListener {
                    removePaymentMethodItem(v)
                    paymentMethodList.remove(selectedItem)
                    updatePaymentMethodTotal()
                }

                editTextMethodPrice.setText("")
                val inputManager = globalContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                editTextMethodPrice.closeKeyBoard(inputManager)
                editTextMethodPrice.clearFocus()

                layoutPaymentList.addView(v)
            } else {
                autoCompleteMethodName.showDropDown()
                Toast.makeText(globalContext, "Ya existe el metodo de pago", Toast.LENGTH_LONG)
                    .show()
            }

        } else {
            Toast.makeText(globalContext, "Verifique el monto", Toast.LENGTH_LONG).show()
        }


    }

    private fun removePaymentMethodItem(view: View) {
        layoutPaymentList.removeView(view)
    }

    private fun updatePaymentMethodTotal() {
        var total = 0.0
        paymentMethodList.forEach { (_, value) ->
            total += value
        }
        textViewSubtotal.text = total.toString()
    }

    private fun chipMaking(title: String) {
        val chip = Chip(globalContext)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(4, 4, 4, 4)
        chip.layoutParams = layoutParams

//chip.chipBackgroundColor = getResources().getColorStateList(R.color.colorChipIconTint)

        chip.text = title
        chip.isCheckable = true
        chip.isClickable = true
        chip.width = 4

        chip.setOnClickListener {
            Toast.makeText(globalContext, "Clicked: ${chip.text}", Toast.LENGTH_SHORT).show()
        }
        chipGroupQuantity.addView(chip)
    }

    private fun saveDispatch() {

        val payed = textViewSubtotal.text.toString().toDouble()
        val totalSale = textViewTotal.text.toString().toDouble()

        if (totalSale == payed ) {

            if(dispatch.clientID > 0){
                createFirebaseDispatch()
                // createRestDispatch()
                registerRestDispatch()

                // (activity as HomeActivity).replaceFragment(OrderPlacedFragment(), "STOCK")
                (activity as HomeActivity).goToOrderPlacedFragment()

                Toast.makeText(globalContext, "Pedido registrado.", Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(globalContext, "Verificar cliente.", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(globalContext, "Verificar pago.", Toast.LENGTH_SHORT).show()
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun createFirebaseDispatch(){

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val sdf2 = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        val currentDate2 = sdf2.format(Date())

        dispatch.details = dispatchDetailMap
        dispatch.paymentMethods = paymentMethodList
        dispatch.status = "02"
        dispatch.dispatchType = "01"
        dispatch.totalPrice = textViewTotal.text.toString().toDouble()
        dispatch.driverID = driver.driverID
        dispatch.driverName = driver.profile.name
        dispatch.driverPhone = driver.profile.phone
        dispatch.distributionKey = driver.distributionKey
        dispatch.dispatchDate = currentDate
        dispatch.identifier = currentDate2.replace("/", "") + "0102"
        dispatch.uid = dispatchReference.push().key!!

        dispatchReference.child(dispatch.uid).setValue(dispatch)

        decreaseFirebaseFilledStock()

        saveFirebasePaymentMethod()


    }

    private fun saveFirebasePaymentMethod(){
        val paymentRef = paymentMethodReference.child("distributions").child(dispatch.distributionKey)

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

        paymentRef.child("dispatches").child(dispatch.uid).child("clientName").setValue(dispatch.clientName)
        paymentRef.child("dispatches").child(dispatch.uid).child("dispatchDate").setValue(dispatch.dispatchDate)
        paymentRef.child("dispatches").child(dispatch.uid).child("total").setValue(dispatch.totalPrice)
        paymentRef.child("dispatches").child(dispatch.uid).child("amounts").setValue(dispatch.paymentMethods)

    }

    private fun decreaseFirebaseFilledStock(){

        val filledRef = vehicleReference.child(vehicleKey).child("stock").child("regular").child("filled")
        val voidRef = vehicleReference.child(vehicleKey).child("stock").child("regular").child("void")
        // val salesRef = distributionReference.child(dispatch.distributionKey).child("departure").child("sales")

        // val pendingRef = creditorReference.child(dispatch.driverID.toString()).child("clients").child(dispatch.clientID.toString()).child("pending")
        // val clientAccountRef = creditorReference.child(dispatch.driverID.toString()).child("clients")

        dispatchDetailMap.forEach {

            filledRef.child(it.productKey).child("quantity").setValue(it.quantityMax - it.quantity)

            when (it.returnability) {
                "R", "PG" -> {
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
                "F" -> {

                    /*val fullMap = distribution.departure.sales.fullCylinder.filter { (key, _) -> key == it.productKey }
                    var fullProduct : Distribution.ProductItem = Distribution.ProductItem()

                    if(fullMap.isNotEmpty()){
                        fullProduct = distribution.departure.sales.fullCylinder[it.productKey]!!
                        salesRef.child("fullCylinder").child(it.productKey).child("quantity").setValue(fullProduct.quantity + it.quantity)
                    }else{
                        fullProduct.productKey = it.productKey
                        fullProduct.productName = filledStockMap[it.productKey]!!.productName
                        fullProduct.productPath = filledStockMap[it.productKey]!!.productPath
                        fullProduct.quantity = it.quantity
                        fullProduct.unit = "BG"
                        // fullProduct.amount = it.subtotal
                        salesRef.child("fullCylinder").child(it.productKey).setValue(fullProduct)
                    }*/

                }
                "P" -> {

                    /*val borrowedMap = distribution.departure.sales.borrowed.filter { (key, _) -> key == it.productKey }
                    var borrowedProduct : Distribution.ProductItem = Distribution.ProductItem()

                    if(borrowedMap.isNotEmpty()){
                        borrowedProduct = distribution.departure.sales.borrowed[it.productKey]!!

                        salesRef.child("borrowed").child(it.productKey).child("quantity").setValue(borrowedProduct.quantity + it.quantity)
                    }else{
                        borrowedProduct.productKey = it.productKey
                        borrowedProduct.productName = filledStockMap[it.productKey]!!.productName
                        borrowedProduct.productPath = filledStockMap[it.productKey]!!.productPath
                        borrowedProduct.quantity = it.quantity
                        borrowedProduct.unit = "B"
                        // borrowedProduct.amount = it.subtotal
                        salesRef.child("borrowed").child(it.productKey).setValue(borrowedProduct)
                    }

                    val creditorMap = creditor.clients.filter { (key, _) -> key == dispatch.clientID }
                    var clientAccount : DriverAccount.ClientAccount = DriverAccount.ClientAccount()
                    var pendingProduct : DriverAccount.ClientAccount.Product = DriverAccount.ClientAccount.Product()

                    if(creditorMap.isNotEmpty()){
                        clientAccount = creditor.clients[dispatch.clientID]!!
                        val pendingMap = clientAccount.pending.filter { (key, _) -> key == it.productKey }

                        if(pendingMap.isNotEmpty()){
                            pendingProduct = clientAccount.pending[it.productKey]!!
                            it.oldClientPendingQuantity = pendingProduct.quantity
                            pendingRef.child(it.productKey).setValue(pendingProduct.quantity + it.quantity)
                        }else{
                            pendingProduct.productKey = it.productKey
                            pendingProduct.productName = filledStockMap[it.productKey]!!.productName
                            pendingProduct.productPath = filledStockMap[it.productKey]!!.productPath
                            pendingProduct.quantity = it.quantity
                            pendingRef.child(it.productKey).setValue(pendingProduct)
                        }

                    }else{
                        pendingProduct.productKey = it.productKey
                        pendingProduct.productName = filledStockMap[it.productKey]!!.productName
                        pendingProduct.productPath = filledStockMap[it.productKey]!!.productPath
                        pendingProduct.quantity = it.quantity
                        clientAccount.pending[it.productKey] = pendingProduct

                        clientAccountRef.child(dispatch.clientID.toString()).setValue(clientAccount)

                    }
                    */

                }
            }
        }
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

                    loadProducts()

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
                    dispatch.distributionID = driver.distributionID
                    dispatch.distributionKey = driver.distributionKey
                    getDistribution(dispatch.distributionKey)
                    getDistributionPayment(dispatch.distributionKey)
                    getCreditor(driver.uid)
                    btnRegisterDispatch.isEnabled = true
                }
                else{
                    btnRegisterDispatch.isEnabled = false
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

    private fun getCreditor(driverKey: String = ""){

        val creditorRef = creditorReference.orderByKey().equalTo(driverKey)
        creditorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (creditorSnapshot in snapshot.children){
                    creditor = creditorSnapshot.getValue(DriverAccount::class.java)!!
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MIKE", error.toException().toString())
            }
        })
    }

    private fun View.closeKeyBoard(inputMethodManager: InputMethodManager) {
        // val view = this.currentFocus
        // if (view != null) {
        // val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        // }
    }
}
