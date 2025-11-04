package com.phoenix.navette

import android.os.Bundle
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import com.phoenix.navette.databinding.ActivityPrivacyBinding

class PrivacyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webPrivacy.settings.apply {
            javaScriptEnabled = false
            cacheMode = WebSettings.LOAD_DEFAULT
            domStorageEnabled = false
        }
        binding.webPrivacy.loadUrl(getString(R.string.privacy_url))
    }

    override fun onBackPressed() {
        if (binding.webPrivacy.canGoBack()) {
            binding.webPrivacy.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
