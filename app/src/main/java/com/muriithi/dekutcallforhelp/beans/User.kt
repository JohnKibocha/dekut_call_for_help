// app/src/main/java/com/muriithi/dekutcallforhelp/beans/User.kt
package com.muriithi.dekutcallforhelp.beans

class User {
    var userId: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var course: String? = null
    var school: String? = null
    var registrationNumber: String? = null
    var idNumber: Int? = null
    var dateOfBirth: String? = null
    var emailAddress: String? = null
    var phoneNumber: String? = null
    var profilePhoto: String? = null
    var superuser: Boolean = false // Default value
    var countryCode: String = "254" // Default value
    var oneSignalPlayerId: String? = null // OneSignal User ID
    var latitude: Double? = null // Location latitude
    var longitude: Double? = null // Location longitude
}