package com.example.driver.rest

import com.google.gson.annotations.SerializedName

class CreditCollectionsSummary {
    @SerializedName("status")
    var status: Int = 0
    
    @SerializedName("cashCollected")
    var cashCollected: Double = 0.0
    
    @SerializedName("plinCollected")
    var plinCollected: Double = 0.0
    
    @SerializedName("yapeCollected")
    var yapeCollected: Double = 0.0
    
    @SerializedName("fiseCollected")
    var fiseCollected: Double = 0.0
    
    @SerializedName("sumTotalAmountCollected")
    var sumTotalAmountCollected: Double = 0.0
}

