package com.example.driver.model

class Driver() {

    var uid: String = ""
    lateinit var coordinates: Coordinate

    var profile: Profile = Profile()

    var distributionKey: String = ""
    var vehicleKey: String = ""
    var vehicleLicensePlate: String = ""

    var distributionID: Int = 0

    var distributionDatetime: String = ""
    var distributionStatusDisplay: String = ""

    var driverID: Int = 0
    var names: String = ""
    var phone: String = ""
    var email: String = ""
    var password: String = ""
    var birthdate: String = ""
    var available: Boolean = false

    var vehicle : Vehicle = Vehicle()

    var error: Boolean = false
    var message: String = ""

    constructor(id: Int) : this() {
        this.driverID = id
    }

    constructor(email: String, password: String) : this() {
        this.email = email
        this.password = password
    }

    class Vehicle(){
        var vehicleID: Int = 0
        var licensePlate: String = ""
        var odometer: String = ""
        var distribution: Distribution = Distribution()
        var stockRegular: MutableList<StockRegular> = mutableListOf()

        class StockRegular(){
            var productID: Int = 0
            var productName: String = ""
            var productPath: String = ""
            var productBrand: String = "C"
            var productCategory: String = "B"
            var productValve: String = ""

            var voidUnitID: Int = 0
            var voidUnitName: String = ""
            var voidStock: Int = 0

            var filledUnitID: Int = 0
            var filledUnitName: String = ""
            var filledStock: Int = 0
            var tariff: MutableList<Tariff> = mutableListOf()

            var returnability: String = "R"

            class Tariff(){
                var presentationID: Int = 0
                var unitID: Int = 0
                var unitName: String = ""
                var price: Double = 0.0
            }
        }

        class Distribution(){
            var distributionID: Int = 0
            var distributionEntranceDatetime: String = ""
            var distributionDepartureDatetime: String = ""
            var distributionStatus: String = ""
        }
    }
}