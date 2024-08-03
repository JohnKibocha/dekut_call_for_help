// app/src/main/java/com/muriithi/dekutcallforhelp/components/Validator.kt
package com.muriithi.dekutcallforhelp.components

class Validator {

    /**
     * Validates an email string based on specific criteria.
     *
     * @param email The email string to validate.
     * @return `true` if the email contains an `@` symbol and ends with `dkut.ac.ke`, `false` otherwise.
     * @throws IllegalArgumentException if the email is null or empty.
     */
    fun validateEmail(email: String?): Boolean {
        if (email.isNullOrEmpty()) {
            throw IllegalArgumentException("Email cannot be null or empty")
        }
        if (!email.contains("@")) {
            return false
        }
        if (!email.endsWith("dkut.ac.ke")) {
            return false
        }
        return true
    }

    /**
     * Validates a registration number string based on specific criteria.
     *
     * @param registrationNumber The registration number string to validate.
     * @return `true` if the registration number matches the pattern, `false` otherwise.
     * @throws IllegalArgumentException if the registration number is null or empty.
     */
    fun validateRegistrationNumber(registrationNumber: String?): Boolean {
        if (registrationNumber.isNullOrEmpty()) {
            throw IllegalArgumentException("Registration number cannot be null or empty")
        }
        val regex = Regex("[A-Z]{1}[0-9]{3}-01-[0-9]{4}/[0-9]{4}")
        return regex.matches(registrationNumber)
    }

    /**
     * Validates a phone number string based on specific criteria.
     *
     * @param phoneNumber The phone number string to validate.
     * @return `true` if the phone number is between 8 and 17 digits long and contains valid characters, `false` otherwise.
     * @throws IllegalArgumentException if the phone number is null or empty.
     */
    fun validatePhoneNumber(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrEmpty()) {
            throw IllegalArgumentException("Phone number cannot be null or empty")
        }
        val regex = Regex("^[+\\d\\s-]{8,17}$")
        return regex.matches(phoneNumber)
    }

    /**
     * Validates an ID number string based on specific criteria.
     *
     * @param idNumber The ID number string to validate.
     * @return `true` if the ID number is between 7 and 15 digits long, `false` otherwise.
     * @throws IllegalArgumentException if the ID number is null or empty.
     */
    fun validateIdNumber(idNumber: String?): Boolean {
        if (idNumber.isNullOrEmpty()) {
            throw IllegalArgumentException("ID number cannot be null or empty")
        }
        return idNumber.length in 7..15
    }
}