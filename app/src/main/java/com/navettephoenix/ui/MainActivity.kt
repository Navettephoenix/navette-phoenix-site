package com.navettephoenix.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.navettephoenix.R
import com.navettephoenix.data.model.ShuttleStatus
import com.navettephoenix.data.repository.ShuttleRepository
import com.navettephoenix.databinding.ActivityMainBinding
import com.navettephoenix.domain.usecase.CreateReservationUseCase
import com.navettephoenix.domain.usecase.GetUpcomingRoutesUseCase
import com.navettephoenix.domain.usecase.ObserveNotificationsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val repository = ShuttleRepository()
    private val getUpcomingRoutes = GetUpcomingRoutesUseCase(repository)
    private val createReservation = CreateReservationUseCase(repository)
    private val observeNotifications = ObserveNotificationsUseCase(repository)

    private var uiState: ShuttleUiState = ShuttleUiState()
    private var stateCollectionJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupListeners()
        observeShuttleData()
    }

    private fun setupListeners() {
        binding.reserveButton.setOnClickListener {
            lifecycleScope.launch {
                binding.reserveButton.isEnabled = false
                createReservation(passengerName = "Invité", seats = 1)
                uiState = uiState.copy(
                    lastReservationMessage = getString(R.string.status_reservation_confirmed)
                )
                renderState(uiState)
                Snackbar.make(
                    binding.root,
                    getString(R.string.message_reservation),
                    Snackbar.LENGTH_LONG
                ).show()
                binding.reserveButton.isEnabled = true
            }
        }

        binding.viewScheduleButton.setOnClickListener {
            Snackbar.make(binding.root, R.string.message_schedule, Snackbar.LENGTH_SHORT).show()
        }

        binding.notificationsButton.setOnClickListener {
            val message = if (uiState.notifications.isEmpty()) {
                getString(R.string.message_notifications)
            } else {
                uiState.notifications.first().body
            }
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeShuttleData() {
        stateCollectionJob?.cancel()
        stateCollectionJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                uiState = uiState.copy(isLoading = true)
                renderState(uiState)

                launch {
                    getUpcomingRoutes().collect { routes ->
                        uiState = uiState.copy(
                            routes = routes,
                            isLoading = false,
                            errorMessage = null
                        )
                        renderState(uiState)
                    }
                }

                launch {
                    observeNotifications().collect { notifications ->
                        uiState = uiState.copy(notifications = notifications)
                        renderState(uiState)
                    }
                }

                launch {
                    repository.refreshRoutes()
                }
            }
        }
    }

    private fun renderState(state: ShuttleUiState) {
        val nextRoute = state.routes.firstOrNull()
        val departureText = if (nextRoute != null) {
            getString(
                R.string.next_departure_format,
                nextRoute.departureTime,
                nextRoute.origin,
                nextRoute.destination,
                nextRoute.availableSeats
            )
        } else {
            getString(R.string.status_loading)
        }
        binding.nextDepartureText.text = departureText

        val chipText = when {
            state.isLoading -> getString(R.string.status_loading)
            nextRoute == null -> getString(R.string.status_loading)
            nextRoute.status == ShuttleStatus.BOARDING -> getString(R.string.status_ready)
            else -> nextRoute.status.name.lowercase().replaceFirstChar { it.titlecase() }
        }
        binding.statusChip.text = chipText

        val notificationsCount = state.notifications.size
        val baseLabel = getString(R.string.action_notifications)
        binding.notificationsButton.text =
            if (notificationsCount > 0) "$baseLabel (${notificationsCount})" else baseLabel

        state.lastReservationMessage?.let {
            binding.statusChip.text = getString(R.string.status_reservation_confirmed)
        }

        binding.reserveButton.isEnabled = !state.isLoading
    }
}
