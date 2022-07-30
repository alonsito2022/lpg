package com.example.driver.fragments

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.DatePickerFragment
import com.example.driver.R
import com.example.driver.activities.HomeActivity
import com.example.driver.adapter.ClientAdapter
import com.example.driver.adapter.SaleProductAdapter
import com.example.driver.model.*
import com.example.driver.rest.ApiResponse
import com.example.driver.retrofit.ClientService
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class SaleFragment : Fragment() {

    lateinit var database: FirebaseDatabase

    private var dispatchDetailMap: MutableList<Dispatch.DispatchDetail> = mutableListOf()
    private var paymentMethodList: MutableMap<String, Double> = mutableMapOf()


    private var dispatch: Dispatch = Dispatch()
    private var driver : Driver = Driver()
    private var driverID: Int = 0

    private var globalContext: Context? = null
    private lateinit var btnRegisterDispatch: Button
    private lateinit var btnAddPayment: Button
    private lateinit var btnSearchClient: Button
    private lateinit var layoutListItem: LinearLayout
    private lateinit var layoutPaymentList: LinearLayout
    private lateinit var chipGroupBrand: ChipGroup
    private lateinit var chipGroupModality: ChipGroup
    private lateinit var chipGroupCategory: ChipGroup

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
    private lateinit var editTextClientPhone: TextInputEditText
    private lateinit var editTextYapeTime: TextInputEditText
    private lateinit var textInputLayoutPaymentDate: TextInputLayout
    private lateinit var textInputLayoutYapeTime: TextInputLayout

    private var categorySelected: String = "B"
    private var brandSelected: String = "C"
    private var modalitySelected: String = "R"

    private lateinit var saleProductAdapter: SaleProductAdapter

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
        return inflater.inflate(R.layout.fragment_sale, container, false)
    }

    private fun loadWayPay(){

        val listMethod = listOf("EFECTIVO", "YAPE", "PLIN", "FISE", "CREDITO")

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

        autoCompleteMethodName.setOnItemClickListener { adapterView, view, position, l ->
            val itemSelected = adapter.getItem(position).toString()
            textInputLayoutPaymentDate.isVisible = itemSelected == "CREDITO"
            textInputLayoutYapeTime.isVisible = itemSelected == "YAPE"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutListItem = view.findViewById(R.id.layoutList)
        layoutPaymentList = view.findViewById(R.id.layoutPaymentList)
        btnSearchClient = view.findViewById(R.id.btnSearchClient)
        btnSearchClient.setOnClickListener{loadRestClient()}
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
        editTextClientPhone = view.findViewById(R.id.editTextClientPhone)
        editTextPaymentDate = view.findViewById(R.id.editTextPaymentDate)
        textInputLayoutPaymentDate = view.findViewById(R.id.textInputLayoutPaymentDate)
        textInputLayoutYapeTime = view.findViewById(R.id.textInputLayoutYapeTime)
        editTextYapeTime = view.findViewById(R.id.editTextYapeTime)

        chipGroupCategory = view.findViewById(R.id.chipGroupCategory)
        chipGroupBrand = view.findViewById(R.id.chipGroupBrand)
        chipGroupModality = view.findViewById(R.id.chipGroupModality)
        chipRefill = view.findViewById(R.id.chipRefill)
        chipFull = view.findViewById(R.id.chipFull)
        chipBorrowedG = view.findViewById(R.id.chipBorrowedG)
        chipBorrowedB = view.findViewById(R.id.chipBorrowedB)
        chipBorrowedBG = view.findViewById(R.id.chipBorrowedBG)


        editTextPaymentDate.setOnClickListener { showDatePickerDialog() }
        loadWayPay()
        editTextYapeTime.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                val cal = Calendar.getInstance()
                val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    editTextYapeTime.setText(SimpleDateFormat("HH:mm").format(cal.time))
                }
                TimePickerDialog(globalContext, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
            }
        })

        chipGroupCategory.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipCategory1 -> {
                    categorySelected = "B"
                    chipRefill.isEnabled = true
                    chipBorrowedG.isEnabled = true
                    chipBorrowedB.isEnabled = true
                    chipRefill.isChecked = true
                }
                R.id.chipCategory2 -> {
                    categorySelected = "NA"
                    chipRefill.isEnabled = false
                    chipBorrowedG.isEnabled = false
                    chipBorrowedB.isEnabled = false
                    chipFull.isChecked = true
                }
            }
            loadProducts()
        }


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

        chipRefill.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
            globalContext!!, android.R.color.holo_green_dark
        ))
        chipRefill.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
            globalContext!!, R.color.white
        )))

        chipGroupModality.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipRefill -> {
                    modalitySelected = "R"
                    clearChipModality()
                    chipRefill.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, android.R.color.holo_green_dark
                    ))
                    chipRefill.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, R.color.white
                    )))
                }
                R.id.chipFull -> {
                    modalitySelected = "F"
                    clearChipModality()
                    chipFull.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, android.R.color.holo_green_dark
                    ))
                    chipFull.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, R.color.white
                    )))
                }
                R.id.chipBorrowedG -> {
                    modalitySelected = "PG"
                    clearChipModality()
                    chipBorrowedG.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, android.R.color.holo_red_dark
                    ))
                    chipBorrowedG.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, R.color.white
                    )))
                }
                R.id.chipBorrowedB -> {
                    modalitySelected = "PB"
                    clearChipModality()
                    chipBorrowedB.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, android.R.color.holo_red_dark
                    ))
                    chipBorrowedB.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(
                        globalContext!!, R.color.white
                    )))
                }
                R.id.chipBorrowedBG -> {
                    modalitySelected = "PBG"
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

        val apiInterface = ClientService.create().registerDispatch(dispatch)
        apiInterface.enqueue(object : Callback<ApiResponse>{
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                val responseDispatchDetail = response.body()!!
                Log.d("MIKE", responseDispatchDetail.status)
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.d("MIKE", "createRestDispatchDetail onFailure: " + t.message.toString())
            }

        })


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

                                dispatch.clientID = model.id!!
                                dispatch.clientName = model.names
                                dispatch.clientPhone = model.phone

                                editTextClientPhone.setText(model.phone)

                                if(model.addresses.size > 0){
                                    dispatch.addressID = model.addresses[0].id!!
                                    dispatch.addressName = model.addresses[0].address
                                    dispatch.addressLatitude = model.addresses[0].latitude
                                    dispatch.addressLongitude = model.addresses[0].longitude
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
        textViewModality.text = d.modality.uppercase()

        editTextPrice.setOnKeyListener(View.OnKeyListener {v, keycode, event ->
            if (event.action == KeyEvent.ACTION_UP){
                val textPrice = editTextPrice.text.toString()
                if (textPrice.isNotEmpty() && textPrice.toDouble() > 0){
                    d.price = textPrice.toDouble()
                    d.subtotal = d.quantity * d.price
                    dispatchDetailMap.filter { it.productID == d.productID && it.returnability == d.returnability }.forEach {
                        it.price = d.price
                        it.subtotal = d.subtotal
                    }
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
            var totalQuantity = 0
            val searchDetail = dispatchDetailMap.filter { it.productID == d.productID && it.quantity > 0 }
            searchDetail.forEach {totalQuantity += it.quantity}

            if (d.quantityMax - totalQuantity > 0) {
                d.quantity += 1
                textViewQuantity.text = d.quantity.toString()
                dispatchDetailMap.filter { it.productID == d.productID && it.returnability == d.returnability }.forEach {
                    it.quantity = d.quantity
                    it.subtotal = d.quantity.toDouble() * d.price
                }
                updateTotal()
            }

        }

        btnMinus.setOnClickListener {
            if (d.quantity > 1) {
                d.quantity -= 1
                textViewQuantity.text = d.quantity.toString()
                dispatchDetailMap.filter { it.productID == d.productID && it.returnability == d.returnability }.forEach {
                    it.quantity = d.quantity
                    it.subtotal = d.quantity.toDouble() * d.price
                }
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
                "FISE" -> {selectedItem = "fise"}
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

    private fun saveDispatch() {

        val payed = textViewSubtotal.text.toString().toDouble()
        val totalSale = textViewTotal.text.toString().toDouble()

        if (totalSale == payed ) {

            if(dispatch.clientID > 0){
                dispatch.details = dispatchDetailMap
                dispatch.paymentMethods = paymentMethodList
                dispatch.status = "02"
                dispatch.dispatchType = "01"
                dispatch.totalPrice = textViewTotal.text.toString().toDouble()
                dispatch.driverID = driver.driverID
                dispatch.distributionID = driver.vehicle.distribution.distributionID
                dispatch.dispatchDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

                dispatch.clientPhone = editTextClientPhone.text.toString()
                dispatch.yapeTime = editTextYapeTime.text.toString()

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

    private fun getDriver(id: Int = 0){

        val apiInterface = ClientService.create().searchDriverByID(Driver(id))
        apiInterface.enqueue(object : Callback<Driver>{
            override fun onResponse(call: Call<Driver>, response: Response<Driver>) {
                if (response.body() != null) {
                    driver = response.body()!!

                    val filterProductArrayList = driver.vehicle.stockRegular.filter { it.productBrand == brandSelected  && it.productCategory == categorySelected } as MutableList<Driver.Vehicle.StockRegular>

                    productListRecyclerView.layoutManager = GridLayoutManager(activity, 1, RecyclerView.HORIZONTAL, false)
                    productListRecyclerView.setHasFixedSize(true)
                    saleProductAdapter = SaleProductAdapter(filterProductArrayList, object : SaleProductAdapter.OnItemClickListener{
                        override fun onItemClick(model: Driver.Vehicle.StockRegular) {

                            var modality = ""
                            var indexTariff = 0

                            when (model.returnability) {
                                "R" -> {
                                    indexTariff = 2
                                    modality = "Liquido"
                                }
                                "F" -> {
                                    indexTariff = 0
                                    modality = "Completo"
                                }
                                "PG" -> {
                                    indexTariff = 2
                                    modality = "Liquido Prestado"
                                }
                                "PB" -> {
                                    indexTariff = 2
                                    modality = "Fierro Prestado"
                                }
                                "PBG" -> {
                                    indexTariff = 0
                                    modality = "Completo Prestado"
                                }
                            }

                            var searchDetail = dispatchDetailMap.filter { it.productID == model.productID && it.returnability == model.returnability }
                            if (searchDetail.isEmpty()) {
                                var totalQuantity = 0
                                searchDetail = dispatchDetailMap.filter { it.productID == model.productID && it.quantity > 0 }
                                searchDetail.forEach {totalQuantity += it.quantity}

                                if (model.filledStock > 0 && totalQuantity < model.filledStock){
                                    val newDetail = Dispatch.DispatchDetail()
                                    newDetail.productID = model.productID
                                    newDetail.productName = model.productName

                                    newDetail.unitID = model.tariff[indexTariff].unitID
                                    newDetail.unitName = model.tariff[indexTariff].unitName

                                    newDetail.returnability = model.returnability
                                    newDetail.modality = modality

                                    newDetail.quantityMax = model.filledStock

                                    newDetail.quantity = 1
                                    newDetail.price = model.tariff[indexTariff].price
                                    newDetail.subtotal = model.tariff[indexTariff].price

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
                    })
                    productListRecyclerView.adapter = saleProductAdapter
                    btnRegisterDispatch.isEnabled = driver.vehicle.distribution.distributionStatus == "P"
                }
            }

            override fun onFailure(call: Call<Driver>, t: Throwable) {
                Log.d("MIKE", "getDriver onFailure: " + t.message.toString())
            }

        })

    }

    private fun loadProducts(){

        val filterProductArrayList: MutableList<Driver.Vehicle.StockRegular> = mutableListOf()

        driver.vehicle.stockRegular.filter { it.productBrand == brandSelected && it.productCategory == categorySelected }.forEach { item ->
            item.returnability = modalitySelected
            filterProductArrayList.add(item)
        }
        saleProductAdapter.updateDataSet(filterProductArrayList)
    }


    private fun View.closeKeyBoard(inputMethodManager: InputMethodManager) {
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
}
