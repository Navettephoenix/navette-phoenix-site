package com.navettephoenix.data.model

data class ShuttleRoute(
    val id: String,
    val departureTime: String,
    val origin: String,
    val destination: String,
    val availableSeats: Int,
    val status: ShuttleStatus
)

enum class ShuttleStatus {
    BOARDING,
    EN_ROUTE,
    LANDED
}
