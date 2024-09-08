package com.muriithi.dekutcallforhelp.beans

data class Notification(
    var notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: String = "",
    val senderID: String = "",
    val receiverID: String = "",
    var status: NotificationStatus = NotificationStatus.UNREAD,
    val type: NotificationType = NotificationType.MESSAGE,
    val requestId: String = "",
    val priority: NotificationPriority = NotificationPriority.HIGH,
    var responseStatus: NotificationResponseStatus = NotificationResponseStatus.PENDING
)

enum class NotificationStatus {
    READ, UNREAD, IGNORED, DELETED
}

enum class NotificationType {
    ALERT, REMINDER, MESSAGE
}

enum class NotificationPriority {
    HIGH, MEDIUM, LOW
}

enum class NotificationResponseStatus {
    PENDING, RESPONDED
}