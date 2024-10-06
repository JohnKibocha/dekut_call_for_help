// app/src/main/java/com/muriithi/dekutcallforhelp/beans/HelpRequest.kt
package com.muriithi.dekutcallforhelp.beans

/**
 * Data class representing a help request
 *
 * @property requestId unique identifier for the request
 * @property senderId the id of the user who sent the request
 * @property receiverId the id of the user who will receive the request
 * @property senderName the name of the user who sent the request
 * @property receiverName the name of the user who will receive the request
 * @property requestDate the date the request was sent
 * @property requestStatus the status of the request.
 * The property describes the current state of the request
 * @property requestResponse the response to the request.
 * The property describes the action taken on the request by the receiver
 * @property officeId the id of the office that will handle the request
 * @property description a brief description of the request
 * @property priority the priority level of the request
 * @property phoneNumber the phone number of the user who sent the request\
 * @property countryCode the country code of the user who sent the request
 * @property email the email address of the user who sent the request
 * @property responseTime the time taken to respond to the request
 * @property resolutionTime the time taken to resolve the request
 * @property processingTime the time taken to process the request.
 * Is given by `average((responseTime - requestDate) + (resolutionTime - requestDate)/2)`.
 * Shows the responsiveness of the office to the request
 */

data class HelpRequest(
    val requestId: String = "",
    val senderId: String = "",
    var senderName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val requestDate: String = "",
    var requestStatus: RequestStatus = RequestStatus.PENDING,
    var requestResponse: RequestResponse = RequestResponse.PENDING,
    var officeId: String = "",
    val description: String = "",
    val priority: Priority = Priority.HIGH,
    var phoneNumber: String = "",
    var countryCode: String = "",
    val email: String = "",
    var responseTime: String = "",
    var processingTime: String = "",
    var resolutionTime: String = ""
)

enum class RequestStatus {
    RESOLVED, PENDING, SUSPENDED, TIMEOUT, UNDER_REVIEW, CANCELLED
}

enum class RequestResponse {
    PENDING, ACCEPTED, REJECTED
}

enum class Priority {
    HIGH, MEDIUM, LOW
}