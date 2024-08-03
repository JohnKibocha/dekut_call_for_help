// app/src/main/java/com/muriithi/dekutcallforhelp/data/FirebaseDatabaseImpl.kt
package com.muriithi.dekutcallforhelp.data

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseAuth
import com.muriithi.dekutcallforhelp.interfaces.FirebaseInterface
import com.muriithi.dekutcallforhelp.components.Formatter
import com.muriithi.dekutcallforhelp.components.Validator
import com.muriithi.dekutcallforhelp.beans.User

class FirebaseDatabaseImpl : FirebaseInterface {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val formatter = Formatter()
    private val validator = Validator()

    override fun <T> readData(node: String, clazz: Class<T>, callback: (T?) -> Unit) {
        val ref = database.getReference(node)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(clazz)
                callback(data)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    override fun <T> writeData(node: String, data: T, callback: (Boolean) -> Unit) {
        val ref = database.getReference(node)
        ref.setValue(data).addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }

    override fun updateData(node: String, data: Map<String, Any>, callback: (Boolean) -> Unit) {
        val ref = database.getReference(node)
        ref.updateChildren(data).addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }

    override fun deleteData(node: String, callback: (Boolean) -> Unit) {
        val ref = database.getReference(node)
        ref.removeValue().addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }

    override fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }

    override fun createAccount(email: String, password: String, callback: (Boolean) -> Unit) {
        if (!validator.validateEmail(email)) {
            callback(false)
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }

    override fun signOut() {
        auth.signOut()
    }
}