// app/src/main/java/com/muriithi/dekutcallforhelp/beans/User.kt
package com.muriithi.dekutcallforhelp.beans

import java.util.Date

class User {
    var userId: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var course: String? = null
    var school: String? = null
    var registrationNumber: String? = null
    var idNumber: Int? = null
    var dateOfBirth: Date? = null
    var emailAddress: String? = null
    var phoneNumber: String? = null
    var isSuperuser: Boolean = false
}