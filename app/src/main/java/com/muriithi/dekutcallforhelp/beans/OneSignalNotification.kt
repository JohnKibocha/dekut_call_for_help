// app/src/main/java/com/muriithi/dekutcallforhelp/beans/OneSignalNotification.kt:
package com.muriithi.dekutcallforhelp.beans

data class OneSignalNotification(
    val app_id: String,
    val include_aliases: Map<String, List<String>>? = null,
    val filters: List<Map<String, String>>? = null,
    val headings: Map<String, String>,
    val contents: Map<String, String>,
    val target_channel: String = "push"
)
