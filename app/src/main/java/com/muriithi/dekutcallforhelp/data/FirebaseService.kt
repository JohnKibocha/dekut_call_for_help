// app/src/main/java/com/muriithi/dekutcallforhelp/data/FirebaseService.kt
package com.muriithi.dekutcallforhelp.data

import com.muriithi.dekutcallforhelp.interfaces.FirebaseInterface

class FirebaseService : FirebaseInterface {
    private val firebaseDatabase: FirebaseInterface = FirebaseDatabaseImpl()

    override fun <T> readData(node: String, clazz: Class<T>, callback: (T?) -> Unit) {
        firebaseDatabase.readData(node, clazz, callback)
    }

    override fun <T> writeData(node: String, data: T, callback: (Boolean) -> Unit) {
        firebaseDatabase.writeData(node, data, callback)
    }

    override fun updateData(node: String, data: Map<String, Any>, callback: (Boolean) -> Unit) {
        firebaseDatabase.updateData(node, data, callback)
    }

    override fun deleteData(node: String, callback: (Boolean) -> Unit) {
        firebaseDatabase.deleteData(node, callback)
    }

    override fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseDatabase.signIn(email, password, callback)
    }

    override fun createAccount(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseDatabase.createAccount(email, password, callback)
    }

    override fun signOut() {
        firebaseDatabase.signOut()
    }
}