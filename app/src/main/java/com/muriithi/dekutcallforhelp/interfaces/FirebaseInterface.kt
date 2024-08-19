// app/src/main/java/com/muriithi/dekutcallforhelp/interfaces/FirebaseInterface.kt
package com.muriithi.dekutcallforhelp.interfaces

interface FirebaseInterface {
    fun <T> readData(node: String, clazz: Class<T>, callback: (T?) -> Unit)
    fun <T> writeData(node: String, data: T, callback: (Boolean) -> Unit)
    fun updateData(node: String, data: Map<String, Any>, callback: (Boolean) -> Unit)
    fun deleteData(node: String, callback: (Boolean) -> Unit)
    fun signIn(email: String, password: String, callback: (Boolean) -> Unit)
    fun createAccount(email: String, password: String, callback: (Boolean) -> Unit)
    fun signOut()
}