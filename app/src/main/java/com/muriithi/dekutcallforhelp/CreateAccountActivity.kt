// app/src/main/java/com/muriithi/dekutcallforhelp/CreateAccountActivity.kt
package com.muriithi.dekutcallforhelp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.muriithi.dekutcallforhelp.beans.User
import com.muriithi.dekutcallforhelp.components.Formatter
import com.muriithi.dekutcallforhelp.components.Validator
import com.muriithi.dekutcallforhelp.data.FirebaseService

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var firebaseService: FirebaseService
    private lateinit var progressIndicator: LinearProgressIndicator
    private val validator = Validator()
    private val formatter = Formatter()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        firebaseService = FirebaseService()
        progressIndicator = findViewById(R.id.progress_indicator)
        auth = FirebaseAuth.getInstance()

        // Create default superuser account
        createDefaultSuperuserAccount()
    }

    private fun createDefaultSuperuserAccount() {
        val email = "muriithi.admin@dkut.ac.ke"
        val password = "defaultPassword" // Use a secure password in a real application
        val dateOfBirth = formatter.parseDate("01-Jan-2000")

        firebaseService.createAccount(email, password) { success ->
            if (success) {
                val user = auth.currentUser
                val userId = user?.uid

                if (userId != null) {
                    val newUser = User().apply {
                        this.userId = userId
                        this.firstName = "Dennis"
                        this.lastName = "Muriithi"
                        this.course = "Not Applicable"
                        this.school = "Not Applicable"
                        this.registrationNumber = "00000000"
                        this.idNumber = 0
                        this.dateOfBirth = dateOfBirth
                        this.emailAddress = email
                        this.phoneNumber = "+254113292833"
                        this.isSuperuser = true
                    }

                    firebaseService.writeData("users/$userId", newUser) { writeSuccess ->
                        if (writeSuccess) {
                            Log.d("CreateAccountActivity", "Superuser account created successfully for userId: $userId")
                        } else {
                            Log.e("CreateAccountActivity", "Failed to create superuser account for userId: $userId")
                        }
                    }
                }
            } else {
                Log.e("CreateAccountActivity", "Superuser account creation failed for email: $email")
            }
        }
    }

    fun createAccount(view: View) {
        progressIndicator.visibility = View.VISIBLE

        val email = findViewById<TextInputEditText>(R.id.edit_text_email_address).text.toString()
        val password = findViewById<TextInputEditText>(R.id.edit_text_password).text.toString()
        val registrationNumber = findViewById<TextInputEditText>(R.id.edit_text_registration_number).text.toString()
        val idNumber = findViewById<TextInputEditText>(R.id.edit_text_id_number).text.toString()
        val phoneNumber = findViewById<TextInputEditText>(R.id.edit_text_phone_number).text.toString()
        val dateOfBirthString = findViewById<TextInputEditText>(R.id.edit_text_date_of_birth).text.toString()
        val firstName = findViewById<TextInputEditText>(R.id.edit_text_first_name).text.toString()
        val lastName = findViewById<TextInputEditText>(R.id.edit_text_last_name).text.toString()
        val course = findViewById<TextInputEditText>(R.id.edit_text_course).text.toString()
        val school = findViewById<TextInputEditText>(R.id.edit_text_school).text.toString()

        if (!validator.validateEmail(email)) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
            progressIndicator.visibility = View.GONE
            return
        }

        if (!validator.validateRegistrationNumber(registrationNumber)) {
            Toast.makeText(this, "Invalid registration number", Toast.LENGTH_SHORT).show()
            progressIndicator.visibility = View.GONE
            return
        }

        if (!validator.validateIdNumber(idNumber)) {
            Toast.makeText(this, "Invalid ID number", Toast.LENGTH_SHORT).show()
            progressIndicator.visibility = View.GONE
            return
        }

        if (!validator.validatePhoneNumber(phoneNumber)) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
            progressIndicator.visibility = View.GONE
            return
        }

        val dateOfBirth = formatter.parseDate(dateOfBirthString)
        if (dateOfBirth == null) {
            Toast.makeText(this, "Invalid date of birth", Toast.LENGTH_SHORT).show()
            progressIndicator.visibility = View.GONE
            return
        }

        val formattedEmail = formatter.formatEmail(email)
        val formattedPhoneNumber = formatter.formatPhoneNumber(phoneNumber, "+254") // Assuming country code +254
        val strippedPhoneNumber = formatter.stripPhoneNumberFormatting(formattedPhoneNumber)
        val formattedFirstName = formatter.formatName(firstName)
        val formattedLastName = formatter.formatName(lastName)
        val formattedCourse = formatter.formatName(course)
        val formattedSchool = formatter.formatName(school)
        val formattedIdNumber = formatter.formatIdNumber(idNumber)

        firebaseService.createAccount(formattedEmail, password) { success ->
            if (success) {
                val user = auth.currentUser
                val userId = user?.uid

                if (userId != null) {
                    val newUser = User().apply {
                        this.userId = userId
                        this.firstName = formattedFirstName
                        this.lastName = formattedLastName
                        this.course = formattedCourse
                        this.school = formattedSchool
                        this.registrationNumber = registrationNumber
                        this.idNumber = idNumber.toIntOrNull()
                        this.dateOfBirth = dateOfBirth
                        this.emailAddress = formattedEmail
                        this.phoneNumber = strippedPhoneNumber
                        this.isSuperuser = false
                    }

                    firebaseService.writeData("users/$userId", newUser) { writeSuccess ->
                        if (writeSuccess) {
                            Log.d("CreateAccountActivity", "User data saved successfully for userId: $userId")
                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Log.e("CreateAccountActivity", "Failed to save user data for userId: $userId")
                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, WelcomeActivity::class.java))
                            finish()
                        }
                        progressIndicator.visibility = View.GONE
                    }
                }
            } else {
                Log.e("CreateAccountActivity", "Account creation failed for email: $email")
                Toast.makeText(this, "Account creation failed", Toast.LENGTH_SHORT).show()
                progressIndicator.visibility = View.GONE
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }
}