// app/src/main/java/com/muriithi/dekutcallforhelp/components/Authorizer.kt
package com.muriithi.dekutcallforhelp.components

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muriithi.dekutcallforhelp.beans.User

class Authorizer {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun authorizeAdmin(userId: String, callback: (Boolean) -> Unit) {
        val userRef = database.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                callback(user?.superuser == true)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }
}