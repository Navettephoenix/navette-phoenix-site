package com.phoenix.navette

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.phoenix.navette.databinding.ActivityReservationBinding
import com.phoenix.navette.util.Intents
import com.phoenix.navette.util.PhoneValidator
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReservationBinding
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    private var selectedPassengers: Int = 1
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private val webhookUrl: String by lazy { BuildConfig.NP_WEBHOOK }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdapters()
        setupPickers()
        updatePriceText()

        binding.buttonMap.setOnClickListener {
            val destination = binding.editDropoff.text?.toString().orEmpty()
            if (destination.isNotBlank()) {
                Intents.mapQuery(this, destination)
            } else {
                val fallback = binding.editPickup.text?.toString().takeUnless { it.isNullOrBlank() }
                    ?: getString(R.string.gare_default)
                Intents.mapQuery(this, fallback)
            }
        }

        binding.buttonSubmit.setOnClickListener {
            submitForm()
        }
    }

    private fun setupAdapters() {
        val communes = resources.getStringArray(R.array.communes_perimetre)
        binding.autoCommune.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, communes)
        )
        val passengers = resources.getStringArray(R.array.passagers_1_4)
        binding.autoPassengers.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, passengers)
        )
        binding.autoPassengers.setOnItemClickListener { _, _, position, _ ->
            selectedPassengers = passengers[position].toInt()
            updatePriceText()
        }
        binding.autoPassengers.setText(passengers.first(), false)
    }

    private fun setupPickers() {
        binding.editDate.setOnClickListener {
            showDatePicker()
        }
        binding.editTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.editDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                binding.editTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updatePriceText() {
        val total = 5 * selectedPassengers
        val formattedTotal = NumberFormat.getNumberInstance(Locale.FRANCE).format(total)
        binding.textPrice.text = getString(R.string.price_per_seat_notice, selectedPassengers, formattedTotal)
    }

    private fun submitForm() {
        val name = binding.editName.text?.toString().orEmpty().trim()
        val phone = binding.editPhone.text?.toString().orEmpty().trim()
        val email = binding.editEmail.text?.toString().orEmpty().trim()
        val commune = binding.autoCommune.text?.toString().orEmpty().trim()
        val pickup = binding.editPickup.text?.toString().orEmpty().trim()
        val dropoff = binding.editDropoff.text?.toString().orEmpty().trim()
        val date = selectedDate
        val time = selectedTime
        val passengers = selectedPassengers

        if (name.isBlank() || commune.isBlank() || pickup.isBlank() || dropoff.isBlank()) {
            Toast.makeText(this, R.string.toast_missing_fields, Toast.LENGTH_SHORT).show()
            return
        }
        if (phone.isBlank()) {
            Toast.makeText(this, R.string.toast_phone_required, Toast.LENGTH_SHORT).show()
            return
        }
        if (!PhoneValidator.isValidFrenchNumber(phone)) {
            Toast.makeText(this, R.string.toast_invalid_phone, Toast.LENGTH_SHORT).show()
            return
        }
        if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.toast_invalid_email, Toast.LENGTH_SHORT).show()
            return
        }
        if (date.isNullOrBlank()) {
            Toast.makeText(this, R.string.toast_pick_date, Toast.LENGTH_SHORT).show()
            return
        }
        if (time.isNullOrBlank()) {
            Toast.makeText(this, R.string.toast_pick_time, Toast.LENGTH_SHORT).show()
            return
        }

        if (webhookUrl.isBlank()) {
            Toast.makeText(this, R.string.toast_network_error, Toast.LENGTH_SHORT).show()
            return
        }

        binding.buttonSubmit.isEnabled = false
        Toast.makeText(this, R.string.toast_request_processing, Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val payload = JSONObject().apply {
                        put("type", "reservation_tad")
                        put("nom", name)
                        put("telephone", phone)
                        put("email", email)
                        put("commune", commune)
                        put("prise_en_charge", pickup)
                        put("depose", dropoff)
                        put("date", date)
                        put("heure", time)
                        put("passagers", passengers)
                        if (BuildConfig.NP_TOKEN.isNotBlank()) {
                            put("token", BuildConfig.NP_TOKEN)
                        }
                    }
                    val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
                    val request = Request.Builder()
                        .url(webhookUrl)
                        .post(requestBody)
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            error("HTTP ${'$'}{response.code}")
                        }
                    }
                }
            }

            binding.buttonSubmit.isEnabled = true

            result.onSuccess {
                Toast.makeText(this@ReservationActivity, R.string.toast_request_sent, Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(this@ReservationActivity, R.string.toast_network_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
