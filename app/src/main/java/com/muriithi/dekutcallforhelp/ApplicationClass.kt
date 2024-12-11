package com.muriithi.dekutcallforhelp

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.muriithi.dekutcallforhelp.databases.FirebaseService
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Get the onesignal app id from `gradle.properties` file
val ONESIGNAL_APP_ID = "b00f6c83-3f74-49db-a0f1-d23525408939"

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()

        // Verbose Logging set to help debug issues, remove before releasing your app.
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // OneSignal Initialization
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)

        // Set the UserId as the OneSignal PlayerId and update database
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseService = FirebaseService()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                OneSignal.login(currentUser.uid)
                Log.d("OneSignal - ApplicationClass", "User logged in with OneSignal successfully")

                // Get the player id of the user from OneSignal
                val oneSignalPlayerId = OneSignal.User.onesignalId
                Log.d("OneSignal - ApplicationClass", "OneSignal Player ID: $oneSignalPlayerId")

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
        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
    }
}
