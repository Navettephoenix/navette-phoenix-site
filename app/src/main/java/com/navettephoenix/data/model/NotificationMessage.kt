package com.navettephoenix.data.model

data class NotificationMessage(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: String,
    val read: Boolean = false
)
