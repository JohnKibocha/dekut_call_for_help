// HelpRequestAdapter.kt
package com.muriithi.dekutcallforhelp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.muriithi.dekutcallforhelp.R
import com.muriithi.dekutcallforhelp.beans.HelpRequest
import com.muriithi.dekutcallforhelp.beans.RequestResponse
import com.muriithi.dekutcallforhelp.beans.RequestStatus
import com.muriithi.dekutcallforhelp.components.Formatter
import com.muriithi.dekutcallforhelp.databases.FirebaseService
import com.muriithi.dekutcallforhelp.databinding.ItemHelpRequestBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HelpRequestAdapter(
    private val helpRequests: List<HelpRequest>,
    private val currentUserId: String
) : RecyclerView.Adapter<HelpRequestAdapter.HelpRequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpRequestViewHolder {
        val helpRequestBinding =
            ItemHelpRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HelpRequestViewHolder(helpRequestBinding)
    }

    override fun onBindViewHolder(holder: HelpRequestViewHolder, position: Int) {
        val helpRequest = helpRequests[position]
        holder.bind(helpRequest)
    }

    override fun getItemCount(): Int = helpRequests.size

    inner class HelpRequestViewHolder(private val helpRequestBinding: ItemHelpRequestBinding) :
        RecyclerView.ViewHolder(helpRequestBinding.root) {
        fun bind(helpRequest: HelpRequest) {

            val firebaseService = FirebaseService()
            val formatter = Formatter()

            helpRequestBinding.textViewClient.text = helpRequest.senderName
            helpRequestBinding.textViewDateTime.text = helpRequest.requestDate
            helpRequestBinding.textViewStatusValue.text = helpRequest.requestStatus.name
            helpRequestBinding.textViewResponseValue.text = helpRequest.requestResponse.name
            helpRequestBinding.textViewPhone.text =
                formatter.formatPhoneNumber(helpRequest.phoneNumber, helpRequest.countryCode)
            helpRequestBinding.textViewDescription.text = helpRequest.description

            // Fetch office name
            firebaseService.getOfficeNameById(helpRequest.officeId) { officeName ->
                helpRequestBinding.textViewOffice.text = officeName
            }

            helpRequestBinding.textViewAdmin.text = helpRequest.receiverName

            // Check if the user is a superuser
            val isSuperUser = helpRequest.receiverId == currentUserId

            // Set visibility based on user type
            helpRequestBinding.linearLayoutCancel.visibility =
                if (isSuperUser) View.VISIBLE else View.GONE
            helpRequestBinding.linearLayoutReview.visibility =
                if (isSuperUser) View.VISIBLE else View.GONE
            helpRequestBinding.linearLayoutSuspend.visibility =
                if (isSuperUser) View.VISIBLE else View.GONE

            helpRequestBinding.linearLayoutResolve.visibility =
                if (isSuperUser) View.GONE else View.VISIBLE
            helpRequestBinding.linearLayoutRateOffice.visibility =
                if (isSuperUser) View.GONE else View.VISIBLE

            // Set response icon and color
            when (helpRequest.requestResponse) {
                RequestResponse.PENDING -> {
                    helpRequestBinding.imageViewDisplayResponseIcon.setImageResource(R.drawable.ic_outlined_snooze)
                    helpRequestBinding.imageViewDisplayResponseIcon.setColorFilter(
                        ContextCompat.getColor(
                            helpRequestBinding.root.context,
                            R.color.material_orange_darker
                        )
                    )
                }

                RequestResponse.ACCEPTED -> {
                    helpRequestBinding.imageViewDisplayResponseIcon.setImageResource(R.drawable.ic_filled_received_call)
                    helpRequestBinding.imageViewDisplayResponseIcon.setColorFilter(
                        ContextCompat.getColor(
                            helpRequestBinding.root.context,
                            R.color.material_green_darker
                        )
                    )
                }

                RequestResponse.REJECTED -> {
                    helpRequestBinding.imageViewDisplayResponseIcon.setImageResource(R.drawable.ic_filled_missed_call)
                    helpRequestBinding.imageViewDisplayResponseIcon.setColorFilter(
                        ContextCompat.getColor(
                            helpRequestBinding.root.context,
                            R.color.material_red_darker
                        )
                    )
                }
            }

            // Set status icon and color
            when (helpRequest.requestStatus) {
                RequestStatus.PENDING -> {
                    helpRequestBinding.imageViewDisplayStatusIcon.setImageResource(R.drawable.ic_filled_history)
                    helpRequestBinding.imageViewDisplayStatusIcon.setColorFilter(
                        ContextCompat.getColor(
                            helpRequestBinding.root.context,
                            R.color.material_orange_darker
                        )
                    )
                }

                RequestStatus.SUSPENDED -> {
                    helpRequestBinding.imageViewDisplayStatusIcon.setImageResource(R.drawable.ic_filled_pause)
                    helpRequestBinding.imageViewDisplayStatusIcon.setColorFilter(
                        ContextCompat.getColor(
                            helpRequestBinding.root.context,
                            R.color.material_orange_darker
                        )
                    )
                }

                RequestStatus.UNDER_REVIEW -> {
                    helpRequestBinding.imageViewDisplayStatusIcon.setImageResource(R.drawable.ic_filled_thumb_up)
                    helpRequestBinding.imageViewDisplayStatusIcon.setColorFilter(
                        ContextCompat.getColor(
                            helpRequestBinding.root.context,
                            R.color.material_blue_darker
                        )
                    )
                }

                RequestStatus.RESOLVED -> {
                    helpRequestBinding.imageViewDisplayStatusIcon.setImageResource(R.drawable.ic_filled_checkmark)
                    helpRequestBinding.imageViewDisplayStatusIcon.setColorFilter(
                        ContextCompat.getColor(
                            helpRequestBinding.root.context,
                            R.color.material_green_darker
                        )
                    )
                }

                RequestStatus.TIMEOUT, RequestStatus.CANCELLED -> {
                    helpRequestBinding.imageViewDisplayStatusIcon.setImageResource(R.drawable.ic_filled_close)
                    helpRequestBinding.imageViewDisplayStatusIcon.setColorFilter(
                        ContextCompat.getColor(
                            helpRequestBinding.root.context,
                            R.color.material_red_darker
                        )
                    )
                }
            }

            // Set click listeners for action buttons
            helpRequestBinding.linearLayoutCancel.setOnClickListener {
                updateRequest(helpRequest, RequestResponse.REJECTED, RequestStatus.CANCELLED)
            }

            helpRequestBinding.linearLayoutResolve.setOnClickListener {
                val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())
                val requestDate = dateFormat.parse(helpRequest.requestDate)
                val responseTime = dateFormat.parse(helpRequest.responseTime)
                val resolutionTime = dateFormat.parse(dateFormat.format(Date()))

                if (requestDate != null && responseTime != null && resolutionTime != null) {
                    val processingTimeMillis =
                        ((responseTime.time - requestDate.time) + (resolutionTime.time - requestDate.time)) / 2
                    val processingTime = processingTimeMillis.toString()

                    updateRequest(
                        helpRequest.copy(processingTime = processingTime),
                        RequestResponse.ACCEPTED,
                        RequestStatus.RESOLVED
                    )
                }
            }

            helpRequestBinding.linearLayoutReview.setOnClickListener {
                updateRequest(helpRequest, RequestResponse.ACCEPTED, RequestStatus.UNDER_REVIEW)
            }

            helpRequestBinding.linearLayoutSuspend.setOnClickListener {
                updateRequest(helpRequest, RequestResponse.ACCEPTED, RequestStatus.SUSPENDED)
            }

            helpRequestBinding.linearLayoutRateOffice.setOnClickListener {
                // TODO: Implement Rating Feature Later
            }

            // Expand/Collapse functionality
            helpRequestBinding.root.setOnClickListener {
                val isVisible =
                    helpRequestBinding.linearLayoutExpandedActions.visibility == View.VISIBLE
                helpRequestBinding.linearLayoutExpandedActions.visibility =
                    if (isVisible) View.GONE else View.VISIBLE
                helpRequestBinding.linearLayoutOffice.visibility =
                    if (isVisible) View.GONE else View.VISIBLE
                helpRequestBinding.linearLayoutAdmin.visibility =
                    if (isVisible) View.GONE else View.VISIBLE
                helpRequestBinding.linearLayoutPhone.visibility =
                    if (isVisible) View.GONE else View.VISIBLE
                helpRequestBinding.linearLayoutDescription.visibility =
                    if (isVisible) View.GONE else View.VISIBLE
                helpRequestBinding.imageViewOfficeAdminCircle.visibility =
                    if (isVisible) View.GONE else View.VISIBLE
                helpRequestBinding.divider.visibility = if (isVisible) View.GONE else View.VISIBLE
            }

            // Set initial visibility to collapsed
            helpRequestBinding.linearLayoutExpandedActions.visibility = View.GONE
            helpRequestBinding.linearLayoutOffice.visibility = View.GONE
            helpRequestBinding.linearLayoutAdmin.visibility = View.GONE
            helpRequestBinding.linearLayoutPhone.visibility = View.GONE
            helpRequestBinding.linearLayoutDescription.visibility = View.GONE
            helpRequestBinding.imageViewOfficeAdminCircle.visibility = View.GONE
            helpRequestBinding.divider.visibility = View.GONE
        }

        private fun updateRequest(
            helpRequest: HelpRequest,
            response: RequestResponse,
            status: RequestStatus
        ) {
            val database = FirebaseDatabase.getInstance().getReference("helpRequests")
            val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())
            val currentTime = dateFormat.format(Date())

            val updatedRequest = helpRequest.copy(
                requestResponse = response,
                requestStatus = status,
                resolutionTime = if (status == RequestStatus.RESOLVED || status == RequestStatus.CANCELLED) currentTime else helpRequest.resolutionTime,
                responseTime = if (status == RequestStatus.UNDER_REVIEW || status == RequestStatus.SUSPENDED) currentTime else helpRequest.responseTime
            )

            database.child(helpRequest.requestId).setValue(updatedRequest)
        }
    }
}