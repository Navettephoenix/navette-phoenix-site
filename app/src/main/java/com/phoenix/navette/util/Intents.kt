package com.phoenix.navette.util

import android.content.Intent
import android.net.Uri

object Intents {
    fun call(number: String): Intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$number")
    }

    fun email(to: String, cc: String? = null, subject: String? = null, body: String? = null): Intent =
        Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$to")
            subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
            if (!cc.isNullOrBlank()) {
                putExtra(Intent.EXTRA_CC, arrayOf(cc))
            }
            body?.let { putExtra(Intent.EXTRA_TEXT, it) }
        }

    fun mapQuery(query: String): Intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
    }
}
