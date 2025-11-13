package com.example.driver.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.driver.LocalDatabase.Preference
import com.example.driver.R
import com.example.driver.model.Device
import com.example.driver.model.Driver
import com.example.driver.rest.ApiResponse
import com.example.driver.retrofit.ClientService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.messaging.FirebaseMessaging
//import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

//    var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    lateinit var preference: Preference
    private lateinit var btn_login: Button
    private lateinit var email_login: TextInputEditText
    private lateinit var password_login: TextInputEditText

    private var device: Device = Device()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btn_login = findViewById(R.id.btn_login)
        email_login = findViewById(R.id.email_login)
        password_login = findViewById(R.id.password_login)

        btn_login.setOnClickListener{ login() }

        preference = Preference(applicationContext)

        if(preference.getData("driverID") != ""){
//            preference.clearPreference()
            notification()
            startActivity(Intent(applicationContext, HomeActivity::class.java))
            finish()
        }

    }

    private fun notification(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if(!task.isSuccessful){
                showToast("Fetching FCM registration token failed. " + task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            device.driverID = preference.getData("driverID").toInt()
            device.deviceToken = token
            device.deviceType = "A"

            val apiInterface = ClientService.create().registerOrUpdateDevice(device)
            apiInterface.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    val responseDevice = response.body()!!
                    Log.d("MIKE", responseDevice.status)
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.d("MIKE", "responseDevice onFailure: " + t.message.toString())
                }

            })

        })
    }

    private fun login(){
        val email = email_login.text.toString().trim()
        val password = password_login.text.toString().trim()
        if(email.isEmpty() || password.isEmpty()){
            showToast("Todos los campos son requeridos")
        }else{
            if(isValidEmail(email)){
                isEmailExist(Driver(email, password))
            }else{
                showToast("Verifica tu email")
            }
        }
    }

    private fun isEmailExist(d: Driver){

        val apiInterface = ClientService.create().searchDriverByCredentials(d)
        apiInterface.enqueue(object : Callback<Driver>{
            override fun onResponse(call: Call<Driver>, response: Response<Driver>) {

                if (response.body() != null) {
                    val driver: Driver = response.body()!!

                    preference.saveData("driverID", driver.driverID.toString())
                    preference.saveData("email", driver.email)
                    preference.saveData("phone", driver.phone)
                    preference.saveData("names", driver.names)
                    preference.saveData("licensePlate", driver.vehicle.licensePlate)
                    if(!driver.error){
                        startActivity(Intent(applicationContext, HomeActivity::class.java))
                        finish()
                    }else{
                        showToast("Login failed! verifica tus credenciales")
                    }
                }
            }

            override fun onFailure(call: Call<Driver>, t: Throwable) {
                Log.d("MIKE", "isEmailExist onFailure: " + t.message.toString())
            }

        })

    }

    private fun isValidEmail(email: String): Boolean{
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun showToast(text:String){
        Toast.makeText(this@LoginActivity, text, Toast.LENGTH_SHORT).show()
    }
}