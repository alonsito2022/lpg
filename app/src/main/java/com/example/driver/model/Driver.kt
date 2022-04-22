package com.example.driver.model
import com.google.firebase.database.PropertyName

class Driver() {
    @get:PropertyName("Uid")
    @set:PropertyName("Uid")
    var uid: String = ""
    val available: Boolean = false
    lateinit var coordinates: Coordinate
    var email: String = ""
    @get:PropertyName("is_online")
    @set:PropertyName("is_online")
    var isOnline: Boolean = false
    lateinit var password: String
    var profile: Profile = Profile()

    var distributionKey: String = ""
    var vehicleKey: String = ""
    var vehicleLicensePlate: String = ""

    var distributionID: Int = 0
    var driverID: Int = 0
    var distributionDatetime: String = ""
    var distributionStatusDisplay: String = ""

    constructor(
        uid: String,
        email: String,
        password: String,
        profile: Profile
    ) : this() {
        this.uid = uid
        this.email = email
        this.password = password
        this.profile = profile
    }
}