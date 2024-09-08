package com.muriithi.dekutcallforhelp.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MarkAsReadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "MARK_AS_READ") {
            Log.e("MarkAsReadReceiver", "Mark as read button clicked")

            // Remove the notification from the tray
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = intent.getIntExtra("notification_id", -1)
            if (notificationId != -1) {
                notificationManager.cancel(notificationId)
            }
        }
    }
}