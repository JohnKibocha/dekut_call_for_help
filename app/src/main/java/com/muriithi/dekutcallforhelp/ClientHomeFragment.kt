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
import android.util.Log
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
import com.google.firebase.messaging.FirebaseMessaging
import com.muriithi.dekutcallforhelp.beans.HelpRequest
import com.muriithi.dekutcallforhelp.beans.Notification
import com.muriithi.dekutcallforhelp.beans.NotificationStatus
import com.muriithi.dekutcallforhelp.beans.Office
import com.muriithi.dekutcallforhelp.beans.RequestStatus
import com.muriithi.dekutcallforhelp.beans.NotificationResponseStatus
import com.muriithi.dekutcallforhelp.beans.RequestResponse
import com.muriithi.dekutcallforhelp.beans.officeType
import com.muriithi.dekutcallforhelp.components.Formatter
import com.muriithi.dekutcallforhelp.data.FirebaseService
import java.util.Date
import java.util.UUID

class ClientHomeFragment : Fragment() {
    private val CHANNEL_ID = "help_request_channel"

    private lateinit var callButton: MaterialButton
    private val firebaseService = FirebaseService()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val formatter = Formatter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_home, container, false)
        callButton = view.findViewById(R.id.button_call)

        callButton.setOnClickListener {
            showHelpRequestForm()
        }

        // Create notification channel
        createNotificationChannel()

        // subscribe to FCM topic
        subscribeToFCMTopic()

        // Check for responses
        checkForNewResponses()

        return view
    }

    private fun createNotificationChannel() {
        val name = "Help Request Response Channel"
        val descriptionText = "Channel for receiving help request responses"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun subscribeToFCMTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("help_requests")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ClientHomeFragment", "Subscribed to FCM topic: help_requests")
                } else {
                    Log.e("ClientHomeFragment", "Failed to subscribe to FCM topic")
                }
            }
    }

    private fun sendNotification(notification: Notification) {
        val uniqueNotificationId = notification.notificationId.hashCode()

        // On notification tap, open the ClientHomeFragment
        val intent = Intent(requireContext(), ClientHomeFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("fragmentToLoad", "ClientHomeFragment")
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            requireContext(), 0, intent, PendingIntent.FLAG_MUTABLE
        )

        // Implement the Mark as Read notification action
        val markAsReadIntent = Intent(requireContext(), MarkAsReadReceiver::class.java).apply {
            action = "MARK_AS_READ"
            putExtra("notificationId", notification.notificationId)
            putExtra("requestId", notification.requestId)
        }
        val markAsReadPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, markAsReadIntent, PendingIntent.FLAG_MUTABLE
        )

        // Build the notification
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_outline_uread_chat_bubble)
            .setContentTitle(notification.title).setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setContentIntent(pendingIntent).setAutoCancel(true)
            .addAction(R.drawable.ic_filled_read, "Mark as Read", markAsReadPendingIntent)


        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(uniqueNotificationId, builder.build())
            } else {
                Log.w("AdminHomeFragment", "Notification not sent: Permission not granted")
                Toast.makeText(
                    requireContext(),
                    "Notification permission is not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    inner class MarkAsReadReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) {
                Log.e("MarkAsReadReceiver", "Context or Intent is null")
                return
            }

            // Validate notificationId and requestId extras
            val notificationId = intent.getStringExtra("notificationId")
            val requestId = intent.getStringExtra("requestId")

            if (notificationId.isNullOrEmpty() || requestId.isNullOrEmpty()) {
                Log.e("MarkAsReadReceiver", "Missing or invalid notificationId or requestId")
                Toast.makeText(context, "Failed to mark as read: Missing data", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            // Fetch the notification and update its status to READ
            firebaseService.getNotificationById(notificationId) { notification ->
                if (notification != null) {
                    notification.status = NotificationStatus.READ
                    notification.responseStatus = NotificationResponseStatus.RESPONDED
                    firebaseService.updateNotification(notification) {
                        if (it) {
                            Log.d("MarkAsReadReceiver", "Notification marked as read")
                        } else {
                            Log.e("MarkAsReadReceiver", "Failed to update notification status")
                        }
                    }

                    // Fetch the corresponding help request and update its status
                    firebaseService.getHelpRequestById(requestId) { helpRequest ->
                        if (helpRequest != null) {
                            helpRequest.requestStatus = RequestStatus.REVIEWING
                            helpRequest.requestResponse = RequestResponse.ACCEPTED
                            helpRequest.responseTime = formatter.formatDateToString(Date())
                            firebaseService.updateHelpRequest(helpRequest) {
                                if (it) {
                                    Log.d("MarkAsReadReceiver", "Help request status updated")
                                    // remove the notification from the notification tray
                                    NotificationManagerCompat.from(context)
                                        .cancel(notificationId.hashCode())
                                } else {
                                    Log.e(
                                        "MarkAsReadReceiver", "Failed to update help request status"
                                    )
                                }
                            }
                        } else {
                            Log.e("MarkAsReadReceiver", "Help request not found for ID: $requestId")
                        }
                    }
                } else {
                    Log.e("MarkAsReadReceiver", "Notification not found for ID: $notificationId")
                }
            }
        }
    }


    private fun checkForNewResponses() {
        displayResponse()
        // every 0.5 seconds, check for new notifications, and
        val timer = object : Thread() {
            override fun run() {
                while (!isInterrupted) {
                    try {
                        sleep(600000) // 10 minutes
                        activity?.runOnUiThread {
                            displayResponse()
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        timer.start()
    }

    private fun displayResponse() {
        firebaseService.getAllNotifications { notifications ->
            if (notifications != null) {
                for (notification in notifications) {
                    if (notification.status == NotificationStatus.UNREAD) {
                        val helpRequestId = notification.requestId
                        firebaseService.getHelpRequestById(helpRequestId) { helpRequest ->
                            if (helpRequest != null) {

                                val message = when (helpRequest.requestStatus) {
                                    RequestStatus.REVIEWING -> "The help request : ${helpRequest.description}, sent on ${helpRequest.requestDate} is being reviewed"
                                    RequestStatus.RESOLVED -> "The help request : ${helpRequest.description}, sent on ${helpRequest.requestDate} has been resolved."
                                    RequestStatus.CANCELLED -> "The help request : ${helpRequest.description}, sent on ${helpRequest.requestDate} has been cancelled."
                                    RequestStatus.TIMEOUT -> "Could not get a response for the help request : ${helpRequest.description} on time."
                                    RequestStatus.SUSPENDED -> "There is an issue with the help request : ${helpRequest.description}, sent on ${helpRequest.requestDate}. We will let you know once it is resolved."
                                    else -> ""
                                }

                                if (message.isNotEmpty()) {
                                    val notificationUpdate = helpRequest.let {
                                        Notification(
                                            notificationId = UUID.randomUUID().toString(),
                                            title = "Help Request Update",
                                            message = message,
                                            timestamp = formatter.formatDateToString(Date()),
                                            senderID = it.receiverId,
                                            receiverID = helpRequest.senderId,
                                            requestId = helpRequest.requestId
                                        )
                                    }
                                    firebaseService.createNotification(notificationUpdate) { success ->
                                        if (success) {
                                            sendNotification(notificationUpdate)
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

    private fun showHelpRequestForm() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_help_request, null)
        val officeSpinner: Spinner = dialogView.findViewById(R.id.office_spinner)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.description_edit_text)

        val officeNames =
            arrayOf("General Office", "Security Office", "Medical Office", "Student Welfare Office")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, officeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        officeSpinner.adapter = adapter

        AlertDialog.Builder(context).setTitle("Request Help").setView(dialogView)
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
            }.setNegativeButton("Cancel", null).show()
    }

    private fun sendHelpRequest(officeName: String, officeType: officeType, description: String) {
        val currentUser = firebaseAuth.currentUser
        val senderId = currentUser?.uid ?: return

        firebaseService.getAllUsers { users ->
            val receiver = users?.find { it.superuser } ?: return@getAllUsers
            val receiverId = receiver.userId ?: return@getAllUsers
            val formatter = Formatter()
            val sender = users.find { it.userId == senderId } ?: return@getAllUsers

            val phoneNumber = sender.phoneNumber ?: ""
            val senderName = currentUser.displayName ?: ""

            // Log the sender object with all its properties
            Log.d("ClientHomeFragment", "Sender: $sender")

            val helpRequest = HelpRequest(
                requestId = UUID.randomUUID().toString(),
                senderId = senderId,
                senderName = formatter.formatName(senderName),
                receiverId = receiverId,
                requestDate = formatter.formatDateToString(Date()),
                officeId = UUID.randomUUID().toString(),
                description = formatter.formatName(description),
                phoneNumber = formatter.stripPhoneNumberFormatting(phoneNumber),
                email = formatter.formatEmail(currentUser.email ?: ""),
                responseTime = "",
                resolutionTime = ""
            )

            // Trigger FCM notification
            val notification = Notification(
                notificationId = UUID.randomUUID().toString(),
                title = "New Help Request",
                message = "Help request from ${helpRequest.senderName}: ${helpRequest.description}",
                timestamp = Date().toString(),
                senderID = helpRequest.senderId,
                receiverID = helpRequest.receiverId,
                requestId = helpRequest.requestId
            )

            firebaseService.createHelpRequest(helpRequest) { success ->
                if (success) {
                    firebaseService.getOfficeByType(officeType.toString()) { existingOffice ->
                        if (existingOffice == null) {
                            val office = Office(
                                officeId = helpRequest.officeId,
                                officeName = officeName,
                                officeType = officeType,
                                createdOn = formatter.formatDateToString(Date()),
                                requestId = helpRequest.requestId
                            )
                            firebaseService.createOffice(office) { officeSuccess ->
                                if (officeSuccess) {
                                    Log.d("ClientHomeFragment", "Office created successfully")

                                } else {
                                    Toast.makeText(
                                        context, "Failed to create office", Toast.LENGTH_SHORT
                                    ).show()
                                } // end createOffice
                            } // end createOffice
                        } else {
                            Log.d("ClientHomeFragment", "Office already exists")
                        } // end if (existingOffice == null)
                        firebaseService.createHelpRequest(helpRequest) { helpRequestSuccess ->
                            if (helpRequestSuccess) {
                                firebaseService.createNotification(notification) { notificationSuccess ->
                                    if (notificationSuccess) {
                                        Log.w(
                                            "ClientHomeFragment",
                                            "Help Request Notificaiton Created Successfully"
                                        )
                                        Toast.makeText(
                                            requireContext(),
                                            "Help Request Created Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Log.e(
                                            "ClientHomeFragment",
                                            "Failed to create notification"
                                        )
                                    } // end createNotification
                                } // end createNotification
                            } else {
                                Log.e(
                                    "ClientHomeFragment",
                                    "Failed to create help request"
                                )
                            } // end createHelpRequest
                        } // end createHelpRequest
                    } // end createOffice
                } else {
                    Toast.makeText(
                        context, "Failed to send help request", Toast.LENGTH_SHORT
                    ).show()
                } // end createHelpRequest
            } // end createHelpRequest
        } // end getAllUsers
    } // end sendHelpRequest
} // end ClientHomeFragment




