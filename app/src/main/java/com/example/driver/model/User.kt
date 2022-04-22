package com.example.driver.model

class User(){
    lateinit var name: String
    lateinit var email: String
    lateinit var password: String
    lateinit var id: String

    constructor(name: String, email: String, password: String, id: String) : this(){
        this.email = email
        this.password = password
        this.id = id
        this.name = name
    }
}