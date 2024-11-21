// app/src/main/java/com/muriithi/dekutcallforhelp/beans/Rating.kt
package com.muriithi.dekutcallforhelp.beans

data class Rating(
    val ratingId: String = "",
    val userId: String = "",
    val officeId: String = "",
    val requestId: String = "",
    val officeRating: Float = 0f,
    val requestRating: Float = 0f,
    val officeReview: String = "",
    val requestReview: String = "",
    val timestamp: Long = System.currentTimeMillis()
)