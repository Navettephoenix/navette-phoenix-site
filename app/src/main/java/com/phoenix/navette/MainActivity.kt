package com.phoenix.navette

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.phoenix.navette.databinding.ActivityMainBinding
import com.phoenix.navette.util.Intents

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonTad.setOnClickListener {
            startActivity(Intent(this, ReservationActivity::class.java))
        }
        binding.buttonDevis.setOnClickListener {
            startActivity(Intent(this, DevisActivity::class.java))
        }
        binding.buttonInfos.setOnClickListener {
            startActivity(Intent(this, InfoClairActivity::class.java))
        }
        binding.buttonZoneGares.setOnClickListener {
            val intent = Intent(this, InfoClairActivity::class.java).apply {
                putExtra(InfoClairActivity.EXTRA_SECTION, InfoClairActivity.SECTION_GARES)
            }
            startActivity(intent)
        }
        binding.buttonContact.setOnClickListener {
            startActivity(Intent(this, ContactActivity::class.java))
        }
        binding.buttonPrivacy.setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }
        binding.buttonSos.setOnClickListener {
            Intents.call(this, "112")
        }
    }
}
