package com.example.driver.rest
import com.google.gson.annotations.SerializedName
class RequestPaymentMethod (

    @SerializedName("status")
    var status: String = "",
    @SerializedName("cash")
    var cash: String = "",
    @SerializedName("plin")
    var plin: String = "",
    @SerializedName("yape")
    var yape: String = "",
    @SerializedName("credit")
    var credit: String = "",
    @SerializedName("sumTotalAmount")
    var sumTotalAmount: String = "",

)

