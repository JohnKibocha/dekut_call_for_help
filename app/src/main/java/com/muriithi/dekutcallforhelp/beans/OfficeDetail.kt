// OfficeDetail.kt
package com.muriithi.dekutcallforhelp.beans

data class OfficeDetail(
    val visitor: String,
    val visitDate: String,
    val officer: String,
    val rating: Double,
    val status: RequestStatus,
    val senderId: String
)