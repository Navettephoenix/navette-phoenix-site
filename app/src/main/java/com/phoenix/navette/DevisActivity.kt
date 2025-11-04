package com.phoenix.navette

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.phoenix.navette.databinding.ActivityDevisBinding
import com.phoenix.navette.util.Intents
import com.phoenix.navette.util.PhoneValidator
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

class DevisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevisBinding
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private val webhookUrl: String by lazy { BuildConfig.NP_WEBHOOK }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdapters()
        setupPickers()

        binding.buttonMap.setOnClickListener {
            val query = binding.editDestination.text?.toString().orEmpty()
            if (query.isNotBlank()) {
                Intents.mapQuery(this, query)
            }
        }

        binding.buttonEnvoyer.setOnClickListener {
            submitForm()
        }
    }

    private fun setupAdapters() {
        val passengers = resources.getStringArray(R.array.passagers_1_4)
        binding.autoPassagers.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, passengers)
        )
        binding.autoPassagers.setText(passengers.first(), false)

        val options = resources.getStringArray(R.array.options_devis)
        binding.autoOption.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        )
    }

    private fun setupPickers() {
        binding.editDate.setOnClickListener { showDatePicker() }
        binding.editHeure.setOnClickListener { showTimePicker() }
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
                binding.editHeure.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun submitForm() {
        val name = binding.editNom.text?.toString().orEmpty().trim()
        val phone = binding.editTel.text?.toString().orEmpty().trim()
        val email = binding.editEmail.text?.toString().orEmpty().trim()
        val depart = binding.editDepart.text?.toString().orEmpty().trim()
        val destination = binding.editDestination.text?.toString().orEmpty().trim()
        val passengers = binding.autoPassagers.text?.toString().orEmpty().trim()
        val option = binding.autoOption.text?.toString().orEmpty().trim()
        val date = selectedDate
        val time = selectedTime

        if (name.isBlank() || depart.isBlank() || destination.isBlank() || passengers.isBlank() || option.isBlank()) {
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
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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

        binding.buttonEnvoyer.isEnabled = false
        Toast.makeText(this, R.string.toast_request_processing, Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val payload = JSONObject().apply {
                        put("type", "devis_hors_secteur")
                        put("nom", name)
                        put("telephone", phone)
                        put("email", email)
                        put("depart", depart)
                        put("destination", destination)
                        put("date", date)
                        put("heure", time)
                        put("passagers", passengers)
                        put("option", option)
                        if (BuildConfig.NP_TOKEN.isNotBlank()) {
                            put("token", BuildConfig.NP_TOKEN)
                        }
                    }
                    val body = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
                    val request = Request.Builder()
                        .url(webhookUrl)
                        .post(body)
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            error("HTTP ${'$'}{response.code}")
                        }
                    }
                }
            }

            binding.buttonEnvoyer.isEnabled = true

            result.onSuccess {
                Toast.makeText(this@DevisActivity, R.string.toast_request_sent, Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(this@DevisActivity, R.string.toast_network_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
