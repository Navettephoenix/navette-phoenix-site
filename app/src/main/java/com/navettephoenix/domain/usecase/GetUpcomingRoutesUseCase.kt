package com.navettephoenix.domain.usecase

import com.navettephoenix.data.model.ShuttleRoute
import com.navettephoenix.data.repository.ShuttleRepository
import kotlinx.coroutines.flow.Flow

class GetUpcomingRoutesUseCase(private val repository: ShuttleRepository) {
    operator fun invoke(): Flow<List<ShuttleRoute>> = repository.observeRoutes()
}
