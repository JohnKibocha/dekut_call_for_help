package com.muriithi.dekutcallforhelp.data

data class FirebaseMessagingTopics(
    val topics: MutableList<String> = mutableListOf()
) {
    fun addTopic(topic: String) {
        if (!topics.contains(topic)) {
            topics.add(topic)
        }
    }

    fun removeTopic(topic: String) {
        topics.remove(topic)
    }

    fun getTopics(): List<String> {
        return topics
    }
}