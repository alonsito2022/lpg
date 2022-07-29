package com.example.driver.model

class Inventory (){

    var stockFilled: ArrayList<Product> = arrayListOf()
    var stockVoid: ArrayList<Product> = arrayListOf()

    class Product(){
        var productKey: String = ""
        var productName: String = ""
        var productPath: String = ""
        var quantity: Int = 0
        var unitName: String = ""
    }
}