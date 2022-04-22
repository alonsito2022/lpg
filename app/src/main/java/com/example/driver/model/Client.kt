package com.example.driver.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName

class Client() {
    @SerializedName("id")
    var id: Int? = null
    var names: String = ""
    var phone: String = ""
    var addresses: ArrayList<Address> = arrayListOf()

    class Address(){
        var id: Int? = null
        var address: String = ""
        var latitude: Double = 0.0
        var longitude: Double = 0.0
    }
}
