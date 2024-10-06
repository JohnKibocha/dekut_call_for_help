// app/src/main/java/com/muriithi/dekutcallforhelp/beans/Office.kt
package com.muriithi.dekutcallforhelp.beans

data class Office(
    val officeId: String = "",
    val createdOn: String = "",
    var requestId: String = "",
    val officeName: String = "",
    val officeType: officeType = com.muriithi.dekutcallforhelp.beans.officeType.GENERAL_OFFICE,
)

enum class officeType {
    GENERAL_OFFICE, SECURITY_OFFICE, MEDICAL_OFFICE, STUDENT_WELFARE_OFFICE
}