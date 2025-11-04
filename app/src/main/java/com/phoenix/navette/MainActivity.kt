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

        setupClicks()
    }

    private fun setupClicks() = with(binding) {
        buttonReservation.setOnClickListener {
            startActivity(Intent(this@MainActivity, ReservationActivity::class.java))
        }
        buttonDevis.setOnClickListener {
            startActivity(Intent(this@MainActivity, DevisActivity::class.java))
        }
        buttonInfos.setOnClickListener {
            startActivity(InfoClairActivity.newIntent(this@MainActivity, InfoClairActivity.Section.INFO))
        }
        buttonZones.setOnClickListener {
            startActivity(InfoClairActivity.newIntent(this@MainActivity, InfoClairActivity.Section.ZONE))
        }
        buttonContact.setOnClickListener {
            startActivity(Intent(this@MainActivity, ContactActivity::class.java))
        }
        buttonPrivacy.setOnClickListener {
            startActivity(Intent(this@MainActivity, PrivacyActivity::class.java))
        }
        buttonSos.setOnClickListener {
            startActivity(Intents.call("112"))
        }
    }
}
