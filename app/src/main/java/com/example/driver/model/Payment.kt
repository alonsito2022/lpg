package com.example.driver.model

import com.google.firebase.database.PropertyName

class Payment() {
    var dispatches: MutableMap<String, PaymentDispatch> = mutableMapOf()
    var total: Double = 0.0
    var wayPays: MutableMap<String, Double> = mutableMapOf()

    class PaymentDispatch() {

        var amounts: MutableMap<String, Double> = mutableMapOf()
        var total: Double? = null

        var clientName: String = ""
        var dispatchDate: String = ""

    }
}