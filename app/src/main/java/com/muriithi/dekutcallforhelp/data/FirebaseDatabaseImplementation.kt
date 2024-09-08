// app/src/main/java/com/muriithi/dekutcallforhelp/data/FirebaseDatabaseImplementation.kt
package com.muriithi.dekutcallforhelp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muriithi.dekutcallforhelp.beans.HelpRequest
import com.muriithi.dekutcallforhelp.beans.Notification
import com.muriithi.dekutcallforhelp.beans.Office
import com.muriithi.dekutcallforhelp.beans.User
import com.muriithi.dekutcallforhelp.components.Validator
import com.muriithi.dekutcallforhelp.interfaces.FirebaseInterface

class FirebaseDatabaseImplementation : FirebaseInterface {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val validator = Validator()

    // Data manipulation methods
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

    // Authentication methods
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

    // User management methods
    override fun getCurrentUser(): User? {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            val user = User()
            user.userId = currentUser.uid
            user.emailAddress = currentUser.email
            user.phoneNumber = currentUser.phoneNumber
            user
        } else {
            null
        }
    }

    override fun getUserById(userId: String, callback: (User?) -> Unit) {
        val node = "users/$userId"
        readData(node, User::class.java) { user ->
            callback(user)
        }
    }

    override fun getUserByEmail(email: String, callback: (User?) -> Unit) {
        val ref = database.getReference("users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                val user = users.find { it.emailAddress == email }
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    override fun updateUser(user: User, callback: (Boolean) -> Unit) {
        val node = "users/${user.userId}"
        writeData(node, user) { success ->
            callback(success)
        }
    }

    override fun deleteUser(userId: String, callback: (Boolean) -> Unit) {
        val node = "users/$userId"
        deleteData(node) { success ->
            callback(success)
        }
    }

    override fun getAllUsers(callback: (List<User>?) -> Unit) {
        val ref = database.getReference("users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                callback(users)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    override fun searchUsers(query: String, callback: (List<User>?) -> Unit) {
        val ref = database.getReference("users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                val filteredUsers = users.filter { user ->
                    user.firstName?.contains(
                        query,
                        ignoreCase = true
                    ) == true || user.lastName?.contains(
                        query,
                        ignoreCase = true
                    ) == true || user.emailAddress?.contains(
                        query,
                        ignoreCase = true
                    ) == true || user.phoneNumber?.contains(
                        query,
                        ignoreCase = true
                    ) == true || user.idNumber?.toString()?.contains(
                        query, ignoreCase = true
                    ) == true || user.superuser.toString()
                        .contains(query, ignoreCase = true) || user.registrationNumber?.contains(
                        query, ignoreCase = true
                    ) == true
                }
                callback(filteredUsers)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    // HelpRequest management methods
    override fun createHelpRequest(helpRequest: HelpRequest, callback: (Boolean) -> Unit) {
        val node = "helpRequests/${helpRequest.requestId}"
        writeData(node, helpRequest, callback)
    }


    override fun getHelpRequestById(requestId: String, callback: (HelpRequest?) -> Unit) {
        val node = "helpRequests/$requestId"
        readData(node, HelpRequest::class.java) { helpRequest ->
            callback(helpRequest)
        }
    }

    override fun getAllHelpRequests(callback: (List<HelpRequest>?) -> Unit) {
        val ref = database.getReference("helpRequests")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val helpRequests =
                    snapshot.children.mapNotNull { it.getValue(HelpRequest::class.java) }
                callback(helpRequests)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    override fun updateHelpRequest(helpRequest: HelpRequest, callback: (Boolean) -> Unit) {
        val node = "helpRequests/${helpRequest.requestId}"
        writeData(node, helpRequest) { success ->
            callback(success)
        }
    }

    override fun deleteHelpRequest(requestId: String, callback: (Boolean) -> Unit) {
        val node = "helpRequests/$requestId"
        deleteData(node) { success ->
            callback(success)
        }
    }

    // Office management methods
    override fun createOffice(office: Office, callback: (Boolean) -> Unit) {
        val node = "offices/${office.officeId}"
        writeData(node, office, callback)
    }

    override fun getOfficeById(officeId: String, callback: (Office?) -> Unit) {
        val node = "offices/$officeId"
        readData(node, Office::class.java) { office ->
            callback(office)
        }
    }

    override fun getOfficeByType(officeType: String, callback: (Office?) -> Unit) {
        val ref = database.getReference("offices")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offices = snapshot.children.mapNotNull { it.getValue(Office::class.java) }
                val office = offices.find { it.officeType.toString() == officeType }
                callback(office)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    override fun getAllOffices(callback: (List<Office>?) -> Unit) {
        val ref = database.getReference("offices")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offices = snapshot.children.mapNotNull { it.getValue(Office::class.java) }
                callback(offices)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    override fun updateOffice(office: Office, callback: (Boolean) -> Unit) {
        val node = "offices/${office.officeId}"
        writeData(node, office) { success ->
            callback(success)
        }
    }

    override fun deleteOffice(officeId: String, callback: (Boolean) -> Unit) {
        val node = "offices/$officeId"
        deleteData(node) { success ->
            callback(success)
        }
    }

// Notification methods

    override fun createNotification(notification: Notification, callback: (Boolean) -> Unit) {
        val node = "notifications/${notification.notificationId}"
        writeData(node, notification, callback)
    }


    override fun getNotificationById(notificationId: String, callback: (Notification?) -> Unit) {
        val node = "notifications/$notificationId"
        readData(node, Notification::class.java) { notification ->
            callback(notification)
        }
    }

    override fun getAllNotifications(callback: (List<Notification>?) -> Unit) {
        val ref = database.getReference("notifications")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifications =
                    snapshot.children.mapNotNull { it.getValue(Notification::class.java) }
                callback(notifications)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }


    override fun updateNotification(notification: Notification, callback: (Boolean) -> Unit) {
        val node = "notifications/${notification.notificationId}"
        writeData(node, notification) { success ->
            callback(success)
        }
    }

    override fun deleteNotification(notificationId: String, callback: (Boolean) -> Unit) {
        val node = "notifications/$notificationId"
        deleteData(node) { success ->
            callback(success)
        }
    }


}