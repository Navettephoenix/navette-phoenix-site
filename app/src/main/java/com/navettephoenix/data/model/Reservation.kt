package com.navettephoenix.data.model

data class Reservation(
    val reservationId: String,
    val passengerName: String,
    val routeId: String,
    val seats: Int,
    val status: ReservationStatus
)

enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
