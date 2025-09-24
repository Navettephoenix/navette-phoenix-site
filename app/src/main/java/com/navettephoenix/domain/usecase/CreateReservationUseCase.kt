package com.navettephoenix.domain.usecase

import com.navettephoenix.data.model.Reservation
import com.navettephoenix.data.repository.ShuttleRepository

class CreateReservationUseCase(private val repository: ShuttleRepository) {
    suspend operator fun invoke(passengerName: String, seats: Int): Reservation {
        return repository.createReservation(passengerName, seats)
    }
}
