package com.navettephoenix.data.repository

import com.navettephoenix.data.model.NotificationMessage
import com.navettephoenix.data.model.Reservation
import com.navettephoenix.data.model.ReservationStatus
import com.navettephoenix.data.model.ShuttleRoute
import com.navettephoenix.data.model.ShuttleStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class ShuttleRepository {

    private val routes = MutableStateFlow(generateRoutes())
    private val notifications = MutableStateFlow<List<NotificationMessage>>(emptyList())

    fun observeRoutes(): Flow<List<ShuttleRoute>> = routes.asStateFlow()

    fun observeNotifications(): Flow<List<NotificationMessage>> = notifications.asStateFlow()

    suspend fun createReservation(passengerName: String, seats: Int): Reservation {
        delay(500)
        val route = routes.value.first()
        val reservation = Reservation(
            reservationId = UUID.randomUUID().toString(),
            passengerName = passengerName,
            routeId = route.id,
            seats = seats,
            status = ReservationStatus.CONFIRMED
        )
        notifications.update { current ->
            current + NotificationMessage(
                id = UUID.randomUUID().toString(),
                title = "Réservation confirmée",
                body = "Votre navette pour ${route.destination} est prête.",
                timestamp = "${System.currentTimeMillis()}"
            )
        }
        routes.update { currentRoutes ->
            currentRoutes.mapIndexed { index, shuttleRoute ->
                if (index == 0) {
                    shuttleRoute.copy(
                        availableSeats = (shuttleRoute.availableSeats - seats).coerceAtLeast(0),
                        status = ShuttleStatus.BOARDING
                    )
                } else {
                    shuttleRoute
                }
            }
        }
        return reservation
    }

    suspend fun refreshRoutes() {
        delay(350)
        routes.update { current ->
            current.map { route ->
                if (route == current.first()) {
                    route.copy(status = ShuttleStatus.BOARDING)
                } else route
            }
        }
    }

    private fun generateRoutes(): List<ShuttleRoute> = listOf(
        ShuttleRoute(
            id = UUID.randomUUID().toString(),
            departureTime = "08:30",
            origin = "Centre-ville",
            destination = "Aéroport",
            availableSeats = 12,
            status = ShuttleStatus.BOARDING
        ),
        ShuttleRoute(
            id = UUID.randomUUID().toString(),
            departureTime = "10:15",
            origin = "Aéroport",
            destination = "Centre-ville",
            availableSeats = 18,
            status = ShuttleStatus.LANDED
        )
    )
}
