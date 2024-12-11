package com.muriithi.dekutcallforhelp.interfaces

import com.muriithi.dekutcallforhelp.beans.HelpRequest
import com.muriithi.dekutcallforhelp.beans.Office
import com.muriithi.dekutcallforhelp.beans.User

interface FirebaseInterface {
    // Data manipulation methods
    fun <T> readData(node: String, clazz: Class<T>, callback: (T?) -> Unit)
    fun <T> writeData(node: String, data: T, callback: (Boolean) -> Unit)
    fun updateData(node: String, data: Map<String, Any>, callback: (Boolean) -> Unit)
    fun deleteData(node: String, callback: (Boolean) -> Unit)

    // Authentication methods
    fun signIn(email: String, password: String, callback: (Boolean) -> Unit)
    fun createAccount(email: String, password: String, callback: (Boolean) -> Unit)
    fun signOut()

    // User management methods
    fun getCurrentUser(): User?
    fun getUserById(userId: String, callback: (User?) -> Unit)
    fun getUserByEmail(email: String, callback: (User?) -> Unit)
    fun updateUser(user: User, callback: (Boolean) -> Unit)
    fun deleteUser(userId: String, callback: (Boolean) -> Unit)
    fun getAllUsers(callback: (List<User>?) -> Unit)
    fun searchUsers(query: String, callback: (List<User>?) -> Unit)

    // HelpRequest management methods
    fun createHelpRequest(helpRequest: HelpRequest, callback: (Boolean) -> Unit)
    fun getHelpRequestById(requestId: String, callback: (HelpRequest?) -> Unit)
    fun getAllHelpRequests(callback: (List<HelpRequest>?) -> Unit)
    fun updateHelpRequest(helpRequest: HelpRequest, callback: (Boolean) -> Unit)
    fun deleteHelpRequest(requestId: String, callback: (Boolean) -> Unit)

    // Office management methods
    fun createOffice(office: Office, callback: (Boolean) -> Unit)
    fun getOfficeById(officeId: String, callback: (Office?) -> Unit)
    fun getOfficeNameById(officeId: String, callback: (String) -> Unit)
    fun getOfficeByType(officeType: String, callback: (Office?) -> Unit)
    fun getAllOffices(callback: (List<Office>?) -> Unit)
    fun updateOffice(office: Office, callback: (Boolean) -> Unit)
    fun deleteOffice(officeId: String, callback: (Boolean) -> Unit)
}