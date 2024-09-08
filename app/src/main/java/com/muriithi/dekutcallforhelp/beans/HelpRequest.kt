// app/src/main/java/com/muriithi/dekutcallforhelp/beans/HelpRequest.kt
package com.muriithi.dekutcallforhelp.beans

data class HelpRequest(
    val requestId: String = "",
    val senderId: String = "",
    var senderName : String = "",
    val receiverId: String = "",
    val requestDate: String = "",
    var requestStatus: RequestStatus = RequestStatus.PENDING,
    var requestResponse: RequestResponse = RequestResponse.PENDING,
    var officeId: String = "",
    val description: String = "",
    val priority: Priority = Priority.HIGH,
    var phoneNumber: String = "",
    val email: String = "",
    var responseTime: String = "",
    var resolutionTime: String = ""
)

enum class RequestStatus {
    RESOLVED, PENDING, SUSPENDED, TIMEOUT, REVIEWING, CANCELLED
}

enum class RequestResponse {
    PENDING, ACCEPTED, REJECTED
}

enum class Priority {
    HIGH, MEDIUM, LOW
}