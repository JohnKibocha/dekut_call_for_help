package com.muriithi.dekutcallforhelp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.muriithi.dekutcallforhelp.ClientHomeFragment
import com.muriithi.dekutcallforhelp.R
import com.muriithi.dekutcallforhelp.receivers.MarkAsReadReceiver

const val channelId = "help_request_channel"
const val channelName = "com.muriithi.dekutcallforhelp"

class PushNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send the token to the server

    }

    fun generateNotification(
        notificationTitle: String, notificationMessage: String,
        notificationMarkAsReadButton: Boolean = false
    ) {
        val intent = Intent(this, ClientHomeFragment::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val markAsReadIntent = Intent(this, MarkAsReadReceiver::class.java).apply {
            action = "MARK_AS_READ"
            putExtra("notification_id", 0) // Use a unique ID for each notification
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val markAsReadPendingIntent = PendingIntent.getBroadcast(
            this, 0, markAsReadIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        var builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.bg_filled_logo)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setContentTitle(notificationTitle)
                .setOnlyAlertOnce(true)
                .setCustomBigContentView(getRemoteView(notificationTitle, notificationMessage))
                .setContentText(notificationMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        builder = builder.setContent(
            getRemoteView(
                notificationTitle,
                notificationMessage,
                notificationMarkAsReadButton
            )
        )
        builder.addAction(R.drawable.ic_filled_read, "Mark as Read", markAsReadPendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
        notificationManager.notify(0, builder.build())
    }

    fun getRemoteView(
        notificationTitle: String, notificationMessage: String,
        notificationMarkAsReadButton: Boolean = true
    ): RemoteViews {
        val remoteView = RemoteViews("com.muriithi.dekutcallforhelp", R.layout.view_notification)

        remoteView.setTextViewText(R.id.text_view_notification_title, notificationTitle)
        remoteView.setTextViewText(R.id.text_view_notification_message, notificationMessage)
        remoteView.setImageViewResource(
            R.id.image_view_notification_logo,
            R.drawable.bg_filled_logo
        )

        if (notificationMarkAsReadButton) {
            remoteView.setViewVisibility(R.id.button_mark_as_read, View.VISIBLE)

            val markAsReadIntent = Intent(this, MarkAsReadReceiver::class.java).apply {
                action = "MARK_AS_READ"
                putExtra("notification_id", 0) // Use a unique ID for each notification
            }

            val markAsReadPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                markAsReadIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            remoteView.setOnClickPendingIntent(R.id.button_mark_as_read, markAsReadPendingIntent)
        } else {
            remoteView.setViewVisibility(R.id.button_mark_as_read, View.GONE)
        }

        return remoteView
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            generateNotification(
                remoteMessage.notification!!.title!!,
                remoteMessage.notification!!.body!!
            )
        }
    }
}