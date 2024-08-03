// app/src/main/java/com/muriithi/dekutcallforhelp/components/Formatter.kt
package com.muriithi.dekutcallforhelp.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formatter class to handle the formatting of various components for UI display.
 */
class Formatter {

    private val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

    /**
     * Formats a Date object to a string in the format dd-MMM-yyyy.
     *
     * @param date The Date object to format.
     * @return The formatted date string.
     */
    fun formatDate(date: Date?): String {
        return date?.let { dateFormat.format(it) } ?: ""
    }

    /**
     * Parses a date string in the format dd-MMM-yyyy to a Date object.
     *
     * @param dateString The date string to parse.
     * @return The parsed Date object, or null if parsing fails.
     */
    fun parseDate(dateString: String?): Date? {
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
     */
    fun formatName(name: String?): String {
        return name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""
    }

    /**
     * Formats an ID number string to add a dash every four digits.
     *
     * @param idNumber The ID number string to format.
     * @return The formatted ID number string.
     */
    fun formatIdNumber(idNumber: String?): String {
        return idNumber?.chunked(4)?.joinToString("-") ?: ""
    }

    /**
     * Formats an email address string to lowercase.
     *
     * @param email The email address string to format.
     * @return The formatted email address string.
     */
    fun formatEmail(email: String?): String {
        return email?.lowercase() ?: ""
    }

    /**
     * Formats a phone number string to include the country code.
     *
     * @param phoneNumber The phone number string to format.
     * @param countryCode The country code to include.
     * @return The formatted phone number string.
     */
    fun formatPhoneNumber(phoneNumber: String?, countryCode: String): String {
        return phoneNumber?.let {
            if (it.startsWith("+")) it else "($countryCode)${it.chunked(3).joinToString("-")}"
        } ?: ""
    }

    /**
     * Strips formatting from a phone number string for database storage.
     *
     * @param phoneNumber The formatted phone number string.
     * @return The unformatted phone number string.
     */
    fun stripPhoneNumberFormatting(phoneNumber: String?): String {
        return phoneNumber?.replace(Regex("[^\\d]"), "") ?: ""
    }
}