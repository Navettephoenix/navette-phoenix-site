package com.phoenix.navette

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.phoenix.navette.databinding.ActivityPrivacyBinding

class PrivacyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.webView.webViewClient = WebViewClient()
        binding.webView.settings.apply {
            builtInZoomControls = true
            displayZoomControls = false
        }
        binding.webView.loadUrl(getString(R.string.privacy_url))
    }
}
