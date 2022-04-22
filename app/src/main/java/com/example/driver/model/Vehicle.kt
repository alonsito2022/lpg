package com.example.driver.model

import com.google.firebase.database.PropertyName

class Vehicle() {
    @get:PropertyName("Uid")
    @set:PropertyName("Uid")
    var uid: String = ""
    @get:PropertyName("driver")
    @set:PropertyName("driver")
    var driverKey: String = ""
    @get:PropertyName("license_plate")
    @set:PropertyName("license_plate")
    var licensePlate: String = ""
    var stock: Stock = Stock()

    class Stock(){
        var regular: StockRegular = StockRegular()

        class StockRegular(){

            var filled: Map<String, StockProduct> = mapOf()
            var void: Map<String, StockProduct> = mapOf()

        }
        class StockProduct(){
            var productKey: String = ""
            var productName: String = ""
            var productPath: String = ""
            var quantity: Int = 0
            var unit: String = ""
        }
    }
}