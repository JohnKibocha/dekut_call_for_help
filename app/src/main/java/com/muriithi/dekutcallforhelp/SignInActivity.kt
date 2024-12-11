package com.muriithi.dekutcallforhelp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.muriithi.dekutcallforhelp.components.Formatter
import com.muriithi.dekutcallforhelp.components.Validator
import com.muriithi.dekutcallforhelp.data.FirebaseService

class SignInActivity : AppCompatActivity() {
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var firebaseService: FirebaseService
    private lateinit var formatter: Formatter
    private lateinit var validator: Validator

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        validator = Validator()
        progressIndicator = findViewById(R.id.progress_indicator)
        firebaseService = FirebaseService()

        findViewById<View>(R.id.button_new_sign_in).setOnClickListener {
            val emailLayout = findViewById<TextInputLayout>(R.id.text_field_email_address)
            val passwordLayout = findViewById<TextInputLayout>(R.id.text_field_signin_password)
            val email = emailLayout.editText?.text.toString()
            val password = passwordLayout.editText?.text.toString()

            if (validateInputs(email, password, emailLayout, passwordLayout)) {
                signInUser(email, password)
            }
        }

        val topAppBar = findViewById<MaterialToolbar>(R.id.top_app_bar)
        topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun validateInputs(
        email: String?,
        password: String?,
        emailLayout: TextInputLayout,
        passwordLayout: TextInputLayout
    ): Boolean {
        var isValid = true

        if (email.isNullOrEmpty()) {
            emailLayout.error = "Email is required"
            isValid = false
        } else if (!validator.validateEmail(email)) {
            emailLayout.error = "Invalid email address"
            isValid = false
        } else {
            emailLayout.error = null
        }

        if (password.isNullOrEmpty()) {
            passwordLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters long"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        return isValid
    }

    private fun signInUser(email: String, password: String) {
        progressIndicator.visibility = View.VISIBLE
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            "Signing in...",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Action", null)
        snackBar.show()

        firebaseService.signIn(email, password) { isAuthenticated ->
            progressIndicator.visibility = View.GONE
            if (isAuthenticated) {
                snackBar.setText("Signed in successfully")
                    .setDuration(Snackbar.LENGTH_SHORT)
                    .show()
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                snackBar.setText("Sign in failed")
                    .setDuration(Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }
}