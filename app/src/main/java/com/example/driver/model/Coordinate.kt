package com.example.driver.model

class Coordinate() {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    constructor(
        latitude: Double,
        longitude: Double
    ) : this() {
        this.latitude = latitude
        this.longitude = longitude
    }
}