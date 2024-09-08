package com.muriithi.dekutcallforhelp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.messaging.FirebaseMessaging
import com.muriithi.dekutcallforhelp.beans.Notification
import com.muriithi.dekutcallforhelp.beans.NotificationStatus
import com.muriithi.dekutcallforhelp.beans.RequestResponse
import com.muriithi.dekutcallforhelp.beans.RequestStatus
import com.muriithi.dekutcallforhelp.beans.NotificationResponseStatus
import com.muriithi.dekutcallforhelp.components.Formatter
import com.muriithi.dekutcallforhelp.data.FirebaseService
import java.util.Date

class AdminHomeFragment : Fragment() {

    private val CHANNEL_ID = "help_request_channel"

    private lateinit var firebaseService: FirebaseService
    private lateinit var dashboard: ViewGroup

    private val formatter = Formatter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)
        dashboard = view.findViewById(R.id.dashboard)
        firebaseService = FirebaseService()

        // Create notification channel
        createNotificationChannel()

        // Subscribe to FCM topic for real-time notifications
        subscribeToFCMTopic()

        // Check Notifications
        checkforNewHelpRequestNotifications()

        // Populate the dashboard
        populateDashboard()

        return view
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Subscribe to the FCM topic for real-time notifications
    private fun subscribeToFCMTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("help_requests")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Successfully subscribed to topic
                    Log.d("AdminHomeFragment", "Subscribed to FCM topic: help_requests")
                } else {
                    // Failed to subscribe to topic
                    Log.e("AdminHomeFragment", "Failed to subscribe to FCM topic: help_requests")
                }
            }
    }

    fun sendNotification(notification: Notification) {
        if (!isAdded) return // Check if the fragment is added to the activity
        val uniqueNotificationId = notification.notificationId.hashCode()

        // On notification tap, open the AdminHomeFragment
        val intent = Intent(requireContext(), ClientHomeFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("fragmentToLoad", "ClientHomeFragment")
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        // Implement the Review notification action
        val reviewIntent = Intent(requireContext(), ReviewReceiver::class.java).apply {
            action = "REVIEW"
            putExtra("notificationId", notification.notificationId)
            putExtra("requestId", notification.requestId)
        }
        val reviewPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            reviewIntent,
            PendingIntent.FLAG_MUTABLE
        )

        // Implement the Reject notification action
        val rejectIntent = Intent(requireContext(), RejectReceiver::class.java).apply {
            action = "REJECT"
            putExtra("notificationId", notification.notificationId)
            putExtra("requestId", notification.requestId)
        }
        val rejectPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            rejectIntent,
            PendingIntent.FLAG_MUTABLE
        )

        // Build the notification
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_outline_uread_chat_bubble)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setAutoCancel(true)
            .addAction(R.drawable.ic_received_call, "Review", reviewPendingIntent)
            .addAction(R.drawable.ic_missed_call, "Reject", rejectPendingIntent)


        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            notify(uniqueNotificationId, builder.build())
        }
    }

    inner class ReviewReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) {
                Log.e("ReviewReceiver", "Context or Intent is null")
                return
            }

            val notificationId = intent.getStringExtra("notificationId")
            val requestId = intent.getStringExtra("requestId")

            if (notificationId.isNullOrEmpty() || requestId.isNullOrEmpty()) {
                Log.e("ReviewReceiver", "Missing or invalid notificationId or requestId")
                Toast.makeText(context, "Failed to process review: Missing data", Toast.LENGTH_SHORT).show()
                return
            }

            firebaseService.getNotificationById(notificationId) { notification ->
                if (notification != null) {
                    notification.status = NotificationStatus.READ
                    notification.responseStatus = NotificationResponseStatus.RESPONDED
                    firebaseService.updateNotification(notification) { success ->
                        if (success) {
                            Log.d("ReviewReceiver", "Notification status updated successfully")
                        } else {
                            Log.e("ReviewReceiver", "Failed to update notification status")
                        }
                    }

                    firebaseService.getHelpRequestById(requestId) { helpRequest ->
                        if (helpRequest != null) {
                            helpRequest.requestStatus = RequestStatus.REVIEWING
                            helpRequest.requestResponse = RequestResponse.ACCEPTED
                            helpRequest.responseTime = formatter.formatDateToString(Date())
                            firebaseService.updateHelpRequest(helpRequest) { success ->
                                if (success) {
                                    Log.d("ReviewReceiver", "Help request status updated successfully")
                                    NotificationManagerCompat.from(context).cancel(notificationId.hashCode())
                                } else {
                                    Log.e("ReviewReceiver", "Failed to update help request status")
                                }
                            }
                        } else {
                            Log.e("ReviewReceiver", "Help request not found for ID: $requestId")
                        }
                    }
                } else {
                    Log.e("ReviewReceiver", "Notification not found for ID: $notificationId")
                }
            }
        }
    }


    inner class RejectReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) {
                Log.e("RejectReceiver", "Context or Intent is null")
                return
            }

            val notificationId = intent.getStringExtra("notificationId")
            val requestId = intent.getStringExtra("requestId")

            if (notificationId.isNullOrEmpty() || requestId.isNullOrEmpty()) {
                Log.e("RejectReceiver", "Missing or invalid notificationId or requestId")
                Toast.makeText(context, "Failed to process rejection: Missing data", Toast.LENGTH_SHORT).show()
                return
            }

            firebaseService.getNotificationById(notificationId) { notification ->
                if (notification != null) {
                    notification.status = NotificationStatus.READ
                    notification.responseStatus = NotificationResponseStatus.RESPONDED
                    firebaseService.updateNotification(notification) { success ->
                        if (success) {
                            Log.d("RejectReceiver", "Notification status updated successfully")
                        } else {
                            Log.e("RejectReceiver", "Failed to update notification status")
                        }
                    }

                    firebaseService.getHelpRequestById(requestId) { helpRequest ->
                        if (helpRequest != null) {
                            helpRequest.requestStatus = RequestStatus.CANCELLED
                            helpRequest.requestResponse = RequestResponse.REJECTED
                            helpRequest.responseTime = formatter.formatDateToString(Date())
                            val resolutionTime = calculateResolutionTime(helpRequest.responseTime)
                            helpRequest.resolutionTime = formatter.formatDateToString(resolutionTime)
                            firebaseService.updateHelpRequest(helpRequest) { success ->
                                if (success) {
                                    Log.d("RejectReceiver", "Help request status updated successfully")
                                    NotificationManagerCompat.from(context).cancel(notificationId.hashCode())
                                } else {
                                    Log.e("RejectReceiver", "Failed to update help request status")
                                }
                            }
                        } else {
                            Log.e("RejectReceiver", "Help request not found for ID: $requestId")
                        }
                    }
                } else {
                    Log.e("RejectReceiver", "Notification not found for ID: $notificationId")
                }
            }
        }
    }


    private fun calculateResolutionTime(responseTime: String): Date {
        val responseDate = formatter.parseStringToDateObject(responseTime)
        val resolutionTime = Date((responseDate?.time ?: 0) + 2 * 60 * 60 * 1000) // Add 2 hours
        return resolutionTime
    }

    private fun checkforNewHelpRequestNotifications() {
        // every 0.5 seconds, check for new notifications
        displayNotification()
        val timer = object : Thread() {
            override fun run() {
                while (!isInterrupted) {
                    try {
                        sleep(600000) // 10 minutes
                        activity?.runOnUiThread {
                            displayNotification()
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        timer.start()
    }

    private fun displayNotification(){
        // get notifications from the database, then call sendNotification(notification) for each notification
        firebaseService.getAllNotifications { notifications ->
            if (notifications != null) {
                for (notification in notifications) {
                    if (notification.status == NotificationStatus.UNREAD) {
                        sendNotification(notification)
                    }
                }
            }
        }
    }

    private fun populateDashboard() {
        // Populate the dashboard with dummy data
        val emergencyHandledCard =
            dashboard.findViewById<MaterialCardView>(R.id.card_emergency_handled)
        val requestsRejectedCard =
            dashboard.findViewById<MaterialCardView>(R.id.card_requests_rejected)
        val topOfficesCard = dashboard.findViewById<MaterialCardView>(R.id.card_top_offices)
        val topOfficesRatingCard =
            dashboard.findViewById<MaterialCardView>(R.id.card_top_offices_rating)
        val recentRequestsList = dashboard.findViewById<ViewGroup>(R.id.list_recent_requests)

        // Set dummy data
        emergencyHandledCard.findViewById<MaterialTextView>(R.id.emergency_handled_metric).text =
            "10"
        requestsRejectedCard.findViewById<MaterialTextView>(R.id.emergency_rejected_metric).text =
            "2"
        topOfficesCard.findViewById<MaterialTextView>(R.id.top_office_metric).text =
            "Medical Office"
        topOfficesRatingCard.findViewById<MaterialTextView>(R.id.office_rating_metric).text = "4.5"
        topOfficesRatingCard.findViewById<MaterialTextView>(R.id.top_rated_office).text =
            "Security Office"

        // Populate recent requests list with dummy data
        for (i in 1..5) {
            val requestView = LayoutInflater.from(context)
                .inflate(R.layout.item_recent_request, recentRequestsList, false)
            requestView.findViewById<MaterialTextView>(R.id.client).text = "User $i"
            requestView.findViewById<MaterialTextView>(R.id.date_time).text = "2023-10-01 12:00"
            requestView.findViewById<MaterialTextView>(R.id.response).text = "Accepted"
            requestView.findViewById<MaterialTextView>(R.id.status).text = "Resolved"

            val responseIcon = requestView.findViewById<ImageView>(R.id.response_icon)
            val statusIcon = requestView.findViewById<ImageView>(R.id.status_icon)

            if (i % 2 == 0) {
                responseIcon.setImageResource(R.drawable.ic_received_call)
                responseIcon.setColorFilter(Color.parseColor("#008000")) // Green
                statusIcon.setImageResource(R.drawable.ic_filled_checkmark)
                statusIcon.setColorFilter(Color.parseColor("#008000")) // Green
            } else {
                responseIcon.setImageResource(R.drawable.ic_missed_call)
                responseIcon.setColorFilter(Color.parseColor("#FF0000")) // Red
                statusIcon.setImageResource(R.drawable.ic_filled_close)
                statusIcon.setColorFilter(Color.parseColor("#FF0000")) // Red
            }

            recentRequestsList.addView(requestView)
        }
    }
}