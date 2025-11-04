package com.phoenix.navette

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.phoenix.navette.databinding.ActivityInfoClairBinding

class InfoClairActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoClairBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoClairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val communes = resources.getStringArray(R.array.communes_perimetre)
        binding.perimeterList.text = communes.joinToString(separator = "\n") { "• $it" }

        binding.buttonToDevis.setOnClickListener {
            startActivity(android.content.Intent(this, DevisActivity::class.java))
        }

        val section = intent.getStringExtra(EXTRA_SECTION)
        binding.root.post {
            when (section) {
                SECTION_GARES -> binding.root.smoothScrollTo(0, binding.garesTitle.top)
                else -> {}
            }
        }
    }

    companion object {
        const val EXTRA_SECTION = "extra_section"
        const val SECTION_GARES = "section_gares"
    }
}
