package com.muriithi.dekutcallforhelp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.muriithi.dekutcallforhelp.adapters.HelpRequestAdapter
import com.muriithi.dekutcallforhelp.beans.RequestStatus
import com.muriithi.dekutcallforhelp.viewmodels.HelpRequestViewModel

class RequestFragment : Fragment() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUserId: String = firebaseAuth.currentUser?.uid ?: ""
    private val viewModel: HelpRequestViewModel by lazy {
        HelpRequestViewModel(currentUserId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pendingRecyclerView: RecyclerView =
            view.findViewById(R.id.recycler_view_pending_requests)
        val suspendedRecyclerView: RecyclerView =
            view.findViewById(R.id.recycler_view_suspended_requests)
        val reviewedRecyclerView: RecyclerView =
            view.findViewById(R.id.recycler_view_resolved_requests)

        val pendingRequestsLinearLayout: LinearLayout = view.findViewById(R.id.linear_layout_pending_requests)
        val suspendedRequestsLinearLayout: LinearLayout = view.findViewById(R.id.linear_layout_suspended_requests)
        val resolvedRequestsLinearLayout: LinearLayout = view.findViewById(R.id.linear_layout_resolved_requests)

        // Make the navigation icon in the MaterialToolbar return to the previous fragment on click
        val topAppBar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.help_request_top_app_bar)
        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        pendingRecyclerView.layoutManager = LinearLayoutManager(context)
        suspendedRecyclerView.layoutManager = LinearLayoutManager(context)
        reviewedRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.helpRequests.observe(viewLifecycleOwner, Observer { helpRequests ->
            val pendingRequests =
                helpRequests.filter { it.requestStatus == RequestStatus.PENDING || it.requestStatus == RequestStatus.UNDER_REVIEW }
            val suspendedRequests =
                helpRequests.filter { it.requestStatus == RequestStatus.SUSPENDED }
            val reviewedRequests =
                helpRequests.filter { it.requestStatus == RequestStatus.RESOLVED || it.requestStatus == RequestStatus.TIMEOUT || it.requestStatus == RequestStatus.CANCELLED }

            // if an adapter is empty hide the entire recycler view else show it
            if (pendingRequests.isEmpty()) {
                pendingRequestsLinearLayout.visibility = View.GONE
            } else {
                pendingRequestsLinearLayout.visibility = View.VISIBLE
            }

            if (suspendedRequests.isEmpty()) {
                suspendedRequestsLinearLayout.visibility = View.GONE
            } else {
                suspendedRequestsLinearLayout.visibility = View.VISIBLE
            }

            if (reviewedRequests.isEmpty()) {
                resolvedRequestsLinearLayout.visibility = View.GONE
            } else {
                resolvedRequestsLinearLayout.visibility = View.VISIBLE
            }

            pendingRecyclerView.adapter = HelpRequestAdapter(pendingRequests, currentUserId ?: "")
            suspendedRecyclerView.adapter = HelpRequestAdapter(suspendedRequests, currentUserId ?: "")
            reviewedRecyclerView.adapter = HelpRequestAdapter(reviewedRequests, currentUserId ?: "")
        })
    }
}