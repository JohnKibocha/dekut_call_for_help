package com.muriithi.dekutcallforhelp.databases

import com.muriithi.dekutcallforhelp.beans.HelpRequest
import com.muriithi.dekutcallforhelp.beans.Office
import com.muriithi.dekutcallforhelp.beans.User
import com.muriithi.dekutcallforhelp.interfaces.FirebaseInterface

class FirebaseService : FirebaseInterface {
    private val firebaseDatabase: FirebaseInterface = FirebaseDatabaseImplementation()

    // Data manipulation methods
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

    // Authentication methods
    override fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseDatabase.signIn(email, password, callback)
    }

    override fun createAccount(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseDatabase.createAccount(email, password, callback)
    }

    override fun signOut() {
        firebaseDatabase.signOut()
    }

    // User management methods

    override fun getCurrentUser(): User? {
        return firebaseDatabase.getCurrentUser()
    }

    override fun getUserById(userId: String, callback: (User?) -> Unit) {
        firebaseDatabase.getUserById(userId, callback)
    }

    override fun getUserByEmail(email: String, callback: (User?) -> Unit) {
        firebaseDatabase.getUserByEmail(email, callback)
    }

    override fun updateUser(user: User, callback: (Boolean) -> Unit) {
        firebaseDatabase.updateUser(user, callback)
    }

    override fun deleteUser(userId: String, callback: (Boolean) -> Unit) {
        firebaseDatabase.deleteUser(userId, callback)
    }

    override fun getAllUsers(callback: (List<User>?) -> Unit) {
        firebaseDatabase.getAllUsers(callback)
    }

    override fun searchUsers(query: String, callback: (List<User>?) -> Unit) {
        firebaseDatabase.searchUsers(query, callback)
    }

    // HelpRequest management methods
    override fun createHelpRequest(helpRequest: HelpRequest, callback: (Boolean) -> Unit) {
        firebaseDatabase.createHelpRequest(helpRequest, callback)
    }

    override fun getHelpRequestById(requestId: String, callback: (HelpRequest?) -> Unit) {
        firebaseDatabase.getHelpRequestById(requestId, callback)
    }

    override fun getAllHelpRequests(callback: (List<HelpRequest>?) -> Unit) {
        firebaseDatabase.getAllHelpRequests(callback)
    }

    override fun updateHelpRequest(helpRequest: HelpRequest, callback: (Boolean) -> Unit) {
        firebaseDatabase.updateHelpRequest(helpRequest, callback)
    }

    override fun deleteHelpRequest(requestId: String, callback: (Boolean) -> Unit) {
        firebaseDatabase.deleteHelpRequest(requestId, callback)
    }

    // Office management methods
    override fun createOffice(office: Office, callback: (Boolean) -> Unit) {
        firebaseDatabase.createOffice(office, callback)
    }

    override fun getOfficeById(officeId: String, callback: (Office?) -> Unit) {
        firebaseDatabase.getOfficeById(officeId, callback)
    }

    override fun getOfficeNameById(officeId: String, callback: (String) -> Unit) {
        firebaseDatabase.getOfficeNameById(officeId, callback)
    }

    override fun getOfficeByType(officeType: String, callback: (Office?) -> Unit) {
        firebaseDatabase.getOfficeByType(officeType, callback)
    }

    override fun getAllOffices(callback: (List<Office>?) -> Unit) {
        firebaseDatabase.getAllOffices(callback)
    }

    override fun updateOffice(office: Office, callback: (Boolean) -> Unit) {
        firebaseDatabase.updateOffice(office, callback)
    }

    override fun deleteOffice(officeId: String, callback: (Boolean) -> Unit) {
        firebaseDatabase.deleteOffice(officeId, callback)
    }


}