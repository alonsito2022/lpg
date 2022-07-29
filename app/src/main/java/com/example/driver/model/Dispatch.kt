package com.example.driver.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName

class Dispatch() {
    @get:PropertyName("Uid")
    @set:PropertyName("Uid")
    @SerializedName("Uid")
    var uid: String = ""
    var clientID: Int = 0
    var clientName: String = ""
    var clientPhone: String = ""
    var addressID: Int = 0
    var addressName: String = ""
    var addressLatitude: Double = 0.0
    var addressLongitude: Double = 0.0
    var dispatchDate: String = ""
    var paymentDate: String = ""
    var yapeTime: String = ""
    var dispatchType: String = ""
    var distributionKey: String = ""
    var distributionID: Int = 0
    var driverID: Int = 0
    var driverName: String = ""
    var driverPhone: String = ""
    var identifier: String = ""
    var status: String = ""
    var totalPrice: Double = 0.0
    var paymentMethods: MutableMap<String, Double> = mutableMapOf()
    var details: MutableList<DispatchDetail> = mutableListOf()

    class DispatchDetail() {
        var productID: Int = 0
        var unitID: Int = 0
        var productKey: String = ""
        var productName: String = ""
        var returnability: String = "" // R, F, PG, PB, PBG
        var quantity: Int = 0
        var oldClientPendingQuantity: Int = 0
        var oldQuantityFilled: Int = 0
        var oldQuantityVoid: Int = 0
        var price: Double = 0.0
        var subtotal: Double = 0.0
        var unit: String = ""
        var unitName: String = ""
        @Exclude var modality: String = "" // Recarga, Completo, Liquido Prestado, Fierro Prestado, Completo Prestado
        @Exclude var quantityMax: Int = 0
    }

}