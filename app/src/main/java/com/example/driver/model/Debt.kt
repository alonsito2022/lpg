package com.example.driver.model

class Debt(){
    var id: Int? = null
    var names: String = ""
    var phone: String = ""
    var addresses: ArrayList<Address> = arrayListOf()
    var debt: MutableMap<Int, ProductOwed> = mutableMapOf()
    var totalDebt: Int = 0

    class ProductOwed() {

        var productID: Int = 0
        var productName: String = ""
        var productKey: String = ""
        var units: MutableMap<Int, UnitOwed> = mutableMapOf()

        class UnitOwed() {
            var unitID: Int = 0
            var unitName: String = ""
            var unitPrice: Double = 0.0
            var remainingQuantity: Int = 0
        }

    }
    class Address(){
        var id: Int? = null
        var address: String = ""
        var latitude: Double = 0.0
        var longitude: Double = 0.0
    }
}
