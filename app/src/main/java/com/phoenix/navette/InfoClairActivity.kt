package com.phoenix.navette

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.phoenix.navette.databinding.ActivityInfoClairBinding

class InfoClairActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoClairBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoClairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val section = intent?.getSerializableExtra(EXTRA_SECTION) as? Section ?: Section.INFO
        populateContent()
        binding.buttonDevis.setOnClickListener {
            startActivity(Intent(this, DevisActivity::class.java))
        }

        binding.root.post {
            if (section == Section.ZONE) {
                binding.scrollView.smoothScrollTo(0, binding.sectionZone.top)
            }
        }
    }

    private fun populateContent() {
        val gares = resources.getStringArray(R.array.gares_perimetre)
        binding.textGares.text = gares.joinToString(separator = "\n")

        val communes = resources.getStringArray(R.array.communes_perimetre)
        binding.textCommunes.text = communes.joinToString(separator = " \u2022 ")
    }

    companion object {
        private const val EXTRA_SECTION = "extra_section"

        fun newIntent(context: Context, section: Section): Intent =
            Intent(context, InfoClairActivity::class.java).apply {
                putExtra(EXTRA_SECTION, section)
            }
    }

    enum class Section {
        INFO, ZONE
    }
}
