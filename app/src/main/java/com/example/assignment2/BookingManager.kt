package com.example.assignment2

object BookingManager {
    //true or false
    fun book(car: Car, renterName: String, days: Int): Boolean {
        val total = days * car.dailyCost

        if (car.isRented) return false
        if (total > UserSession.MAX_PER_RENTAL) return false
        if (total > UserSession.balance) return false

        UserSession.balance -= total
        UserSession.bookings.add(Booking(car, renterName, days, total))
        CarRepository.setRented(car.id, true)
        return true
    }
    //refund
    fun cancel(booking: Booking) {
        if (UserSession.bookings.remove(booking)) {
            UserSession.balance += booking.total
            CarRepository.setRented(booking.car.id, false)
        }
    }
}