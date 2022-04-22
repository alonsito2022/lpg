package com.example.driver.model

import com.google.firebase.database.PropertyName

class DriverAccount(){
    var uid : String = ""
    var clients: Map<Int, ClientAccount> = mapOf()

    class ClientAccount(){
        var id: Int? = null
        var recovered: Map<String, Product> = mapOf()
        var pending: MutableMap<String, Product> = mutableMapOf()
        var payed: Map<String, Product> = mapOf()

        class Product(){
            var productKey: String = ""
            var productName: String = ""
            var productPath: String = ""
            var quantity: Int = 0
            var unit: String = ""
        }

    }
}