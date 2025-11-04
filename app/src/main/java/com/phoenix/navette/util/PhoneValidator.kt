package com.phoenix.navette.util

object PhoneValidator {
    private val regex = Regex(
        pattern = "^(?:0(?:[1-5]|6|7|9)(?:[\\s.-]?\\d{2}){4}|\\+33\\s?(?:[1-5]|6|7|9)(?:[\\s.-]?\\d{2}){4})$",
        option = RegexOption.IGNORE_CASE
    )

    fun isValid(number: String?): Boolean {
        if (number.isNullOrBlank()) return false
        val normalized = number.trim()
        return regex.matches(normalized)
    }
}
