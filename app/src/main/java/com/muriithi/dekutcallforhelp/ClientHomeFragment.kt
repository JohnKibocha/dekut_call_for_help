package com.muriithi.dekutcallforhelp

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.muriithi.dekutcallforhelp.beans.HelpRequest
import com.muriithi.dekutcallforhelp.beans.Notification
import com.muriithi.dekutcallforhelp.beans.NotificationStatus
import com.muriithi.dekutcallforhelp.beans.Office
import com.muriithi.dekutcallforhelp.beans.RequestResponse
import com.muriithi.dekutcallforhelp.beans.RequestStatus
import com.muriithi.dekutcallforhelp.beans.notificationResponseStatus
import com.muriithi.dekutcallforhelp.beans.officeType
import com.muriithi.dekutcallforhelp.components.Formatter
import com.muriithi.dekutcallforhelp.data.FirebaseService
import com.muriithi.dekutcallforhelp.handlers.HelpNotificationHandler
import java.util.Date
import java.util.LinkedList
import java.util.Queue
import java.util.UUID

class ClientHomeFragment : Fragment() {
    private val CHANNEL_ID = "help_request_channel"
    private val NOTIFICATION_ID = 1

    private lateinit var helpNotificationHandler: HelpNotificationHandler
    private lateinit var callButton: MaterialButton
    private val firebaseService = FirebaseService()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval: Long = 500 // 0.5 seconds
    private val helpRequestQueue: Queue<HelpRequest> = LinkedList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_home, container, false)
        callButton = view.findViewById(R.id.button_call)

        helpNotificationHandler = HelpNotificationHandler(firebaseService)
        callButton.setOnClickListener {
            showHelpRequestForm()
        }
        // Create notification channel
        createNotificationChannel()
        startCheckingForResponses()

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

    private fun startCheckingForResponses() {
        // check for responses on app start then every 0.5 seconds
        checkForResponses()
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkForResponses()
                handler.postDelayed(this, checkInterval)
            }
        }, checkInterval)
    }

    private fun checkForResponses() {
        helpNotificationHandler.getProcessedNotifications { notifications ->
            notifications.forEach { notification ->
                firebaseService.getHelpRequestById(notification.requestId) { helpRequest ->
                    if (helpRequest != null && !helpRequestQueue.contains(helpRequest)) {
                        helpRequestQueue.add(helpRequest)
                        val message = when (notification.status) {
                            NotificationStatus.READ -> "Your help request ${notification.message} is being reviewed"
                            NotificationStatus.IGNORED -> "Your help request ${notification.message} has been cancelled"
                            else -> "Your help request ${notification.message} has been updated"
                        }

                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                        // Update notification response status
                        notification.responseStatus = notificationResponseStatus.RESPONDED
                        firebaseService.updateNotification(notification) {}

                        // Send notification
                        sendNotification(notification)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        helpRequestQueue.clear()
    }

    private fun showHelpRequestForm() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_help_request, null)
        val officeSpinner: Spinner = dialogView.findViewById(R.id.office_spinner)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.description_edit_text)

        // Populate the spinner with office names
        val officeNames =
            arrayOf("General Office", "Security Office", "Medical Office", "Student Welfare Office")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, officeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        officeSpinner.adapter = adapter

        // Set up AlertDialog
        AlertDialog.Builder(context)
            .setTitle("Request Help")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val selectedOffice = officeSpinner.selectedItem.toString()
                val description = descriptionEditText.text.toString()
                val officeType = when (selectedOffice) {
                    "General Office" -> officeType.GENERAL_OFFICE
                    "Security Office" -> officeType.SECURITY_OFFICE
                    "Medical Office" -> officeType.MEDICAL_OFFICE
                    "Student Welfare Office" -> officeType.STUDENT_WELFARE_OFFICE
                    else -> officeType.GENERAL_OFFICE
                }
                sendHelpRequest(selectedOffice, officeType, description)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendHelpRequest(officeName: String, officeType: officeType, description: String) {
        // Gather data from the device (e.g., location)
        val currentUser = firebaseAuth.currentUser
        val senderId = currentUser?.uid ?: return

        // Find a receiver (superuser)
        firebaseService.getAllUsers { users ->
            val receiver = users?.find { it.superuser } ?: return@getAllUsers
            val receiverId = receiver.userId ?: return@getAllUsers
            val formatter = Formatter()

            // Create HelpRequest
            val helpRequest = HelpRequest(
                requestId = UUID.randomUUID().toString(),
                senderId = senderId,
                receiverId = receiverId,
                requestDate = Date().toString(),
                officeId = UUID.randomUUID().toString(),
                description = formatter.formatName(description),
                phoneNumber = currentUser.phoneNumber ?: "",
                email = currentUser.email ?: "",
                responseTime = "",
                resolutionTime = ""
            )
            // Create Notification
            val notification = Notification(
                notificationId = UUID.randomUUID().toString(),
                title = "New Help Request",
                message = "Help request from ${currentUser.displayName}: $description",
                timestamp = Date().toString(),
                senderID = senderId,
                receiverID = receiverId,
                requestId = helpRequest.requestId
            )

            // Create Office
            val office = Office(
                officeId = helpRequest.officeId,
                officeName = officeName,
                officeType = officeType,
                createdOn = Date().toString(),
                requestId = helpRequest.requestId
            )

            // Save to database
            firebaseService.createHelpRequest(helpRequest) { success ->
                if (success) {
                    firebaseService.createOffice(office) { officeSuccess ->
                        if (officeSuccess) {
                            firebaseService.createNotification(notification) { notificationSuccess ->
                                if (notificationSuccess) {
                                    Toast.makeText(
                                        context,
                                        "Help request sent successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to send notification",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to send help request",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}