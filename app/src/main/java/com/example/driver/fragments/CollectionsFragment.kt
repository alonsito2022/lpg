package com.example.driver.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driver.DatePickerFragment
import com.example.driver.R
import com.example.driver.adapter.ClientWithCollectionAdapter
import com.example.driver.rest.ClientWithCollection
import com.example.driver.rest.CreditCollectionsSummary
import com.example.driver.rest.RequestDateRange
import com.example.driver.retrofit.ClientService
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CollectionsFragment : Fragment() {

    private var globalContext: Context? = null
    private var loadingCount = 0
    private var dateRange = RequestDateRange()
    private var driverID: Int = 0

    private lateinit var editTextSearchDate: TextInputEditText
    private lateinit var cardViewSummary: MaterialCardView
    private lateinit var btnSearch: Button
    private lateinit var recyclerViewClients: RecyclerView
    private lateinit var progressBarLoading: ProgressBar
    private lateinit var textViewEmptyState: TextView

    private lateinit var textViewCashCollected: TextView
    private lateinit var textViewYapeCollected: TextView
    private lateinit var textViewPlinCollected: TextView
    private lateinit var textViewFiseCollected: TextView
    private lateinit var textViewTotalCollected: TextView

    private var listClients = arrayListOf<ClientWithCollection>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalContext = this.activity
        
        val bundle = arguments
        driverID = bundle!!.getInt("driverID")
        dateRange.driverID = driverID
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_collections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        cardViewSummary = view.findViewById(R.id.cardViewSummary)
        recyclerViewClients = view.findViewById(R.id.recyclerViewClients)
        editTextSearchDate = view.findViewById(R.id.editTextSearchDate)
        progressBarLoading = view.findViewById(R.id.progressBarLoading)
        textViewEmptyState = view.findViewById(R.id.textViewEmptyState)
        
        val sdf2 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val sdf3 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        dateRange.startDate = sdf3
        dateRange.endDate = sdf3
        editTextSearchDate.setText(sdf2)

        editTextSearchDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnSearch = view.findViewById(R.id.btnSearch)
        btnSearch.setOnClickListener {
            if (editTextSearchDate.text.toString() != "") {
                // Enviar la misma fecha en startDate y endDate
                val selectedDate = dateRange.startDate
                dateRange.endDate = selectedDate
                
                loadingCount = 0
                showLoading()
                cardViewSummary.visibility = View.VISIBLE
                loadClientsWithCollections()
                loadCreditCollectionsSummary()
            } else {
                Toast.makeText(globalContext, "Seleccione una fecha.", Toast.LENGTH_SHORT).show()
            }
        }

        textViewCashCollected = view.findViewById(R.id.textViewCashCollected)
        textViewYapeCollected = view.findViewById(R.id.textViewYapeCollected)
        textViewPlinCollected = view.findViewById(R.id.textViewPlinCollected)
        textViewFiseCollected = view.findViewById(R.id.textViewFiseCollected)
        textViewTotalCollected = view.findViewById(R.id.textViewTotalCollected)
    }

    private fun loadClientsWithCollections() {
        val apiInterface = ClientService.create().getClientsWithCollections(dateRange)
        apiInterface.enqueue(object : Callback<ArrayList<ClientWithCollection>> {
            override fun onResponse(
                call: Call<ArrayList<ClientWithCollection>>,
                response: Response<ArrayList<ClientWithCollection>>
            ) {
                if (response.body() != null) {
                    listClients = response.body()!!
                    if (listClients.isNotEmpty()) {
                        recyclerViewClients.layoutManager = LinearLayoutManager(activity)
                        recyclerViewClients.setHasFixedSize(true)
                        recyclerViewClients.adapter = ClientWithCollectionAdapter(
                            listClients,
                            object : ClientWithCollectionAdapter.OnItemClickListener {
                                override fun onItemClick(model: ClientWithCollection) {
                                    // Puedes agregar funcionalidad aquí si es necesario
                                }
                            }
                        )
                        recyclerViewClients.visibility = View.VISIBLE
                        textViewEmptyState.visibility = View.GONE
                    } else {
                        recyclerViewClients.visibility = View.GONE
                        textViewEmptyState.visibility = View.VISIBLE
                    }
                } else {
                    recyclerViewClients.visibility = View.GONE
                    textViewEmptyState.visibility = View.VISIBLE
                }
                checkAndHideLoading()
            }

            override fun onFailure(call: Call<ArrayList<ClientWithCollection>>, t: Throwable) {
                Log.d("MIKE", "loadClientsWithCollections. Algo salio mal..." + t.message.toString())
                recyclerViewClients.visibility = View.GONE
                textViewEmptyState.visibility = View.VISIBLE
                Toast.makeText(globalContext, "Error al cargar los clientes con cobranzas", Toast.LENGTH_SHORT).show()
                checkAndHideLoading()
            }
        })
    }

    private fun loadCreditCollectionsSummary() {
        val apiInterface = ClientService.create().getCreditCollectionsSummary(dateRange)
        apiInterface.enqueue(object : Callback<CreditCollectionsSummary> {
            override fun onResponse(
                call: Call<CreditCollectionsSummary>,
                response: Response<CreditCollectionsSummary>
            ) {
                if (response.body() != null) {
                    val summary = response.body()!!
                    textViewTotalCollected.text = "S/ ${summary.sumTotalAmountCollected}"
                    textViewCashCollected.text = "S/ ${summary.cashCollected}"
                    textViewYapeCollected.text = "S/ ${summary.yapeCollected}"
                    textViewPlinCollected.text = "S/ ${summary.plinCollected}"
                    textViewFiseCollected.text = "S/ ${summary.fiseCollected}"
                }
                checkAndHideLoading()
            }

            override fun onFailure(call: Call<CreditCollectionsSummary>, t: Throwable) {
                Log.d("MIKE", "loadCreditCollectionsSummary. Algo salio mal..." + t.message.toString())
                Toast.makeText(globalContext, "Error al cargar el resumen de cobranzas", Toast.LENGTH_SHORT).show()
                checkAndHideLoading()
            }
        })
    }

    private fun showLoading() {
        progressBarLoading.visibility = View.VISIBLE
        recyclerViewClients.visibility = View.GONE
        textViewEmptyState.visibility = View.GONE
        btnSearch.isEnabled = false
    }

    private fun checkAndHideLoading() {
        loadingCount++
        if (loadingCount >= 2) {
            hideLoading()
        }
    }

    private fun hideLoading() {
        progressBarLoading.visibility = View.GONE
        btnSearch.isEnabled = true
    }

    private fun showDatePickerDialog() {
        val fm: FragmentManager = (activity as AppCompatActivity?)!!.supportFragmentManager
        val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(fm, "datePicker")
    }

    @SuppressLint("SimpleDateFormat")
    private fun onDateSelected(day: Int, month: Int, year: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val sdf2 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        val sdf3 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        dateRange.startDate = sdf3
        dateRange.endDate = sdf3
        editTextSearchDate.setText(sdf2)
    }
}

