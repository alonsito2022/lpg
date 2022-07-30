package com.example.driver.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.R
import com.example.driver.adapter.PaymentAdapter
import com.example.driver.model.*
import com.example.driver.rest.RequestPaymentMethod
import com.example.driver.retrofit.ClientService
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PaymentFragment : Fragment() {

    private var globalContext: Context? = null

    lateinit var database: FirebaseDatabase
    private lateinit var driverReference: DatabaseReference
    private lateinit var distributionReference: DatabaseReference
    private lateinit var creditorReference: DatabaseReference
    private lateinit var paymentMethodReference: DatabaseReference

    private var distribution: Distribution = Distribution()
    private var creditor: DriverAccount = DriverAccount()
    private var dispatch: Dispatch = Dispatch()
    private var driver : Driver = Driver()
    private var payment : Payment = Payment()
    private var vehicleKey: String = ""
    private var driverKey: String = ""

    private lateinit var textViewCashPrice: TextView
    private lateinit var textViewYapePrice: TextView
    private lateinit var textViewPlinPrice: TextView
    private lateinit var textViewCreditPrice: TextView
    private lateinit var textViewTotalPrice: TextView
    private lateinit var recyclerViewDistributionPayment: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity
        database = FirebaseDatabase.getInstance()

        driverReference = database.getReference("drivers")
        distributionReference = database.getReference("distributions")
        creditorReference = database.getReference("creditors")
        paymentMethodReference = database.getReference("paymentMethods")

        val bundle = arguments
        vehicleKey = bundle!!.getString("vehicleKey").toString()
        driverKey = bundle.getString("driverKey").toString()

        getDriver(driverKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textViewCashPrice = view.findViewById(R.id.textViewCashPrice)
        textViewYapePrice = view.findViewById(R.id.textViewYapePrice)
        textViewPlinPrice = view.findViewById(R.id.textViewPlinPrice)
        textViewTotalPrice = view.findViewById(R.id.textViewTotalPrice)
        textViewCreditPrice = view.findViewById(R.id.textViewCreditPrice)
        recyclerViewDistributionPayment = view.findViewById(R.id.recyclerViewDistributionPayment)
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
                    getRestPaymentMethods()
                    getCreditor(driver.uid)
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
    private fun getRestPaymentMethods(){
        Log.d("MIKE", "getRestPaymentMethods")

        val apiInterface = ClientService.create().getPaymentMethods(driver)
        apiInterface.enqueue(object : Callback<RequestPaymentMethod> {
            override fun onResponse(call: Call<RequestPaymentMethod>, response: Response<RequestPaymentMethod>) {
                Log.d("MIKE", response.isSuccessful.toString())
                val responsePayment = response.body()!!
                Log.d("MIKE", responsePayment.status)
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
    private fun getDistributionPayment(distributionKey: String = ""){

        val distributionPaymentRef = paymentMethodReference.child("distributions").child(distributionKey)
        distributionPaymentRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {

                Log.d("MIKE", snapshot.value.toString())
                if (snapshot.value !== null){

                    payment = snapshot.getValue(Payment::class.java)!!

                    /*for (distributionPaymentSnapshot in snapshot.children){
                        Log.d("MIKE", distributionPaymentSnapshot.toString())
                        payment = distributionPaymentSnapshot.getValue(Payment::class.java)!!
                    }*/

//                    textViewTotalPrice.text = "S/ ${payment.total}"

//                    if(payment.wayPays["yape"] !== null){
//                        textViewYapePrice.text = "S/ ${payment.wayPays["yape"]}"
//                    }
//                    if(payment.wayPays["plin"] !== null){
//                        textViewPlinPrice.text = "S/ ${payment.wayPays["plin"]}"
//                    }
//                    if(payment.wayPays["cash"] !== null){
//                        textViewCashPrice.text = "S/ ${payment.wayPays["cash"]}"
//                    }
//                    if(payment.wayPays["credit"] !== null){
//                        textViewCreditPrice.text = "S/ ${payment.wayPays["credit"]}"
//                    }

                    recyclerViewDistributionPayment.layoutManager = LinearLayoutManager(activity)
                    recyclerViewDistributionPayment.setHasFixedSize(true)
                    recyclerViewDistributionPayment.adapter = PaymentAdapter(
                        payment.dispatches,
                        object : PaymentAdapter.OnItemClickListener {
                            override fun onItemClick(model: Payment.PaymentDispatch) {
                                Log.d("MIKE", model.total.toString())
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

}