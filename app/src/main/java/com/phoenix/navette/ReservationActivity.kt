package com.phoenix.navette

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.phoenix.navette.databinding.ActivityReservationBinding
import com.phoenix.navette.network.NavetteApi
import com.phoenix.navette.util.Intents
import com.phoenix.navette.util.PhoneValidator
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReservationBinding
    private var selectedDate: LocalDate? = null
    private var selectedTime: LocalTime? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupDropdowns()
        setupPickers()
        setupActions()
        updateTariff(1)
    }

    private fun setupDropdowns() = with(binding) {
        val communes = resources.getStringArray(R.array.communes_perimetre)
        val communeAdapter = ArrayAdapter(this@ReservationActivity, android.R.layout.simple_list_item_1, communes)
        autoCommune.setAdapter(communeAdapter)
        autoCommune.setOnClickListener { autoCommune.showDropDown() }
        autoCommune.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) autoCommune.showDropDown() }

        val passengers = resources.getStringArray(R.array.passagers_1_4)
        val passengerAdapter = ArrayAdapter(this@ReservationActivity, android.R.layout.simple_list_item_1, passengers)
        autoPassengers.setAdapter(passengerAdapter)
        autoPassengers.setText(passengers.first(), false)
        autoPassengers.setOnItemClickListener { _, _, position, _ ->
            val value = passengers[position].toIntOrNull() ?: 1
            updateTariff(value)
        }
        autoPassengers.setOnClickListener { autoPassengers.showDropDown() }
        autoPassengers.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) autoPassengers.showDropDown() }
    }

    private fun setupPickers() = with(binding) {
        editDate.setOnClickListener { showDatePicker() }
        editTime.setOnClickListener { showTimePicker() }
    }

    private fun setupActions() = with(binding) {
        buttonMap.setOnClickListener {
            val destination = editDestination.text?.toString().orEmpty()
            if (destination.isBlank()) {
                Toast.makeText(this@ReservationActivity, R.string.message_map_destination_required, Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intents.mapQuery(destination))
            }
        }

        buttonSend.setOnClickListener { submitForm() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            binding.editDate.setText(selectedDate?.format(dateFormatter))
        }
        DatePickerDialog(
            this,
            listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = calendar.timeInMillis
        }.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            selectedTime = LocalTime.of(hourOfDay, minute)
            binding.editTime.setText(selectedTime?.format(timeFormatter))
        }
        TimePickerDialog(
            this,
            listener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun submitForm() {
        val name = binding.editName.text?.toString()?.trim().orEmpty()
        val phone = binding.editPhone.text?.toString()?.trim().orEmpty()
        val email = binding.editEmail.text?.toString()?.trim().orEmpty()
        val commune = binding.autoCommune.text?.toString()?.trim().orEmpty()
        val depart = binding.editDepart.text?.toString()?.trim().orEmpty()
        val destination = binding.editDestination.text?.toString()?.trim().orEmpty()
        val passengers = binding.autoPassengers.text?.toString()?.toIntOrNull() ?: 1

        if (!validate(name, phone, email, commune, depart, destination)) {
            return
        }

        if (!NavetteApi.isConfigured()) {
            Snackbar.make(binding.root, R.string.message_configure_webhook, Snackbar.LENGTH_LONG).show()
            return
        }

        setLoading(true)
        val fields = mapOf(
            "nom" to name,
            "telephone" to phone,
            "email" to email.ifBlank { null },
            "commune" to commune,
            "depart" to depart,
            "destination" to destination,
            "date" to selectedDate?.toString(),
            "date_affichee" to binding.editDate.text?.toString(),
            "heure" to binding.editTime.text?.toString(),
            "passagers" to passengers,
            "tarif_estime" to passengers * 5
        )

        NavetteApi.post("reservation_tad", fields) { success, error ->
            runOnUiThread {
                setLoading(false)
                if (success) {
                    Snackbar.make(binding.root, R.string.message_send_success, Snackbar.LENGTH_LONG).show()
                    binding.buttonSend.isEnabled = false
                } else {
                    val message = error ?: getString(R.string.message_send_failed)
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validate(
        name: String,
        phone: String,
        email: String,
        commune: String,
        depart: String,
        destination: String
    ): Boolean {
        var isValid = true

        binding.layoutCommune.error = null
        binding.layoutPassengers.error = null

        if (name.isBlank()) {
            binding.editName.error = getString(R.string.error_required)
            isValid = false
        } else {
            binding.editName.error = null
        }

        if (!PhoneValidator.isValid(phone)) {
            binding.editPhone.error = getString(R.string.error_phone)
            isValid = false
        } else {
            binding.editPhone.error = null
        }

        if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.error = getString(R.string.error_email)
            isValid = false
        } else {
            binding.editEmail.error = null
        }

        val communes = resources.getStringArray(R.array.communes_perimetre)
        if (commune.isBlank() || !communes.contains(commune)) {
            binding.layoutCommune.error = getString(R.string.error_commune)
            isValid = false
        } else {
            binding.layoutCommune.error = null
        }

        if (depart.isBlank()) {
            binding.editDepart.error = getString(R.string.error_required)
            isValid = false
        } else {
            binding.editDepart.error = null
        }

        if (destination.isBlank()) {
            binding.editDestination.error = getString(R.string.error_required)
            isValid = false
        } else {
            binding.editDestination.error = null
        }

        if (selectedDate == null) {
            binding.editDate.error = getString(R.string.error_required)
            isValid = false
        } else {
            binding.editDate.error = null
        }

        if (selectedTime == null) {
            binding.editTime.error = getString(R.string.error_required)
            isValid = false
        } else {
            binding.editTime.error = null
        }

        return isValid
    }

    private fun updateTariff(passengers: Int) {
        val total = passengers * 5
        binding.textTarif.text = getString(R.string.price_per_seat_notice, passengers.toString(), total.toString())
    }

    private fun setLoading(loading: Boolean) {
        binding.progressIndicator.isVisible = loading
        binding.buttonSend.isEnabled = !loading
        binding.buttonMap.isEnabled = !loading
    }
}
