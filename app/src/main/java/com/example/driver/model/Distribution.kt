package com.example.driver.model

import com.google.firebase.database.PropertyName

class Distribution() {
    @get:PropertyName("Uid")
    @set:PropertyName("Uid")
    var uid: String = ""
    var code: String = ""
    var status: String = ""
    var vehicle: String = ""

    lateinit var departure: Departure

    var supplying: Map<String, ProductItem> = mapOf()

    class Departure(){
        var sales: Sale = Sale()
        var datetime: String = ""

        class Sale(){
            var refill: Map<String, ProductItem> = mapOf()
            var fullCylinder: Map<String, ProductItem> = mapOf()
            var borrowed: Map<String, ProductItem> = mapOf()
        }

    }

    class ProductItem(){
        var productKey: String = ""
        var productName: String = ""
        var productPath: String = ""
        var quantity: Int = 0
        var unit: String = ""
        var amount: Double = 0.0
    }


}