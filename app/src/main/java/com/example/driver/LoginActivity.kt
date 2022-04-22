package com.example.driver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.driver.Activity.HomeActivity
import com.example.driver.LocalDatabase.Preference
import com.example.driver.model.Device
import com.example.driver.model.Dispatch
import com.example.driver.model.Driver
import com.example.driver.model.Profile
import com.example.driver.rest.ApiResponse
import com.example.driver.retrofit.ClientService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity(), View.OnClickListener {

//    var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    lateinit var database : FirebaseDatabase
    lateinit var databaseReference: DatabaseReference

    lateinit var preference: Preference

    private var device: Device = Device()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_all_ready_account.setOnClickListener(this)
        btn_create_new_account.setOnClickListener(this)
        btn_login.setOnClickListener(this)
        btn_signup.setOnClickListener(this)

        preference = Preference(applicationContext)



        if(preference.getData("driverID") != ""){
//            preference.clearPreference()
            notification()
            startActivity(Intent(applicationContext, HomeActivity::class.java))
            finish()
        }

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference().child("drivers")
    }

    private fun notification(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if(!task.isSuccessful){
                showToast("Fetching FCM registration token failed. " + task.exception)
                return@OnCompleteListener
            }
            val token = task.result
//            showToast("token. $token")
            device.driverID = preference.getData("driverID").toInt()
            device.deviceToken = token
            device.deviceType = "A"

            val apiInterface = ClientService.create().registerOrUpdateDevice(device)
            apiInterface.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    Log.d("MIKE", response.isSuccessful.toString())
                    val responseDevice = response.body()!!
                    Log.d("MIKE", responseDevice.status)
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.d("MIKE", "responseDevice onFailure: " + t.message.toString())
                }

            })

        })
    }

    override fun onClick(p0: View?) {
        when(p0){
            btn_all_ready_account -> {
                login_layout.visibility = View.VISIBLE
                signup_layout.visibility = View.GONE
            }
            btn_create_new_account -> {
                login_layout.visibility = View.GONE
                signup_layout.visibility = View.VISIBLE
            }
            btn_login -> {
                login()
            }
            btn_signup -> {
                signUp()
            }
        }
    }

    private fun login(){
        var email = email_login.text.toString().trim()
        var password = password_login.text.toString().trim()
        if(email.isEmpty() || password.isEmpty()){
            showToast("Todos los campos son requeridos")
        }else{
            if(isValidEmail(email)){
                isEmailExist(email, password)
            }else{
                showToast("Verifica tu email")
            }
        }
    }

    private fun isEmailExist(email: String, password: String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var list = ArrayList<Driver>()
                var isEmailExist = false

                for(uss in dataSnapshot.children){
                    val value = uss.getValue(Driver::class.java)
//                    Log.d("MIKE", value!!.email)
//                    Log.d("MIKE", value.coordinates.latitude)
                    if(value!!.email == email && value.password == password) {
                        isEmailExist = true

                        preference.saveData("name", value.profile.name)
                        preference.saveData("phone", value.profile.phone)
                        preference.saveData("path", value.profile.path)
                        preference.saveData("vehicleKey", value.vehicleKey)
                        preference.saveData("driverID", value.driverID.toString())
                        preference.saveData("vehicleLicensePlate", value.vehicleLicensePlate)
                        preference.saveData("email", value.email)
                        preference.saveData("uid", value.uid)

                    }
                    list.add(value!!)
                }

                if(isEmailExist){
                    //showToast("Login successfull")
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    finish()
                }else{
                    showToast("Login failed! verifica tus credenciales")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                showToast(databaseError.toException().toString())
            }
        }
        databaseReference.addValueEventListener(postListener)
    }

    private fun signUp(){
        var name = name_signup.text.toString().trim()
        var email = email_signup.text.toString().trim()
        var phone = phone_signup.text.toString().trim()
        var password = password_signup.text.toString().trim()
        var confirmPassword = confirm_password_signup.text.toString().trim()

        if(name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            showToast("Todos los campos son requeridos.")
        }else{
            if(isValidEmail(email)){

                var uid = databaseReference.push().key
                var profile = Profile(name, phone, "", "", "")
                var model = Driver(uid!!, email, phone, profile)

                databaseReference.child(uid!!).setValue(model)
                showToast("Registrado con exito.")

                preference.saveData("name", name)
                preference.saveData("email", email)
                preference.saveData("phone", phone)
                preference.saveData("path", "")
                preference.saveData("uid", uid)

                startActivity(Intent(applicationContext, HomeActivity::class.java))
                finish()


            }else{
                showToast("Verifica tu email.")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean{
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun showToast(text:String){
        Toast.makeText(this@LoginActivity, text, Toast.LENGTH_SHORT).show()
    }
}