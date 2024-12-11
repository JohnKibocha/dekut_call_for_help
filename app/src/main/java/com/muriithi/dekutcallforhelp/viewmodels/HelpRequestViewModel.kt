package com.muriithi.dekutcallforhelp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muriithi.dekutcallforhelp.beans.HelpRequest
import com.muriithi.dekutcallforhelp.beans.Rating
import com.muriithi.dekutcallforhelp.databases.FirebaseService
import java.text.SimpleDateFormat
import java.util.Locale

class HelpRequestViewModel(val currentUserId: String?) : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("helpRequests")
    private val _helpRequests = MutableLiveData<List<HelpRequest>>()
    val helpRequests: LiveData<List<HelpRequest>> get() = _helpRequests

    var currentOfficeId: String? = null
    var currentRequestId: String? = null

    private val firebaseService = FirebaseService()

    init {
        fetchHelpRequests()
    }

    private fun fetchHelpRequests() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = snapshot.children.mapNotNull { it.getValue(HelpRequest::class.java) }
                val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())

                val sortedRequests = requests.filter {
                    currentUserId != null && (it.senderId == currentUserId || it.receiverId == currentUserId)
                }.sortedByDescending {
                    dateFormat.parse(it.requestDate)?.time ?: 0L
                }

                _helpRequests.value = sortedRequests
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HelpRequestViewModel", "Failed to fetch help requests", error.toException())
            }
        })
    }

    fun updateCurrentOfficeId(officeId: String) {
        currentOfficeId = officeId
    }

    fun updateCurrentRequestId(requestId: String) {
        currentRequestId = requestId
    }

    fun saveRating(rating: Rating, callback: (Boolean) -> Unit) {
        firebaseService.saveRating(rating, callback)
    }

    fun getHelpRequestsByOfficeId(officeId: String): LiveData<List<HelpRequest>> {
        val helpRequestsLiveData = MutableLiveData<List<HelpRequest>>()
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = snapshot.children.mapNotNull { it.getValue(HelpRequest::class.java) }
                val filteredRequests = requests.filter { it.receiverId == officeId }
                helpRequestsLiveData.value = filteredRequests
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HelpRequestViewModel", "Failed to fetch help requests by office ID", error.toException())
            }
        })

        return helpRequestsLiveData
    }
}