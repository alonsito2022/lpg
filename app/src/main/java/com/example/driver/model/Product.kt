package com.example.driver.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

class Product() {
    @get:PropertyName("Uid")
    @set:PropertyName("Uid")
    var uid: String = ""
    val available: Boolean = false
    var name: String = ""
    var brand: String = ""
    @Exclude var modality: String = "" // Refill, Full, BorrowedG, BorrowedB, BorrowedBG
    var category: String = ""
    var valve: String = ""
    var path: String = ""
    var presentation: List<Presentation> = listOf()

    var productID: Int = 0

    /*constructor(
        uid: String,
        name: String,
        path: String
    ) : this() {
        this.uid = uid
        this.name = name
        this.path = path
    }*/

    class Presentation(){
        var price: Double = 0.0
        var unit: String = ""
        var unitName: String = ""

        @Exclude var quantityFilled: Int = 0
        @Exclude var quantityVoid: Int = 0

        var unitID: Int = 0
    }
}