package com.muriithi.dekutcallforhelp.components

import android.util.Log
import com.muriithi.dekutcallforhelp.beans.OneSignalNotification
import com.muriithi.dekutcallforhelp.interfaces.OneSignalApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OneSignalNotificationManager {

    private val oneSignalApi = OneSignalApi.create()
    private val appId = "b00f6c83-3f74-49db-a0f1-d23525408939" // Replace with your OneSignal App ID

    fun sendNotification(receiverUserIds: List<String>, title: String, message: String) {
        val notification = OneSignalNotification(
            app_id = appId,
            include_aliases = mapOf("external_id" to receiverUserIds),
            headings = mapOf("en" to title),
            contents = mapOf("en" to message)
        )

        CoroutineScope(Dispatchers.IO).launch {
            val response = oneSignalApi.sendNotification(notification)
            if (response.isSuccessful) {
                Log.d("OneSignalNotificationManager", "Notification sent successfully")
            } else {
                Log.e("OneSignalNotificationManager", "Failed to send notification")
            }
        }
    }

    fun sendNotification(receiverUserId: String, title: String, message: String) {
        sendNotification(listOf(receiverUserId), title, message)
    }

    // Send notification to all admins
    fun sendNotificationToAdmins(title: String, message: String) {
        val filter = mapOf("field" to "tag", "key" to "role", "relation" to "=", "value" to "admin")

        val notification = OneSignalNotification(
            app_id = appId,
            filters = listOf(filter),
            headings = mapOf("en" to title),
            contents = mapOf("en" to message)
        )

        CoroutineScope(Dispatchers.IO).launch {
            val response = oneSignalApi.sendNotification(notification)
            if (response.isSuccessful) {
                // Handle success
                Log.d(
                    "OneSignalNotificationManager",
                    "Broadcast to Administrators sent successfully"
                )
            } else {
                // Handle error
                Log.e("OneSignalNotificationManager", "Failed to broadcast to Administrators")
            }
        }
    }

    // Send notification to all clients
    fun sendNotificationToClients(title: String, message: String) {
        val filter =
            mapOf("field" to "tag", "key" to "role", "relation" to "=", "value" to "client")

        val notification = OneSignalNotification(
            app_id = appId,
            filters = listOf(filter),
            headings = mapOf("en" to title),
            contents = mapOf("en" to message)
        )

        CoroutineScope(Dispatchers.IO).launch {
            val response = oneSignalApi.sendNotification(notification)
            if (response.isSuccessful) {
                // Handle success
                Log.d("OneSignalNotificationManager", "Broadcast to Clients sent successfully")
            } else {
                // Handle error
                Log.e("OneSignalNotificationManager", "Failed to broadcast to Clients")
            }
        }
    }

    // Send notification to everyone
    fun sendNotificationToEveryone(title: String, message: String) {
        val notification = OneSignalNotification(
            app_id = appId,
            headings = mapOf("en" to title),
            contents = mapOf("en" to message)
        )

        CoroutineScope(Dispatchers.IO).launch {
            val response = oneSignalApi.sendNotification(notification)
            if (response.isSuccessful) {
                // Handle success
                Log.d("OneSignalNotificationManager", "Broadcast to Everyone sent successfully")
            } else {
                // Handle error
                Log.e("OneSignalNotificationManager", "Failed to broadcast to Everyone")
            }
        }
    }
}
