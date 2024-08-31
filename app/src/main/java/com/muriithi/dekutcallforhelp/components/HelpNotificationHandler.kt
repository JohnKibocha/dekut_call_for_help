package com.muriithi.dekutcallforhelp.handlers

import com.muriithi.dekutcallforhelp.beans.*
import com.muriithi.dekutcallforhelp.data.FirebaseService
import java.util.*
import kotlin.concurrent.timer

class HelpNotificationHandler(private val firebaseService: FirebaseService) {

    private val notificationQueue: Queue<Notification> = LinkedList()
    private val notificationCounter: MutableMap<String, Int> = mutableMapOf()
    private val checkInterval: Long = 500 // 0.5 seconds
    private val notificationInterval: Long = 300 // 0.3 seconds

    init {
        startNotificationHandler()
    }

    private fun startNotificationHandler() {
        timer(period = checkInterval) { // Every half a second
            checkForNewNotifications()
            sendNotificationFromQueue()
            updateQueue()
        }
    }

    private fun checkForNewNotifications() {
        firebaseService.getAllNotifications { notifications ->
            notifications?.forEach { notification ->
                if (shouldAddNotification(notification)) {
                    handleNotification(notification)
                }
            }
        }
    }

    private fun shouldAddNotification(notification: Notification): Boolean {
        var shouldAdd = false
        firebaseService.getHelpRequestById(notification.requestId) { helpRequest ->
            shouldAdd = helpRequest?.requestStatus == RequestStatus.PENDING &&
                        helpRequest.requestResponse == RequestResponse.PENDING &&
                        notification.status == NotificationStatus.UNREAD
        }
        return shouldAdd
    }

    private fun handleNotification(notification: Notification) {
        if (notificationQueue.contains(notification)) return

        notificationQueue.add(notification)
        notificationCounter[notification.notificationId] = notificationCounter.getOrDefault(notification.notificationId, 0) + 1

        Timer().schedule(object : TimerTask() {
            override fun run() {
                processNotification(notification)
            }
        }, notificationInterval)
    }

    private fun sendNotificationFromQueue() {
        val notification = notificationQueue.peek() ?: return
        // Logic to send notification
        // If notification needs to be sent twice, re-add to the back of the queue
        if ((notificationCounter[notification.notificationId] ?: 0) < 2) {
            notificationQueue.add(notification)
        }
        notificationQueue.poll()
    }

    private fun processNotification(notification: Notification) {
        firebaseService.getHelpRequestById(notification.requestId) { helpRequest ->
            if (helpRequest != null) {
                val count = notificationCounter[notification.notificationId] ?: 0
                if (count >= 2) {
                    helpRequest.requestStatus = RequestStatus.TIMEOUT
                    helpRequest.requestResponse = RequestResponse.REJECTED
                    notification.status = NotificationStatus.IGNORED
                } else {
                    helpRequest.requestStatus = RequestStatus.REVIEWING
                    helpRequest.requestResponse = RequestResponse.ACCEPTED
                    notification.status = NotificationStatus.READ
                }

                firebaseService.updateHelpRequest(helpRequest) {}
                firebaseService.updateNotification(notification) {}

                notificationQueue.remove(notification)
            }
        }
    }

    private fun updateQueue() {
        notificationQueue.removeIf { notification ->
            notification.status == NotificationStatus.READ || notification.status == NotificationStatus.IGNORED
        }
    }

    fun getProcessedNotifications(callback: (List<Notification>) -> Unit) {
        firebaseService.getAllNotifications { notifications ->
            val processedNotifications = notifications?.filter { notification ->
                notification.status == NotificationStatus.READ || notification.status == NotificationStatus.IGNORED
            } ?: emptyList()
            callback(processedNotifications)
        }
    }
}