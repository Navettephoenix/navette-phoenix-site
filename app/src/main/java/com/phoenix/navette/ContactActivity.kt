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

        binding.textTelInfos.setOnClickListener {
            Intents.call(this, getString(R.string.tel_infos))
        }
        binding.textTelResa.setOnClickListener {
            Intents.call(this, getString(R.string.tel_resa))
        }
        binding.textEmail.setOnClickListener {
            Intents.email(
                this,
                to = getString(R.string.mail_resa_to),
                cc = getString(R.string.mail_cc),
                subject = getString(R.string.subject_contact)
            )
        }
        binding.buttonSite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.site_url)))
            startActivity(intent)
        }
    }
}
