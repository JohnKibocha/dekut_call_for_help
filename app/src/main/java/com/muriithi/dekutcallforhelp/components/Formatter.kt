// app/src/main/java/com/muriithi/dekutcallforhelp/components/Formatter.kt
package com.muriithi.dekutcallforhelp.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formatter class to handle the formatting of various components for UI display.
 */
class Formatter {

    private val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())
    /**
     * Formats a Date object to a string in the format dd-MMM-yyyy HH:mm.
     * @example "01-Jan-2021 21:14"
     * @param date The Date object to format.
     * @return The formatted date string.
     */
    fun formatDateToString(date: Date?): String {
        return date?.let { dateFormat.format(it) } ?: ""
    }

    /**
     * Parses a date string to a Date object in the date object format Ddd Mmm DD HH:MM:SS GMT YYYY.
     * @example "Mon Jan 15 10:15:00 GMT 2024"
     * @param dateString The date string to parse.
     * @return The parsed Date object, or null if parsing fails.
     */
    fun parseStringToDateObject(dateString: String?): Date? {
        return try {
            dateString?.let { dateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Formats a name string to capitalize the first letter and lowercase the rest.
     *
     * @param name The name string to format.
     * @return The formatted name string.
     * @example "John Doe"
     */
    fun formatName(name: String?): String {
        return name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""
    }

    /**
     * Formats text to capitalize the first letter and lowercase the rest in  a word
     * Each word's first letter is capitalized
     * When a word is in all caps, and the rest are in lowercase, the formatting is preserved
     * @param text The text to format.
     * @return The formatted text.
     * @example "John Doe"
     */
    fun formatText(text: String?): String {
        return text?.split(" ")?.joinToString(" ") { it.lowercase().replaceFirstChar { it.uppercase() } } ?: ""
    }

    /**
     * Formats an ID number string to add a dash every four digits.
     *
     * @param idNumber The ID number string to format.
     * @return The formatted ID number string.
     * @example "1234-5678"
     */
    fun formatIdNumber(idNumber: String?): String {
        return idNumber?.chunked(4)?.joinToString("-") ?: ""
    }

    /**
     * Formats an email address string to lowercase.
     *
     * @param email The email address string to format.
     * @return The formatted email address string.
     * @example "john.doe@dkut.ac.ke"
     */
    fun formatEmail(email: String?): String {
        return email?.lowercase() ?: ""
    }

    /**
     * Formats a phone number string to include the country code in the format (+Country Code) XXX-XXX-XXXX.
     *
     * @param phoneNumber The phone number string to format.
     * @param countryCode The country code to include.
     * @return The formatted phone number string.
     * @example "(+254) 123-456-7890"
     */
    fun formatPhoneNumber(phoneNumber: String?, countryCode: String): String {
        return phoneNumber?.let {
            var cleanedNumber = it.replace(Regex("\\D"), "")

            // Remove leading plus sign if present
            if (cleanedNumber.startsWith("+")) {
                cleanedNumber = cleanedNumber.substring(1)
            }


            // Remove non-digits from countryCode eg. from `+254 (Kenya)` to `254`
            var cleanedCountryCode = countryCode.replace(Regex("\\D"), "")
            if (cleanedCountryCode.startsWith("+")) {
                // Remove leading plus sign if present
                cleanedCountryCode = cleanedCountryCode.substring(1)
            }

            // Remove leading zero if present
            if (cleanedNumber.startsWith("0")) {
                cleanedNumber = cleanedNumber.substring(1)
            }

            // Remove country code if present
            if (cleanedNumber.startsWith(cleanedCountryCode)) {
                cleanedNumber = cleanedNumber.substring(cleanedCountryCode.length)
            }

            // Format the number
            val formattedNumber = StringBuilder()
            var index = 0
            while (index < cleanedNumber.length) {
                val remainingLength = cleanedNumber.length - index
                val chunkSize =
                    if (remainingLength == 4) 4 else if (remainingLength > 4) 3 else remainingLength
                formattedNumber.append(cleanedNumber.substring(index, index + chunkSize))
                if (index + chunkSize < cleanedNumber.length) {
                    formattedNumber.append("-")
                }
                index += chunkSize
            }
            "(+$cleanedCountryCode) $formattedNumber"
        } ?: ""
    }

    /**
     * Strips formatting from a phone number string for database storage.
     * Useful for passing phone numbers to APIs that require unformatted phone numbers.
     *
     * @param phoneNumber The formatted phone number string.
     * @return The unformatted phone number string.
     * @example "2541234567890"
     */
    fun stripPhoneNumberFormatting(phoneNumber: String?): String {
        return phoneNumber?.replace(Regex("\\D"), "") ?: ""
    }
}