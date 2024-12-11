// app/src/main/java/com/muriithi/dekutcallforhelp/SignInActivity.kt
package com.muriithi.dekutcallforhelp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dataconnect.LogLevel
import com.muriithi.dekutcallforhelp.components.Validator
import com.muriithi.dekutcallforhelp.databases.FirebaseService
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var firebaseService: FirebaseService
    private lateinit var validator: Validator

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        validator = Validator()
        progressIndicator = findViewById(R.id.progress_indicator)
        firebaseService = FirebaseService()

        // Add verbose firebase debugging

        findViewById<View>(R.id.button_new_sign_in).setOnClickListener {
            val emailLayout = findViewById<TextInputLayout>(R.id.text_field_email_address)
            val passwordLayout = findViewById<TextInputLayout>(R.id.text_field_signin_password)
            val email = emailLayout.editText?.text.toString()
            val password = passwordLayout.editText?.text.toString()

            if (validateInputs(email, password, emailLayout, passwordLayout)) {
                signInUser(email, password)
            }
        }

        val topAppBar = findViewById<MaterialToolbar>(R.id.sign_in_top_app_bar)
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
            findViewById(android.R.id.content), "Signing in...", Snackbar.LENGTH_INDEFINITE
        ).setAction("Action", null)
        snackBar.show()

        firebaseService.signIn(email, password) { isAuthenticated ->
            progressIndicator.visibility = View.GONE
            if (isAuthenticated) {

                registerUserForOneSignalNotifications()

                snackBar.setText("Signed in successfully").setDuration(Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                snackBar.setText("Sign in failed").setDuration(Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            }
        }
    }

    private fun registerUserForOneSignalNotifications() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            // Check if the user has a onesignal player id
            firebaseService.getAllUsers { users ->
                if (users != null) {
                    for (user in users) {
                        if (user.userId == currentUser.uid) {
                            if (user.oneSignalPlayerId == null) {
                                // Sign up the user for notifications
                                CoroutineScope(Dispatchers.IO).launch {
                                    OneSignal.login(currentUser.uid)
                                    Log.d(
                                        "OneSignal - ApplicationClass",
                                        "User logged in with OneSignal successfully"
                                    )

                                    // Get the player id of the user from OneSignal
                                    val oneSignalPlayerId = OneSignal.User.onesignalId
                                    Log.d(
                                        "OneSignal - ApplicationClass",
                                        "OneSignal Player ID: $oneSignalPlayerId"
                                    )

                                    // Check if the user is admin and tag them as such and if not tag them as a client
                                    firebaseService.getAllUsers { users ->
                                        if (users != null) {
                                            for (user in users) {
                                                if (user.userId == currentUser.uid) {
                                                    if (user.superuser) {
                                                        OneSignal.User.addTag("role", "admin")
                                                    } else {
                                                        OneSignal.User.addTag("role", "client")
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Update the user's OneSignal Player ID in the database
                                    firebaseService.getUserById(currentUser.uid) { user ->
                                        if (user != null) {
                                            user.oneSignalPlayerId = oneSignalPlayerId
                                            firebaseService.updateUser(user) {
                                                Log.d(
                                                    "OneSignal - ApplicationClass",
                                                    "User's OneSignal Player ID: $oneSignalPlayerId was updated successfully"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}