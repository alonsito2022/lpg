package com.example.driver.rest

import com.google.gson.annotations.SerializedName

class RequestDateRange {
    @SerializedName("startDate")
    var startDate: String = ""
    
    @SerializedName("endDate")
    var endDate: String = ""
    
    @SerializedName("driverID")
    var driverID: Int = 0
}

