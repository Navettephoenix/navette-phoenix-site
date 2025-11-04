package com.phoenix.navette.util

object PhoneValidator {
    private val allowedPrefixes = setOf("01", "02", "03", "04", "05", "06", "07", "09")

    fun isValidFrenchNumber(raw: String?): Boolean {
        if (raw.isNullOrBlank()) return false
        val normalized = normalize(raw)
        if (normalized.length != 10 || normalized.first() != '0') return false
        val prefix = normalized.substring(0, 2)
        if (prefix !in allowedPrefixes) return false
        return normalized.all { it.isDigit() }
    }

    private fun normalize(value: String): String {
        val cleaned = value.replace("[^0-9+]".toRegex(), "")
        return when {
            cleaned.startsWith("+33") -> {
                var rest = cleaned.removePrefix("+33")
                if (rest.startsWith("0")) {
                    rest = rest.drop(1)
                }
                "0$rest"
            }
            cleaned.startsWith("0033") -> {
                var rest = cleaned.removePrefix("0033")
                if (rest.startsWith("0")) {
                    rest = rest.drop(1)
                }
                "0$rest"
            }
            cleaned.startsWith("0") -> cleaned
            else -> cleaned
        }
    }
}
