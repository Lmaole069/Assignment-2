package com.example.assignment2
//data
object UserSession {
    const val STARTING_BALANCE = 500
    const val MAX_PER_RENTAL = 400

    var balance: Int = STARTING_BALANCE
    val bookings = mutableListOf<Booking>()
}