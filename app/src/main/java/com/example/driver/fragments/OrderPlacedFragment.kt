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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.DatePickerFragment
import com.example.driver.R
import com.example.driver.activities.HomeActivity
import com.example.driver.adapter.CashFlowAdapter
import com.example.driver.adapter.DispatchDetailAdapter
import com.example.driver.adapter.DispatchPlacedAdapter
import com.example.driver.adapter.MethodPaymentAdapter
import com.example.driver.model.CashFlow
import com.example.driver.model.Dispatch
import com.example.driver.model.Driver
import com.example.driver.model.Vehicle
import com.example.driver.rest.RequestPaymentMethod
import com.example.driver.retrofit.ClientService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class OrderPlacedFragment : Fragment() {

    private var globalContext: Context? = null

    private var dispatch: Dispatch = Dispatch()
    private var listDispatches = arrayListOf<Dispatch>()

    private lateinit var editTextSearchDate: TextInputEditText
    private lateinit var constraintLayoutSummary: ConstraintLayout
    private lateinit var btnSearch: Button
    private lateinit var recyclerViewOrderPlaced: RecyclerView
    private lateinit var recyclerViewDispatchMethodPayment: RecyclerView
    private lateinit var fab: FloatingActionButton

    private lateinit var textViewCashPrice: TextView
    private lateinit var textViewYapePrice: TextView
    private lateinit var textViewPlinPrice: TextView
    private lateinit var textViewCreditPrice: TextView
    private lateinit var textViewTotalPrice: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity

        val bundle = arguments
        dispatch.driverID = bundle!!.getInt("driverID")
        dispatch.dispatchType = "01"
        dispatch.status = "02"

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_placed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        constraintLayoutSummary = view.findViewById(R.id.constraintLayoutSummary)
        recyclerViewOrderPlaced = view.findViewById(R.id.recyclerViewOrderPlaced)
        editTextSearchDate = view.findViewById(R.id.editTextSearchDate)
        val sdf2 = SimpleDateFormat("dd/MM/yyyy").format(Date())
        val sdf3 = SimpleDateFormat("yyyy-MM-dd").format(Date())
        dispatch.dispatchDate = sdf3
        editTextSearchDate.setText(sdf2)

        editTextSearchDate.setOnClickListener { showDatePickerDialog() }

        btnSearch = view.findViewById(R.id.btnSearch)
        btnSearch.setOnClickListener{
            if(editTextSearchDate.text.toString() != "") {
                constraintLayoutSummary.visibility = View.VISIBLE
                loadDispatches()
                getRestPaymentMethods()
            }
            else
                Toast.makeText(globalContext, "Elija caja.", Toast.LENGTH_SHORT).show()
        }
        fab = view.findViewById(R.id.floatingActionButtonNewDispatch)
        fab.setOnClickListener { goToFragment() }

        textViewCashPrice = view.findViewById(R.id.textViewCashPrice)
        textViewYapePrice = view.findViewById(R.id.textViewYapePrice)
        textViewPlinPrice = view.findViewById(R.id.textViewPlinPrice)
        textViewTotalPrice = view.findViewById(R.id.textViewTotalPrice)
        textViewCreditPrice = view.findViewById(R.id.textViewCreditPrice)
    }

    private fun getRestPaymentMethods(){
        Log.d("MIKE", "getRestPaymentMethods")

        val apiInterface = ClientService.create().getPaymentMethods(dispatch)
        apiInterface.enqueue(object : Callback<RequestPaymentMethod> {
            override fun onResponse(call: Call<RequestPaymentMethod>, response: Response<RequestPaymentMethod>) {
                Log.d("MIKE", response.isSuccessful.toString())
                val responsePayment = response.body()!!
                textViewTotalPrice.text = "S/ ${responsePayment.sumTotalAmount}"
                if(responsePayment.yape !== null){
                    textViewYapePrice.text = "S/ ${responsePayment.yape}"
                }
                if(responsePayment.plin !== null){
                    textViewPlinPrice.text = "S/ ${responsePayment.plin}"
                }
                if(responsePayment.cash !== null){
                    textViewCashPrice.text = "S/ ${responsePayment.cash}"
                }
                if(responsePayment.credit !== null){
                    textViewCreditPrice.text = "S/ ${responsePayment.credit}"
                }
            }

            override fun onFailure(call: Call<RequestPaymentMethod>, t: Throwable) {
                Log.d("MIKE", "getRestPaymentMethods onFailure: " + t.message.toString())
            }

        })
    }

    private fun goToFragment(){
        Log.d("MIKE", "go to fragment")
        (activity as HomeActivity).goToSaleFragment()
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
        addDialog.setView(v)
        addDialog.setPositiveButton("OK") { dialog, _ ->
            Toast.makeText(globalContext, "OK", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        addDialog.create()
        addDialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun loadDispatches() {
        val apiInterface = ClientService.create().getDispatchesByDate(dispatch)
        apiInterface.enqueue(object : Callback<ArrayList<Dispatch>> {
            override fun onResponse(
                call: Call<ArrayList<Dispatch>>,
                response: Response<ArrayList<Dispatch>>
            ) {
                if (response.body() != null) {
                    listDispatches = response.body()!!
                    recyclerViewOrderPlaced.layoutManager = LinearLayoutManager(activity)
                    recyclerViewOrderPlaced.setHasFixedSize(true)
                    recyclerViewOrderPlaced.adapter = DispatchPlacedAdapter(
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

}