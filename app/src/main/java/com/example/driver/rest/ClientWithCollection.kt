package com.example.driver.rest

import com.google.gson.annotations.SerializedName

class ClientWithCollection {
    @SerializedName("clientID")
    var clientID: Int = 0
    
    @SerializedName("clientName")
    var clientName: String = ""
    
    @SerializedName("clientPhone")
    var clientPhone: String = ""
}

