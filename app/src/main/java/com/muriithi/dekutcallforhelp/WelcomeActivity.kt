// app/src/main/java/com/muriithi/dekutcallforhelp/WelcomeActivity.kt
package com.muriithi.dekutcallforhelp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muriithi.dekutcallforhelp.beans.User
import com.muriithi.dekutcallforhelp.components.Formatter
import com.muriithi.dekutcallforhelp.components.ImageUploader
import com.muriithi.dekutcallforhelp.components.PermissionHandler
import com.muriithi.dekutcallforhelp.data.FirebaseService

class WelcomeActivity : AppCompatActivity() {
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var firebaseService: FirebaseService
    private val formatter = Formatter()
    private lateinit var auth: FirebaseAuth
    private lateinit var imageUploader: ImageUploader
    private lateinit var permissionHandler: PermissionHandler
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            // Handle window insets
            insets
        }

        firebaseService = FirebaseService()
        progressIndicator = findViewById(R.id.progress_indicator)
        auth = FirebaseAuth.getInstance()
        imageUploader = ImageUploader()
        permissionHandler = PermissionHandler(this)

        // Create default admin user if it does not exist
        createDefaultSuperuserAccount()

        // Check if the user is already signed in
        checkIfUserIsSignedIn()

        findViewById<View>(R.id.button_create_account).setOnClickListener {
            showProgressAndNavigate(CreateAccountActivity::class.java)
        }

        findViewById<View>(R.id.button_sign_in).setOnClickListener {
            showProgressAndNavigate(SignInActivity::class.java)
        }
    }

    private fun showProgressAndNavigate(destination: Class<*>) {
        progressIndicator.visibility = View.VISIBLE
        Log.d("WelcomeActivity", "Switched to ${destination.simpleName}")
        startActivity(Intent(this, destination))
        progressIndicator.visibility = View.GONE
    }

    private fun createDefaultSuperuserAccount() {
        val email = "muriithi@dkut.ac.ke"
        val password = "123456" // Use a secure password in production
        val dateOfBirth = formatter.parseStringToDateObject("01-Jan-2000 03:00")
        Log.w("CreateAccountActivity", "Date of birth: $dateOfBirth")

        firebaseService.getUserByEmail(email) { user ->
            if (user == null) {
                progressIndicator.visibility = View.VISIBLE
                var snackBar = Snackbar.make(
                    findViewById(R.id.main),
                    "Creating admin account...",
                    Snackbar.LENGTH_SHORT
                ).setAction("Action", null)
                snackBar.show()
                // Retrieve the profile photo from R.drawable.bg_default_profile_photo
                val profilePhotoUri =
                    Uri.parse("android.resource://${packageName}/${R.drawable.bg_default_profile_photo}")

                // Upload the profile photo to Firebase Storage
                imageUploader.uploadImage(profilePhotoUri) { downloadUrl ->
                    if (downloadUrl != null) {
                        val profilePhoto = downloadUrl
                        firebaseService.createAccount(email, password) { success ->
                            if (success) {
                                progressIndicator.visibility = View.VISIBLE
                                val authUser = auth.currentUser
                                val userId = authUser?.uid

                                if (userId != null) {
                                    val newUser = User().apply {
                                        this.userId = userId
                                        this.firstName = "Dennis"
                                        this.lastName = "Muriithi"
                                        this.course = "Information Technology"
                                        this.school = "Computer Science and Information Technology"
                                        this.registrationNumber = "C001-01-0001/2020"
                                        this.idNumber = 22114433
                                        this.dateOfBirth = formatter.formatDateToString(dateOfBirth)
                                        this.emailAddress = email
                                        this.phoneNumber =
                                            formatter.stripPhoneNumberFormatting("+254712345678")
                                        this.countryCode = "254"
                                        this.superuser = true
                                        this.profilePhoto = profilePhoto
                                    }
                                    firebaseService.writeData(
                                        "users/$userId",
                                        newUser
                                    ) { writeSuccess ->
                                        if (writeSuccess) {
                                            Log.d(
                                                "CreateAccountActivity",
                                                "Superuser account created successfully for userId: $userId"
                                            )
                                            progressIndicator.visibility = View.GONE
                                            snackBar = Snackbar.make(
                                                findViewById(R.id.main),
                                                "Admin account created successfully",
                                                Snackbar.LENGTH_SHORT
                                            ).setAction("Action", null)
                                            snackBar.show()
                                        } else {
                                            Log.e(
                                                "CreateAccountActivity",
                                                "Failed to create superuser account for userId: $userId"
                                            )
                                            progressIndicator.visibility = View.GONE
                                            snackBar = Snackbar.make(
                                                findViewById(R.id.main),
                                                "Failed to create admin account",
                                                Snackbar.LENGTH_SHORT
                                            ).setAction("Action", null)
                                            snackBar.show()
                                        }
                                    }
                                }
                            } else {
                                Log.e(
                                    "CreateAccountActivity",
                                    "Failed to create default superuser account"
                                )
                                progressIndicator.visibility = View.GONE
                                snackBar = Snackbar.make(
                                    findViewById(R.id.main),
                                    "Failed to create admin account",
                                    Snackbar.LENGTH_SHORT
                                ).setAction("Action", null)
                                snackBar.show()
                            }
                        }
                    }
                }
            } else {
                Log.e("CreateAccountActivity", "Failed to upload profile photo")
                progressIndicator.visibility = View.GONE
                val snackBar = Snackbar.make(
                    findViewById(R.id.main),
                    "Failed to upload profile photo",
                    Snackbar.LENGTH_SHORT
                ).setAction("Action", null)
                snackBar.show()
            }
        }
    }

    private fun checkIfUserIsSignedIn() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val ref = database.getReference("users").child(currentUser.uid)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    val displayName = user?.let { "${it.firstName} ${it.lastName}" }
                    createDisplayName(displayName)
                    val snackBar = Snackbar.make(
                        findViewById(R.id.main),
                        "Welcome back, $displayName!",
                        Snackbar.LENGTH_SHORT
                    ).setAction("Action", null)
                    snackBar.show()
                    startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("WelcomeActivity", "Failed to read value.", error.toException())
                }
            })
        }
    }

    private fun createDisplayName(displayName: String?) {
        val user = FirebaseAuth.getInstance().currentUser

        // Check if the user object is not null
        user?.let {
            // check if the display name is not null
            if (user.displayName != null) {

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("WelcomeActivity", "User profile updated.")
                        }
                    }
            }
        }
    }
}