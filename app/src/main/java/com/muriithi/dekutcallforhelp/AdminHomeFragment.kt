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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.muriithi.dekutcallforhelp.beans.Notification
import com.muriithi.dekutcallforhelp.beans.RequestResponse
import com.muriithi.dekutcallforhelp.beans.RequestStatus
import com.muriithi.dekutcallforhelp.data.FirebaseService
import com.muriithi.dekutcallforhelp.handlers.HelpNotificationHandler

class AdminHomeFragment : Fragment() {

    private val CHANNEL_ID = "help_request_channel"
    private val NOTIFICATION_ID = 1

    private lateinit var firebaseService: FirebaseService
    private lateinit var helpNotificationHandler: HelpNotificationHandler
    private lateinit var dashboard: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)
        dashboard = view.findViewById(R.id.dashboard)
        firebaseService = FirebaseService()
        helpNotificationHandler = HelpNotificationHandler(firebaseService)

        // Create notification channel
        createNotificationChannel()

        // Send notifications processed by HelpNotificationHandler
        helpNotificationHandler.getProcessedNotifications { notifications ->
            notifications.forEach { notification ->
                sendNotification(notification)
            }
        }

        // Populate the dashboard
        populateDashboard()

        return view
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun sendNotification(notification: Notification) {
        val reviewIntent = Intent(requireContext(), AdminReviewRequestActivity::class.java).apply {
            putExtra("requestId", notification.requestId)
        }
        val reviewPendingIntent: PendingIntent = PendingIntent.getActivity(
            requireContext(), 0, reviewIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val rejectIntent = Intent(requireContext(), RejectRequestReceiver::class.java).apply {
            putExtra("requestId", notification.requestId)
        }
        val rejectPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, rejectIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_outline_uread_chat_bubble)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(reviewPendingIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(notification.message))
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_received_call,
                getString(R.string.review_request),
                reviewPendingIntent
            )
            .addAction(
                R.drawable.ic_missed_call,
                getString(R.string.reject_request),
                rejectPendingIntent
            )

        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    inner class RejectRequestReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val requestId = intent?.getStringExtra("requestId") ?: return
            firebaseService.getHelpRequestById(requestId) { helpRequest ->
                if (helpRequest != null) {
                    helpRequest.requestStatus = RequestStatus.CANCELLED
                    helpRequest.requestResponse = RequestResponse.REJECTED
                    firebaseService.updateHelpRequest(helpRequest) {}
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