// OfficeFragment.kt
package com.muriithi.dekutcallforhelp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muriithi.dekutcallforhelp.adapters.OfficeAdapter
import com.muriithi.dekutcallforhelp.adapters.OfficeData
import com.muriithi.dekutcallforhelp.beans.HelpRequest
import com.muriithi.dekutcallforhelp.beans.Office
import com.muriithi.dekutcallforhelp.beans.OfficeDetail
import com.muriithi.dekutcallforhelp.beans.Rating
import com.muriithi.dekutcallforhelp.beans.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OfficeFragment : Fragment() {

    private val firebaseDatabase = FirebaseDatabase.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUserId = firebaseAuth.currentUser?.uid
    private lateinit var recyclerView: RecyclerView
    private lateinit var officeAdapter: OfficeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_office, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: MaterialToolbar = view.findViewById(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        recyclerView = view.findViewById(R.id.recycler_view_offices)
        recyclerView.layoutManager = LinearLayoutManager(context)
        officeAdapter = OfficeAdapter()
        recyclerView.adapter = officeAdapter

        fetchAndDisplayOfficeData()
    }

    private fun fetchAndDisplayOfficeData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val helpRequests = getAllHelpRequests()
                val offices = getAllOffices()
                val user = getCurrentUser()

                Log.d("OfficeFragment", "Fetched helpRequests: $helpRequests")
                Log.d("OfficeFragment", "Fetched offices: $offices")
                Log.d("OfficeFragment", "Fetched user: $user")

                val officeData = offices.map { office ->
                    val filteredRequests = helpRequests.filter { it.officeId == office.officeId }
                    val officeDetails = filteredRequests.map { request ->
                        val rating = getRatingByRequestId(request.requestId) ?: Rating()
                        OfficeDetail(
                            visitDate = request.requestDate,
                            officer = request.receiverName,
                            rating = rating.requestRating.toDouble(),
                            status = request.requestStatus,
                            senderId = request.senderId
                        )
                    }
                    OfficeData(office.officeName, officeDetails)
                }

                withContext(Dispatchers.Main) {
                    officeAdapter.submitList(officeData)
                }
            } catch (e: Exception) {
                Log.e("OfficeFragment", "Error fetching office data: ${e.message}")
            }
        }
    }

    private suspend fun getAllHelpRequests(): List<HelpRequest> {
        val snapshot = firebaseDatabase.child("helpRequests").get().await()
        return snapshot.children.mapNotNull { it.getValue(HelpRequest::class.java) }
    }

    private suspend fun getAllOffices(): List<Office> {
        val snapshot = firebaseDatabase.child("offices").get().await()
        return snapshot.children.mapNotNull { it.getValue(Office::class.java) }
    }

    private suspend fun getCurrentUser(): User? {
        val snapshot = firebaseDatabase.child("users").child(currentUserId!!).get().await()
        return snapshot.getValue(User::class.java)
    }

    private suspend fun getRatingByRequestId(requestId: String): Rating? {
        val snapshot =
            firebaseDatabase.child("ratings").orderByChild("requestId").equalTo(requestId).get()
                .await()
        return snapshot.children.mapNotNull { it.getValue(Rating::class.java) }.firstOrNull()
    }
}