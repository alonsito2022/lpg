package com.example.driver.rest

import com.google.gson.annotations.SerializedName

class RequestDispatchDetail (
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("quantity")
    var quantity: String = "",
    @SerializedName("price")
    var price: String = "",
    @SerializedName("subtotal")
    var subtotal: String = "",
    @SerializedName("returnability")
    var returnability: String = "",
    @SerializedName("dispatch")
    var dispatch: Int? = null,
    @SerializedName("product")
    var product: Int? = null,
    @SerializedName("unit")
    var unit: Int? = null
)