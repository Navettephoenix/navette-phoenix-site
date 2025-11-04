package com.phoenix.navette.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object Intents {
    fun call(context: Context, number: String) {
        val uri = Uri.parse("tel:${number.trim()}")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        context.startActivity(intent)
    }

    fun email(context: Context, to: String, cc: String? = null, subject: String? = null, body: String? = null) {
        val uriBuilder = StringBuilder("mailto:").append(to)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(uriBuilder.toString())
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            cc?.let { putExtra(Intent.EXTRA_CC, arrayOf(it)) }
            subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
            body?.let { putExtra(Intent.EXTRA_TEXT, it) }
        }
        context.startActivity(intent)
    }

    fun mapQuery(context: Context, query: String) {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}
