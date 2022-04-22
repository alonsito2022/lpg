package com.example.driver.rest

import com.google.gson.annotations.SerializedName

data class RequestDispatch (
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("firebase_key")
    var firebaseKey: String = "",
    @SerializedName("type")
    var type: String = "",
    @SerializedName("status")
    var status: String = "",
    @SerializedName("created_at")
    var createdAt: String = "",
    @SerializedName("client")
    var client: Int = 0,
    @SerializedName("address")
    var address: Int? = null,
    @SerializedName("driver")
    var driver: Int? = null,
    @SerializedName("distribution")
    var distribution: Int? = null,
)
