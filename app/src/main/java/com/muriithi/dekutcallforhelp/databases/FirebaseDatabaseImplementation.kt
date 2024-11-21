// app/src/main/java/com/muriithi/dekutcallforhelp/data/FirebaseDatabaseImplementation.kt
package com.muriithi.dekutcallforhelp.databases

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muriithi.dekutcallforhelp.beans.HelpRequest
import com.muriithi.dekutcallforhelp.beans.Office
import com.muriithi.dekutcallforhelp.beans.Rating
import com.muriithi.dekutcallforhelp.beans.User
import com.muriithi.dekutcallforhelp.interfaces.FirebaseInterface
import java.text.SimpleDateFormat
import java.util.Locale

class FirebaseDatabaseImplementation : FirebaseInterface {
    private val database = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Data manipulation methods
    override fun <T> readData(node: String, clazz: Class<T>, callback: (T?) -> Unit) {
        val ref = database.getReference(node)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(clazz)
                Log.d("FirebaseDatabaseImpl", "Data read from $node: $data")
                callback(data)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseImpl", "Error reading data from $node: ${error.message}")
                callback(null)
            }
        })
    }

    override fun <T> writeData(node: String, data: T, callback: (Boolean) -> Unit) {
        val ref = database.getReference(node)
        ref.setValue(data).addOnCompleteListener { task ->
            Log.d("FirebaseDatabaseImpl", "Data written to $node: $data, success: ${task.isSuccessful}")
            callback(task.isSuccessful)
        }
    }

    override fun updateData(node: String, data: Map<String, Any>, callback: (Boolean) -> Unit) {
        val ref = database.getReference(node)
        ref.updateChildren(data).addOnCompleteListener { task ->
            Log.d("FirebaseDatabaseImpl", "Data updated at $node: $data, success: ${task.isSuccessful}")
            callback(task.isSuccessful)
        }
    }

    override fun deleteData(node: String, callback: (Boolean) -> Unit) {
        val ref = database.getReference(node)
        ref.removeValue().addOnCompleteListener { task ->
            Log.d("FirebaseDatabaseImpl", "Data deleted at $node, success: ${task.isSuccessful}")
            callback(task.isSuccessful)
        }
    }

    // Authentication methods
    override fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            Log.d("FirebaseDatabaseImpl", "Sign in with email: $email, success: ${task.isSuccessful}")
            callback(task.isSuccessful)
        }
    }

    override fun createAccount(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            Log.d("FirebaseDatabaseImpl", "Account created with email: $email, success: ${task.isSuccessful}")
            callback(task.isSuccessful)
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
        Log.d("FirebaseDatabaseImpl", "User signed out")
    }

    // User management methods
    override fun getCurrentUser(): User? {
        val currentUser = firebaseAuth.currentUser
        return if (currentUser != null) {
            val user = User()
            user.userId = currentUser.uid
            user.emailAddress = currentUser.email
            user.phoneNumber = currentUser.phoneNumber
            Log.d("FirebaseDatabaseImpl", "Current user: $user")
            user
        } else {
            Log.d("FirebaseDatabaseImpl", "No current user")
            null
        }
    }

    override fun getUserById(userId: String, callback: (User?) -> Unit) {
        val node = "users/$userId"
        readData(node, User::class.java) { user ->
            Log.d("FirebaseDatabaseImpl", "User retrieved by ID: $userId, user: $user")
            callback(user)
        }
    }

    override fun getUserByEmail(email: String, callback: (User?) -> Unit) {
        val ref = database.getReference("users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                val user = users.find { it.emailAddress == email }
                Log.d("FirebaseDatabaseImpl", "User retrieved by email: $email, user: $user")
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseImpl", "Error retrieving user by email: $email, error: ${error.message}")
                callback(null)
            }
        })
    }

    override fun updateUser(user: User, callback: (Boolean) -> Unit) {
        val node = "users/${user.userId}"
        writeData(node, user) { success ->
            Log.d("FirebaseDatabaseImpl", "User updated: $user, success: $success")
            callback(success)
        }
    }

    override fun deleteUser(userId: String, callback: (Boolean) -> Unit) {
        val node = "users/$userId"
        deleteData(node) { success ->
            Log.d("FirebaseDatabaseImpl", "User deleted: $userId, success: $success")
            callback(success)
        }
    }

    override fun getAllUsers(callback: (List<User>?) -> Unit) {
        val ref = database.getReference("users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                Log.d("FirebaseDatabaseImpl", "All users retrieved: $users")
                callback(users)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseImpl", "Error retrieving all users: ${error.message}")
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
                    user.firstName?.contains(query, ignoreCase = true) == true ||
                    user.lastName?.contains(query, ignoreCase = true) == true ||
                    user.emailAddress?.contains(query, ignoreCase = true) == true ||
                    user.phoneNumber?.contains(query, ignoreCase = true) == true ||
                    user.idNumber?.toString()?.contains(query, ignoreCase = true) == true ||
                    user.superuser.toString().contains(query, ignoreCase = true) ||
                    user.registrationNumber?.contains(query, ignoreCase = true) == true
                }
                Log.d("FirebaseDatabaseImpl", "Users searched with query: $query, result: $filteredUsers")
                callback(filteredUsers)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseImpl", "Error searching users with query: $query, error: ${error.message}")
                callback(null)
            }
        })
    }

    // HelpRequest management methods
    override fun createHelpRequest(helpRequest: HelpRequest, callback: (Boolean) -> Unit) {
        val node = "helpRequests/${helpRequest.requestId}"
        writeData(node, helpRequest) { success ->
            Log.d("FirebaseDatabaseImpl", "Help request created: $helpRequest, success: $success")
            callback(success)
        }
    }

    override fun getHelpRequestById(requestId: String, callback: (HelpRequest?) -> Unit) {
        val node = "helpRequests/$requestId"
        readData(node, HelpRequest::class.java) { helpRequest ->
            Log.d("FirebaseDatabaseImpl", "Help request retrieved by ID: $requestId, helpRequest: $helpRequest")
            callback(helpRequest)
        }
    }

    override fun getAllHelpRequests(callback: (List<HelpRequest>?) -> Unit) {
        val ref = database.getReference("helpRequests")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val helpRequests = snapshot.children.mapNotNull { it.getValue(HelpRequest::class.java) }
                Log.d("FirebaseDatabaseImpl", "All help requests retrieved: $helpRequests")
                callback(helpRequests)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseImpl", "Error retrieving all help requests: ${error.message}")
                callback(null)
            }
        })
    }

    override fun updateHelpRequest(helpRequest: HelpRequest, callback: (Boolean) -> Unit) {
        val node = "helpRequests/${helpRequest.requestId}"
        writeData(node, helpRequest) { success ->
            Log.d("FirebaseDatabaseImpl", "Help request updated: $helpRequest, success: $success")
            callback(success)
        }
    }

    override fun deleteHelpRequest(requestId: String, callback: (Boolean) -> Unit) {
        val node = "helpRequests/$requestId"
        deleteData(node) { success ->
            Log.d("FirebaseDatabaseImpl", "Help request deleted: $requestId, success: $success")
            callback(success)
        }
    }

    // Office management methods
    override fun createOffice(office: Office, callback: (Boolean) -> Unit) {
        val node = "offices/${office.officeId}"
        writeData(node, office) { success ->
            Log.d("FirebaseDatabaseImpl", "Office created: $office, success: $success")
            callback(success)
        }
    }

    override fun getOfficeById(officeId: String, callback: (Office?) -> Unit) {
        val node = "offices/$officeId"
        readData(node, Office::class.java) { office ->
            Log.d("FirebaseDatabaseImpl", "Office retrieved by ID: $officeId, office: $office")
            callback(office)
        }
    }

    override fun getOfficeNameById(officeId: String, callback: (String) -> Unit) {
        val node = "offices/$officeId"
        readData(node, Office::class.java) { office ->
            Log.d("FirebaseDatabaseImpl", "Office name retrieved by ID: $officeId, officeName: ${office?.officeName}")
            callback(office?.officeName ?: "")
        }
    }

    override fun getOfficeByType(officeType: String, callback: (Office?) -> Unit) {
        val ref = database.getReference("offices")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offices = snapshot.children.mapNotNull { it.getValue(Office::class.java) }
                val office = offices.find { it.officeType.toString() == officeType }
                Log.d("FirebaseDatabaseImpl", "Office retrieved by type: $officeType, office: $office")
                callback(office)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseImpl", "Error retrieving office by type: $officeType, error: ${error.message}")
                callback(null)
            }
        })
    }

    override fun getAllOffices(callback: (List<Office>?) -> Unit) {
        val ref = database.getReference("offices")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offices = snapshot.children.mapNotNull { it.getValue(Office::class.java) }
                Log.d("FirebaseDatabaseImpl", "All offices retrieved: $offices")
                callback(offices)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseImpl", "Error retrieving all offices: ${error.message}")
                callback(null)
            }
        })
    }

    override fun updateOffice(office: Office, callback: (Boolean) -> Unit) {
        val node = "offices/${office.officeId}"
        writeData(node, office) { success ->
            Log.d("FirebaseDatabaseImpl", "Office updated: $office, success: $success")
            callback(success)
        }
    }

    override fun deleteOffice(officeId: String, callback: (Boolean) -> Unit) {
        val node = "offices/$officeId"
        deleteData(node) { success ->
            Log.d("FirebaseDatabaseImpl", "Office deleted: $officeId, success: $success")
            callback(success)
        }
    }

    // Rating management methods
    override fun saveRating(rating: Rating, callback: (Boolean) -> Unit) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("ratings")
        databaseRef.child(rating.ratingId).setValue(rating)
            .addOnSuccessListener {
                Log.d("FirebaseDatabaseImpl", "Rating saved: $rating")
                callback(true)
            }
            .addOnFailureListener { error ->
                Log.e("FirebaseDatabaseImpl", "Error saving rating: $rating, error: ${error.message}")
                callback(false)
            }
    }

    override fun getRatingById(ratingId: String, callback: (Rating?) -> Unit) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("ratings")
        databaseRef.child(ratingId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rating = snapshot.getValue(Rating::class.java)
                Log.d("FirebaseDatabaseImpl", "Rating retrieved by ID: $ratingId, rating: $rating")
                callback(rating)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseImpl", "Error retrieving rating by ID: $ratingId, error: ${error.message}")
                callback(null)
            }
        })
    }

    override fun getRatingsByOfficeId(officeId: String, callback: (List<Rating>) -> Unit) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("ratings")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ratings = snapshot.children.mapNotNull { it.getValue(Rating::class.java) }
                val officeRatings = ratings.filter { it.officeId == officeId }
                Log.d("FirebaseDatabaseImpl", "Ratings retrieved by office ID: $officeId, ratings: $officeRatings")
                callback(officeRatings)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseImpl", "Error retrieving ratings by office ID: $officeId, error: ${error.message}")
                callback(emptyList())
            }
        })
    }

    override fun getHelpRequestsByOfficeId(officeId: String, callback: (List<HelpRequest>) -> Unit) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("helpRequests")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val helpRequests = snapshot.children.mapNotNull { it.getValue(HelpRequest::class.java) }
                val officeRequests = helpRequests.filter { it.officeId == officeId }
                Log.d("FirebaseDatabaseImpl", "Help requests retrieved by office ID: $officeId, requests: $officeRequests")
                callback(officeRequests)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseImpl", "Error retrieving help requests by office ID: $officeId, error: ${error.message}")
                callback(emptyList())
            }
        })
    }

    override fun getHelpRequestsSince(timestamp: Long, callback: (List<HelpRequest>) -> Unit) {
        val query = database.getReference("helpRequests")
            .orderByChild("requestDate")
            .startAt(timestamp.toDouble())

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val helpRequests = mutableListOf<HelpRequest>()
                for (data in snapshot.children) {
                    val request = data.getValue(HelpRequest::class.java)
                    if (request != null) {
                        val requestDate = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault()).parse(request.requestDate)
                        if (requestDate != null && requestDate.time >= timestamp) {
                            helpRequests.add(request)
                        }
                    }
                }
                Log.d("FirebaseDatabaseImpl", "Help requests retrieved since timestamp: $timestamp, requests: $helpRequests")
                callback(helpRequests)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseImpl", "Error retrieving help requests since timestamp: $timestamp, error: ${error.message}")
                callback(emptyList())
            }
        })
    }
}