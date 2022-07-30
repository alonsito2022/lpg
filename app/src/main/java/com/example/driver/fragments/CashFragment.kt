package com.example.driver.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.driver.model.Cash
import com.example.driver.model.Driver
import com.example.driver.retrofit.ClientService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.DatePickerFragment
import com.example.driver.R
import com.example.driver.adapter.CashAdapter
import com.example.driver.adapter.CashFlowAdapter
import com.example.driver.model.CashFlow
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CashFragment : Fragment() {

    private var globalContext: Context? = null
    private var driver : Driver = Driver()
    private var cashFlow: CashFlow = CashFlow()

    private var listCashes = arrayListOf<Cash>()
    private var listCashFlows = arrayListOf<CashFlow>()

    private lateinit var recyclerViewCashFlow: RecyclerView
    private lateinit var textInputLayoutCash: TextInputLayout
    private lateinit var autoCompleteCash: AutoCompleteTextView
    private lateinit var editTextSearchDate: TextInputEditText
    private lateinit var btnCleanCash: Button
    private lateinit var btnSearchCashFlow: Button
    private lateinit var btnNewCashFlow: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity

        val bundle = arguments
        driver.driverID = bundle!!.getInt("driverID")
        loadCashes()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cash, container, false)
        autoCompleteCash = view.findViewById(R.id.autoCompleteCash)
        textInputLayoutCash = view.findViewById(R.id.textInputLayoutCash)
        textInputLayoutCash.setEndIconOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                autoCompleteCash.showDropDown()
                autoCompleteCash.setText("")
                loadCashes()
            }

        })

        editTextSearchDate = view.findViewById(R.id.editTextSearchDate)


        val sdf2 = SimpleDateFormat("dd/MM/yyyy").format(Date())
        val sdf3 = SimpleDateFormat("yyyy-MM-dd").format(Date())
        cashFlow.transactionDate = sdf3
        editTextSearchDate.setText(sdf2)

        editTextSearchDate.setOnClickListener { showDatePickerDialog() }

        btnNewCashFlow = view.findViewById(R.id.btnNewCashFlow)
        btnNewCashFlow.setOnClickListener{

            if(cashFlow.cashID > 0){
                val inflater = LayoutInflater.from(globalContext)
                val v = inflater.inflate(R.layout.dialog_new_cash_flow, null)

                val textViewCashName = v.findViewById<TextView>(R.id.textViewCashName)
                val textViewCashBalance = v.findViewById<TextView>(R.id.textViewCashBalance)
                val radioGroupTypeOperation = v.findViewById<RadioGroup>(R.id.radioGroupTypeOperation)
                val radioButtonEntrance = v.findViewById<RadioButton>(R.id.radioButtonEntrance)
                val radioButtonDeparture = v.findViewById<RadioButton>(R.id.radioButtonDeparture)

                val radioButtonDepositFise = v.findViewById<RadioButton>(R.id.radioButtonDepositFise)
                val radioButtonRetireFise = v.findViewById<RadioButton>(R.id.radioButtonRetireFise)
                val radioButtonDepositPlin = v.findViewById<RadioButton>(R.id.radioButtonDepositPlin)
                val radioButtonRetirePLin = v.findViewById<RadioButton>(R.id.radioButtonRetirePLin)
                val radioButtonDepositYape = v.findViewById<RadioButton>(R.id.radioButtonDepositYape)
                val radioButtonRetireYape = v.findViewById<RadioButton>(R.id.radioButtonRetireYape)
                val editTextDescription = v.findViewById<EditText>(R.id.editTextDescription)
                val editTextTotal = v.findViewById<EditText>(R.id.editTextTotal)

                textViewCashName.text = cashFlow.cashName
                textViewCashBalance.text = cashFlow.cashBalance.toString()


                when(cashFlow.cashAccountCode){
                    "10111" -> {
                        radioButtonEntrance.visibility = View.VISIBLE
                        radioButtonDeparture.visibility = View.VISIBLE
                        radioButtonEntrance.isChecked = true

                        radioButtonDepositPlin.visibility = View.GONE
                        radioButtonRetirePLin.visibility = View.GONE
                        radioButtonDepositYape.visibility = View.GONE
                        radioButtonRetireYape.visibility = View.GONE
                        radioButtonDepositFise.visibility = View.GONE
                        radioButtonRetireFise.visibility = View.GONE
                        cashFlow.type="E"

                    }
                    "10411" -> {
                        radioButtonDepositYape.visibility = View.VISIBLE
                        radioButtonRetireYape.visibility = View.VISIBLE
                        radioButtonDepositYape.isChecked = true

                        radioButtonDepositPlin.visibility = View.GONE
                        radioButtonRetirePLin.visibility = View.GONE
                        radioButtonEntrance.visibility = View.GONE
                        radioButtonDeparture.visibility = View.GONE
                        radioButtonDepositFise.visibility = View.GONE
                        radioButtonRetireFise.visibility = View.GONE
                        cashFlow.type="DY"

                    }
                    "10412" -> {
                        radioButtonDepositPlin.visibility = View.VISIBLE
                        radioButtonRetirePLin.visibility = View.VISIBLE
                        radioButtonDepositPlin.isChecked = true

                        radioButtonDepositYape.visibility = View.GONE
                        radioButtonRetireYape.visibility = View.GONE
                        radioButtonEntrance.visibility = View.GONE
                        radioButtonDeparture.visibility = View.GONE
                        radioButtonDepositFise.visibility = View.GONE
                        radioButtonRetireFise.visibility = View.GONE
                        cashFlow.type="DP"

                    }
                    "10414" -> {
                        radioButtonDepositFise.visibility = View.VISIBLE
                        radioButtonRetireFise.visibility = View.VISIBLE
                        radioButtonDepositFise.isChecked = true

                        radioButtonDepositYape.visibility = View.GONE
                        radioButtonRetireYape.visibility = View.GONE
                        radioButtonEntrance.visibility = View.GONE
                        radioButtonDeparture.visibility = View.GONE
                        radioButtonDepositPlin.visibility = View.GONE
                        radioButtonRetirePLin.visibility = View.GONE
                        cashFlow.type="DF"

                    }
                }

                radioGroupTypeOperation.setOnCheckedChangeListener{ _, checkedId ->
                    when (checkedId) {
                        R.id.radioButtonEntrance -> { cashFlow.type="E" }
                        R.id.radioButtonDeparture -> { cashFlow.type="S" }
                        R.id.radioButtonDepositPlin -> { cashFlow.type="DP" }
                        R.id.radioButtonRetirePLin -> { cashFlow.type="RP" }
                        R.id.radioButtonDepositFise -> { cashFlow.type="DF" }
                        R.id.radioButtonRetireFise -> { cashFlow.type="RF" }
                        R.id.radioButtonDepositYape -> { cashFlow.type="DY" }
                        R.id.radioButtonRetireYape -> { cashFlow.type="RY" }
                        else -> {}
                    }
                }

                val dialogClose = v.findViewById<Button>(R.id.dialog_close)
                val dialogSave = v.findViewById<Button>(R.id.dialog_save)
                val addDialog = AlertDialog.Builder(globalContext)
                addDialog.setView(v)
                addDialog.setTitle("NUEVA OPERACION")

                val dialog: AlertDialog = addDialog.create()
                dialog.show()
                dialogClose.setOnClickListener{
                    dialog.dismiss()
                }
                dialogSave.setOnClickListener{

                    cashFlow.total = editTextTotal.text.toString().toDouble()
                    cashFlow.description = editTextDescription.text.toString().uppercase()

                    if(editTextTotal.text.isNotEmpty()){
                        if(editTextDescription.text.isNotEmpty()){
                            saveCashFlow()
                        }else Toast.makeText(globalContext, "Verificar descripcion.", Toast.LENGTH_SHORT).show()
                    }else Toast.makeText(globalContext, "Verificar pago.", Toast.LENGTH_SHORT).show()

                    dialog.dismiss()
                }
            }else{
                Toast.makeText(globalContext, "Elija caja.", Toast.LENGTH_SHORT).show()

            }

        }

        btnSearchCashFlow = view.findViewById(R.id.btnSearchCashFlow)
        btnSearchCashFlow.setOnClickListener{
            if(cashFlow.cashID > 0)
                loadCashFlows()
            else
                Toast.makeText(globalContext, "Elija caja.", Toast.LENGTH_SHORT).show()
        }

        recyclerViewCashFlow = view.findViewById(R.id.recyclerViewCashFlow)
        return view
    }


    private fun loadCashes(){

        val apiInterface = ClientService.create().getCashes(driver)
        apiInterface.enqueue(object : Callback<ArrayList<Cash>> {
            override fun onResponse(call: Call<ArrayList<Cash>>, response: Response<ArrayList<Cash>>) {
                listCashes = response.body()!!
                autoCompleteCash.setAdapter(CashAdapter(globalContext!!,
                    R.layout.item_cash_view, listCashes, object : CashAdapter.OnItemClickListener{
                    override fun onItemClick(model: Cash) {
                        autoCompleteCash.setText(model.name)
                        autoCompleteCash.dismissDropDown()
                        val inputManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        autoCompleteCash.closeKeyBoard(inputManager)
                        autoCompleteCash.clearFocus()
                        cashFlow.cashID = model.cashID
                        cashFlow.cashName = model.name
                        cashFlow.cashAccountCode = model.accountCode
                        cashFlow.cashBalance = model.balance
                    }
                }))
                Log.d("MIKE", "listCashes ok: " + listCashes.size)
            }

            override fun onFailure(call: Call<ArrayList<Cash>>, t: Throwable) {
                Log.d("MIKE", "loadCashes onFailure: " + t.message.toString())
            }
        })

    }

    private fun loadCashFlows() {
        val apiInterface = ClientService.create().getCashFlow(cashFlow)
        apiInterface.enqueue(object : Callback<ArrayList<CashFlow>> {
            override fun onResponse(
                call: Call<ArrayList<CashFlow>>,
                response: Response<ArrayList<CashFlow>>
            ) {
                if (response.body() != null) {
                    listCashFlows = response.body()!!
                    recyclerViewCashFlow.layoutManager = LinearLayoutManager(globalContext)
                    recyclerViewCashFlow.setHasFixedSize(true)
                    recyclerViewCashFlow.adapter= CashFlowAdapter( listCashFlows )
                }
            }
            override fun onFailure(call: Call<ArrayList<CashFlow>>, t: Throwable) {
                Log.d("MIKE", "loadCashFlows. Algo salio mal..." + t.message.toString())
            }
        })
    }

    private fun saveCashFlow() {
        val apiInterface = ClientService.create().saveCashFlow(cashFlow)
        apiInterface.enqueue(object : Callback<ArrayList<CashFlow>> {
            override fun onResponse(
                call: Call<ArrayList<CashFlow>>,
                response: Response<ArrayList<CashFlow>>
            ) {
                if (response.body() != null) {
                    listCashFlows = response.body()!!
                    recyclerViewCashFlow.layoutManager = LinearLayoutManager(globalContext)
                    recyclerViewCashFlow.setHasFixedSize(true)
                    recyclerViewCashFlow.adapter= CashFlowAdapter( listCashFlows )
                }
            }
            override fun onFailure(call: Call<ArrayList<CashFlow>>, t: Throwable) {
                Log.d("MIKE", "loadCashFlows. Algo salio mal..." + t.message.toString())
            }
        })
    }

    private fun View.closeKeyBoard(inputMethodManager: InputMethodManager) {
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
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
        cashFlow.transactionDate = sdf3
        editTextSearchDate.setText(sdf2)
    }

}