package com.muriithi.dekutcallforhelp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.muriithi.dekutcallforhelp.databases.FirebaseService

class AdminHomeFragment : Fragment() {

    private lateinit var firebaseService: FirebaseService
    private lateinit var dashboard: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)
        dashboard = view.findViewById(R.id.dashboard)
        firebaseService = FirebaseService()

        // Add the current user profile image to the appbar
        val topAppBar =
            view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.admin_top_app_bar)
        val profileImageView = topAppBar.findViewById<ImageView>(R.id.profile_image)
        val currentUser = firebaseService.getCurrentUser()

        // Get the profile image from user database and if exists set it to the appbar
        currentUser?.profilePhoto?.let { profilePhotoUrl ->
            Glide.with(this)
                .load(profilePhotoUrl)
                .placeholder(R.drawable.ic_filled_account)
                .into(profileImageView)
        }

        // When the user presses the native back button, log them out and close the app
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            firebaseService.signOut()
            requireActivity().finish()
        }

        // Populate the dashboard
        populateDashboard()
        return view
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_top_app_bar, menu)
        val profileMenuItem = menu.findItem(R.id.action_profile)
        val profileImageView = profileMenuItem.actionView as ImageView

        // Load the current user's profile image
        val currentUser = firebaseService.getCurrentUser()
        currentUser?.profilePhoto?.let { profilePhotoUrl ->
            Glide.with(this)
                .load(profilePhotoUrl)
                .placeholder(R.drawable.ic_filled_account)
                .into(profileImageView)
        }
    }

    private fun populateDashboard() {
        // Populate the dashboard with dummy data
        val emergencyHandledCard =
            dashboard.findViewById<MaterialCardView>(R.id.card_emergency_handled)
        val requestsRejectedCard =
            dashboard.findViewById<MaterialCardView>(R.id.card_requests_rejected)
        val topOfficesCard = dashboard.findViewById<MaterialCardView>(R.id.card_top_offices)
        val topOfficesRatingCard =
            dashboard.findViewById<MaterialCardView>(R.id.card_top_offices_rating)
        val recentRequestsList = dashboard.findViewById<ViewGroup>(R.id.list_recent_requests)

        // Set dummy data
        emergencyHandledCard.findViewById<MaterialTextView>(R.id.emergency_handled_metric).text =
            "10"
        requestsRejectedCard.findViewById<MaterialTextView>(R.id.emergency_rejected_metric).text =
            "2"
        topOfficesCard.findViewById<MaterialTextView>(R.id.top_office_metric).text =
            "Medical Office"
        topOfficesRatingCard.findViewById<MaterialTextView>(R.id.office_rating_metric).text = "4.5"
        topOfficesRatingCard.findViewById<MaterialTextView>(R.id.top_rated_office).text =
            "Security Office"

        // Populate recent requests list with dummy data
        for (i in 1..5) {
            val requestView = LayoutInflater.from(context)
                .inflate(R.layout.item_recent_request, recentRequestsList, false)
            requestView.findViewById<MaterialTextView>(R.id.client).text = "User $i"
            requestView.findViewById<MaterialTextView>(R.id.date_time).text = "2023-10-01 12:00"

            val responseIcon = requestView.findViewById<ImageView>(R.id.display_response_icon)
            val statusIcon = requestView.findViewById<ImageView>(R.id.display_status_icon)

            if (i % 2 == 0) {
                requestView.findViewById<MaterialTextView>(R.id.response_value).text = "Accepted"
                requestView.findViewById<MaterialTextView>(R.id.status_value).text = "Resolved"

                responseIcon.setImageResource(R.drawable.ic_filled_received_call)
                responseIcon.setColorFilter(Color.parseColor("#2E7D32")) // Green
                statusIcon.setImageResource(R.drawable.ic_filled_checkmark)
                statusIcon.setColorFilter(Color.parseColor("#2E7D32")) // Green
            } else {
                requestView.findViewById<MaterialTextView>(R.id.response_value).text = "Cancelled"
                requestView.findViewById<MaterialTextView>(R.id.status_value).text = "Rejected"
                responseIcon.setImageResource(R.drawable.ic_filled_missed_call)
                responseIcon.setColorFilter(Color.parseColor("#C62828")) // Dark Red
                statusIcon.setImageResource(R.drawable.ic_filled_close)
                statusIcon.setColorFilter(Color.parseColor("#C62828")) // Dark Red
            }

            recentRequestsList.addView(requestView)
        }
    }
}