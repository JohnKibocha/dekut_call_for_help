// app/src/main/java/com/muriithi/dekutcallforhelp/CreateAccountActivity.kt
package com.muriithi.dekutcallforhelp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.muriithi.dekutcallforhelp.beans.User
import com.muriithi.dekutcallforhelp.components.Component
import com.muriithi.dekutcallforhelp.components.Formatter
import com.muriithi.dekutcallforhelp.components.ImageRetriever
import com.muriithi.dekutcallforhelp.components.ImageUploader
import com.muriithi.dekutcallforhelp.components.Validator
import com.muriithi.dekutcallforhelp.data.FirebaseService
import java.util.Date

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var firebaseService: FirebaseService
    private lateinit var progressIndicator: LinearProgressIndicator
    private val validator = Validator()
    private val formatter = Formatter()
    private lateinit var auth: FirebaseAuth
    private lateinit var countryCodeDropdown: AutoCompleteTextView
    private lateinit var imageRetriever: ImageRetriever
    private lateinit var imageUploader: ImageUploader
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var createAccountButton: Button
    private var profileImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        firebaseService = FirebaseService()
        progressIndicator = findViewById(R.id.progress_indicator)
        auth = FirebaseAuth.getInstance()
        imageUploader = ImageUploader()
        profileImageView = findViewById(R.id.image_view_profile_photo)
        createAccountButton = findViewById(R.id.button_create_new_account)

        countryCodeDropdown =
            findViewById<TextInputLayout>(R.id.dropdown_field_country_code).editText as AutoCompleteTextView
        val countryCodes = resources.getStringArray(R.array.country_codes)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countryCodes)
        countryCodeDropdown.setAdapter(adapter)

        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").build()
        datePicker.addOnPositiveButtonClickListener {
            val date = formatter.formatDateToString(Date(it))
            (findViewById<TextInputLayout>(R.id.text_field_date_of_birth).editText as TextInputEditText).setText(
                date
            )
        }

        val dateOfBirthLayout = findViewById<TextInputLayout>(R.id.text_field_date_of_birth)
        dateOfBirthLayout.setEndIconOnClickListener {
            if (!datePicker.isAdded) {
                datePicker.show(supportFragmentManager, "date_picker")
            }
        }

        val dateOfBirthEditText = dateOfBirthLayout.editText as TextInputEditText
        dateOfBirthEditText.setOnClickListener {
            if (!datePicker.isAdded) {
                datePicker.show(supportFragmentManager, "date_picker")
            }
        }

        val defaultCountryCode = "+254 (KE)"
        val defaultPosition = countryCodes.indexOf(defaultCountryCode)
        if (defaultPosition != -1) {
            countryCodeDropdown.setText(defaultCountryCode, false)
        }

        createAccountButton.setOnClickListener {
            createAccount()
        }

        val topAppBar = findViewById<MaterialToolbar>(R.id.top_app_bar)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        imageRetriever = ImageRetriever(Component.ActivityComponent(this))

        profileImageView.setOnClickListener {
            imageRetriever.retrieveImage { uri ->
                uri?.let {
                    uploadImage(it)
                }
            }
        }
    }

    private fun uploadImage(uri: Uri) {
        createAccountButton.isEnabled = false
        createAccountButton.alpha = 0.5f
        progressIndicator.visibility = View.VISIBLE
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            "Uploading profile photo...",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Action", null)
        snackBar.show()

        imageUploader.uploadImage(uri) { downloadUrl ->
            if (downloadUrl != null) {
                profileImageUrl = downloadUrl
                // Set the profile image to the image view
                profileImageView.setImageURI(uri)
                snackBar.setText("Profile photo uploaded successfully")
                    .setDuration(Snackbar.LENGTH_SHORT)
                    .setAction("Action", null)
                    .show()
            } else {
                snackBar.setText("Failed to upload profile photo")
                    .setDuration(Snackbar.LENGTH_SHORT)
                    .setAction("Action", null)
                    .show()
            }
            createAccountButton.isEnabled = true
            createAccountButton.alpha = 1.0f
            progressIndicator.visibility = View.GONE
        }
    }


    @SuppressLint("CutPasteId")
    private fun createAccount() {
        progressIndicator.visibility = View.VISIBLE
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            "Creating account...",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Action", null)
        snackBar.show()

        val email =
            (findViewById<TextInputLayout>(R.id.text_field_email).editText as TextInputEditText).text.toString()
        val confirmPassword =
            (findViewById<TextInputLayout>(R.id.text_field_confirm_password).editText as TextInputEditText).text.toString()
        val password =
            (findViewById<TextInputLayout>(R.id.text_field_password).editText as TextInputEditText).text.toString()
        val registrationNumber =
            (findViewById<TextInputLayout>(R.id.text_field_registration_number).editText as TextInputEditText).text.toString()
        val idNumber =
            (findViewById<TextInputLayout>(R.id.text_field_id_number).editText as TextInputEditText).text.toString()
        val phoneNumber =
            (findViewById<TextInputLayout>(R.id.text_field_phone_number).editText as TextInputEditText).text.toString()
        val dateOfBirthString =
            (findViewById<TextInputLayout>(R.id.text_field_date_of_birth).editText as TextInputEditText).text.toString()
        val firstName =
            (findViewById<TextInputLayout>(R.id.text_field_first_name).editText as TextInputEditText).text.toString()
        val lastName =
            (findViewById<TextInputLayout>(R.id.text_field_last_name).editText as TextInputEditText).text.toString()
        val course =
            (findViewById<TextInputLayout>(R.id.dropdown_field_course).editText as AutoCompleteTextView).text.toString()
        val school =
            (findViewById<TextInputLayout>(R.id.dropdown_field_school).editText as AutoCompleteTextView).text.toString()
        val countryCode = countryCodeDropdown.text.toString()

        var isValid = true

        try {
            validator.validateField(email)
        } catch (e: IllegalArgumentException) {
            findViewById<TextInputLayout>(R.id.text_field_email).error = "Email is required"
            isValid = false
        }

        try {
            validator.validateField(password)
        } catch (e: IllegalArgumentException) {
            findViewById<TextInputLayout>(R.id.text_field_password).error = "Password is required"
            isValid = false
        }

        try {
            validator.validateField(confirmPassword)
        } catch (e: IllegalArgumentException) {
            findViewById<TextInputLayout>(R.id.text_field_confirm_password).error =
                "Confirm Password is required"
            isValid = false
        }

        try {
            validator.validateField(registrationNumber)
        } catch (e: IllegalArgumentException) {
            findViewById<TextInputLayout>(R.id.text_field_registration_number).error =
                "Registration Number is required"
            isValid = false
        }

        try {
            validator.validateField(idNumber)
        } catch (e: IllegalArgumentException) {
            findViewById<TextInputLayout>(R.id.text_field_id_number).error = "ID Number is required"
            isValid = false
        }

        try {
            validator.validateField(phoneNumber)
        } catch (e: IllegalArgumentException) {
            findViewById<TextInputLayout>(R.id.text_field_phone_number).error =
                "Phone Number is required"
            isValid = false
        }

        try {
            validator.validateField(dateOfBirthString)
        } catch (e: IllegalArgumentException) {
            findViewById<TextInputLayout>(R.id.text_field_date_of_birth).error =
                "Date of Birth is required"
            isValid = false
        }

        try {
            validator.validateField(firstName)
        } catch (e: IllegalArgumentException) {
            findViewById<TextInputLayout>(R.id.text_field_first_name).error =
                "First Name is required"
            isValid = false
        }

        try {
            validator.validateField(lastName)
        } catch (e: IllegalArgumentException) {
            findViewById<TextInputLayout>(R.id.text_field_last_name).error = "Last Name is required"
            isValid = false
        }

        try {
            validator.validateField(course)
        } catch (e: IllegalArgumentException) {
            findViewById<TextInputLayout>(R.id.dropdown_field_course).error = "Course is required"
            isValid = false
        }

        try {
            validator.validateField(school)
        } catch (e: IllegalArgumentException) {
            findViewById<TextInputLayout>(R.id.dropdown_field_school).error = "School is required"
            isValid = false
        }

        if (!isValid) {
            progressIndicator.visibility = View.GONE
            return
        }

        if (!validator.validateEmail(email)) {
            findViewById<TextInputLayout>(R.id.text_field_email).error = "Invalid email address"
            progressIndicator.visibility = View.GONE
            return
        }

        if (!validator.validateRegistrationNumber(registrationNumber)) {
            findViewById<TextInputLayout>(R.id.text_field_registration_number).error =
                "Invalid registration number"
            progressIndicator.visibility = View.GONE
            return
        }

        if (!validator.validateIdNumber(idNumber)) {
            findViewById<TextInputLayout>(R.id.text_field_id_number).error = "Invalid ID number"
            progressIndicator.visibility = View.GONE
            return
        }

        if (!validator.validatePhoneNumber(phoneNumber)) {
            findViewById<TextInputLayout>(R.id.text_field_phone_number).error =
                "Invalid phone number"
            progressIndicator.visibility = View.GONE
            return
        }

        val dateOfBirth = formatter.parseStringToDateObject(dateOfBirthString)
        if (dateOfBirth == null) {
            findViewById<TextInputLayout>(R.id.text_field_date_of_birth).error =
                "Invalid date of birth"
            progressIndicator.visibility = View.GONE
            return
        }

        if (password.length < 6) {
            findViewById<TextInputLayout>(R.id.text_field_password).error =
                "Password must be at least 6 characters long"
            progressIndicator.visibility = View.GONE
            return
        }

        if (password != confirmPassword) {
            findViewById<TextInputLayout>(R.id.text_field_confirm_password).error =
                "Passwords do not match"
            progressIndicator.visibility = View.GONE
            return
        }

        val formattedEmail = formatter.formatEmail(email)
        val formattedFirstName = formatter.formatName(firstName)
        val formattedLastName = formatter.formatName(lastName)
        val formattedCourse = formatter.formatText(course)
        val formattedSchool = formatter.formatText(school)

        val formattedPhoneNumber = formatter.stripPhoneNumberFormatting(phoneNumber)
        val formattedCountryCode = formatter.stripPhoneNumberFormatting(countryCode)
        val formattedPhoneNumberWithCountryCode =
            formatter.formatPhoneNumber(formattedPhoneNumber, formattedCountryCode)

        firebaseService.createAccount(formattedEmail, password) { success ->
            if (success) {
                var user = auth.currentUser
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
                        this.dateOfBirth = formatter.formatDateToString(dateOfBirth)
                        this.emailAddress = formattedEmail
                        this.phoneNumber = formatter.stripPhoneNumberFormatting(formattedPhoneNumberWithCountryCode)
                        this.superuser = false
                        this.countryCode = countryCode
                        this.profilePhoto = profileImageUrl
                    }

                    firebaseService.writeData("users/$userId", newUser) { writeSuccess ->
                        if (writeSuccess) {
                            snackBar.setText("Account created successfully")
                                .setDuration(Snackbar.LENGTH_SHORT)
                                .setAction("Action", null)
                                .show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            snackBar.setText("Failed to save user data to database")
                                .setDuration(Snackbar.LENGTH_SHORT)
                                .setAction("Action", null)
                                .show()
                            startActivity(Intent(this, WelcomeActivity::class.java))
                            finish()
                        }
                        progressIndicator.visibility = View.GONE
                    }

                    user = FirebaseAuth.getInstance().currentUser

                    // Check if the user object is not null
                    user?.let {
                        // check if display name is null
                        if (user.displayName == null) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName("$formattedFirstName $formattedLastName")
                                .build()

                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "${user.displayName} profile updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }
                }
            } else {
                snackBar.setText("Failed to create account")
                    .setDuration(Snackbar.LENGTH_SHORT)
                    .setAction("Action", null)
                    .show()
                progressIndicator.visibility = View.GONE
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }
}