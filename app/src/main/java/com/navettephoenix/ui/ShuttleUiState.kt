package com.navettephoenix.ui

import com.navettephoenix.data.model.NotificationMessage
import com.navettephoenix.data.model.ShuttleRoute

data class ShuttleUiState(
    val isLoading: Boolean = true,
    val routes: List<ShuttleRoute> = emptyList(),
    val notifications: List<NotificationMessage> = emptyList(),
    val lastReservationMessage: String? = null,
    val errorMessage: String? = null
)
