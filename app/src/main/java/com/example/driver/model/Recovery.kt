package com.example.driver.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName

class Recovery() {
    @get:PropertyName("Uid")
    @set:PropertyName("Uid")
    @SerializedName("Uid")
    var uid: String = ""
    var clientID: Int = 0
    var driverID: Int = 0
    var distributionID: Int = 0
    var distributionKey: String = ""
    var paymentMethods: MutableMap<String, Double> = mutableMapOf()
    var details: MutableList<RecoveryDetail> = mutableListOf()
    var concept: String = "03"  // 03: PAYED, 02: RECOVERY

    var clientName: String = ""
    var totalPrice: Double = 0.0
    var recoveryDate: String = ""

    class RecoveryDetail() {
        var productID: Int = 0
        var productKey: String = ""
        var productName: String = ""
        var unitID: Int = 0
        var quantity: Int = 0
        var price: Double = 0.0
        var subtotal: Double = 0.0

    }

}