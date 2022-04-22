package com.example.driver.model

class Profile() {
    var name: String = ""
    var phone: String = ""
    var path: String = ""
    var birthday: String = ""
    var gender: String = ""
    constructor(name: String, phone: String, path: String, birthday: String, gender: String) : this(){
        this.name = name
        this.phone = phone
        this.path = path
        this.birthday = birthday
        this.gender = gender
    }
}