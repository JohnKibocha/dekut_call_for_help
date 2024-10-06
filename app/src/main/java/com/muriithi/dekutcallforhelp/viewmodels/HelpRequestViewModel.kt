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
import java.text.SimpleDateFormat
import java.util.Locale

class HelpRequestViewModel(private val currentUserId: String?) : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("helpRequests")
    private val _helpRequests = MutableLiveData<List<HelpRequest>>()
    val helpRequests: LiveData<List<HelpRequest>> get() = _helpRequests

    init {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = snapshot.children.mapNotNull { it.getValue(HelpRequest::class.java) }
                val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())

                val sortedRequests = requests.filter {
                   currentUserId !=null && (it.senderId == currentUserId || it.receiverId == currentUserId)
                }.sortedByDescending {
                    dateFormat.parse(it.requestDate)
                }

                _helpRequests.value = sortedRequests
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HelpRequestViewModel", "Error fetching help requests", error.toException())
            }
        })
    }
}