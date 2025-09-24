package com.navettephoenix.domain.usecase

import com.navettephoenix.data.model.NotificationMessage
import com.navettephoenix.data.repository.ShuttleRepository
import kotlinx.coroutines.flow.Flow

class ObserveNotificationsUseCase(private val repository: ShuttleRepository) {
    operator fun invoke(): Flow<List<NotificationMessage>> = repository.observeNotifications()
}
