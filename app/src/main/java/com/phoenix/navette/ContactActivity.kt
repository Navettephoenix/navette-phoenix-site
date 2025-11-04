package com.phoenix.navette

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.phoenix.navette.databinding.ActivityContactBinding
import com.phoenix.navette.util.Intents

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupInteractions()
    }

    private fun setupInteractions() = with(binding) {
        cardRenseignements.setOnClickListener {
            startActivity(Intents.call(getString(R.string.tel_infos)))
        }
        cardReservations.setOnClickListener {
            startActivity(Intents.call(getString(R.string.tel_resa)))
        }
        cardEmail.setOnClickListener {
            val intent = Intents.email(
                to = getString(R.string.mail_resa_to),
                cc = getString(R.string.mail_cc),
                subject = getString(R.string.subject_resa)
            )
            startActivity(intent)
        }
        cardSite.setOnClickListener {
            val url = getString(R.string.site_url)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
