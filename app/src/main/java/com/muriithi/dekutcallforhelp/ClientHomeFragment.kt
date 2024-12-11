package com.muriithi.dekutcallforhelp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.muriithi.dekutcallforhelp.beans.HelpRequest
import com.muriithi.dekutcallforhelp.beans.Office
import com.muriithi.dekutcallforhelp.beans.officeType
import com.muriithi.dekutcallforhelp.components.Formatter
import com.muriithi.dekutcallforhelp.components.OneSignalNotificationManager
import com.muriithi.dekutcallforhelp.components.Validator
import com.muriithi.dekutcallforhelp.databases.FirebaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class ClientHomeFragment : Fragment() {

    private lateinit var callButton: MaterialButton
    private val oneSignalNotifications = OneSignalNotificationManager()
    private val firebaseService = FirebaseService()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val formatter = Formatter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_home, container, false)
        callButton = view.findViewById(R.id.button_call)

        // Initialize the progress indicator
        val progressIndicator: LinearProgressIndicator = view.findViewById(R.id.progress_indicator)
        progressIndicator.visibility = View.GONE
        callButton.setOnClickListener {
            showHelpRequestForm()
        }

        // Add the current user profile image to the appbar
        val topAppBar =
            view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.client_top_app_bar)
        val profileImageView = topAppBar.findViewById<ImageView>(R.id.profile_image)
        val currentUser = firebaseService.getCurrentUser()

        // Get the profile image from user database and if exists set it to the appbar
        currentUser?.profilePhoto?.let { profilePhotoUrl ->
            Glide.with(this)
                .load(profilePhotoUrl)
                .placeholder(R.drawable.ic_filled_account)
                .into(profileImageView)
            Log.d("ClientHomeFragment", "Profile image loaded successfully")
        }

        // When the user presses the native back button, log them out and close the app
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            firebaseService.signOut()
            requireActivity().finish()
        }

        return view
    }

    private fun showHelpRequestForm() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_help_request, null)

        val officeAutoCompleteTextView: AutoCompleteTextView =
            dialogView.findViewById<TextInputLayout>(R.id.office_spinner)
                .editText as AutoCompleteTextView
        val descriptionEditText: EditText =
            dialogView.findViewById<TextInputLayout>(R.id.description_edit_text)
                .editText as EditText

        // Dropdown office options
        val officeNames = arrayOf(
            "General Office", "Security Office", "Medical Office", "Student Welfare Office"
        )
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, officeNames
        )
        officeAutoCompleteTextView.setAdapter(adapter)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Request Help")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val selectedOffice = officeAutoCompleteTextView.text.toString()
                val description = descriptionEditText.text.toString()

                val validator = Validator()
                var isValid = true

                // Validate office field
                try {
                    validator.validateField(selectedOffice)
                } catch (e: IllegalArgumentException) {
                    dialogView.findViewById<TextInputLayout>(R.id.office_spinner)
                        .error = "Office selection is required"
                    isValid = false
                }

                // Validate description field
                try {
                    validator.validateField(description)
                } catch (e: IllegalArgumentException) {
                    dialogView.findViewById<TextInputLayout>(R.id.description_edit_text)
                        .error = "Description is required"
                    isValid = false
                }

                // If validation fails, don't proceed with sending the request
                if (!isValid) {
                    val snackbar = Snackbar.make(
                        requireView(), "Please fill in all required fields", Snackbar.LENGTH_SHORT
                    )
                    snackbar.show()
                    return@setPositiveButton
                }

                // Enum mapping for office type
                val officeType = when (selectedOffice) {
                    "General Office" -> officeType.GENERAL_OFFICE
                    "Security Office" -> officeType.SECURITY_OFFICE
                    "Medical Office" -> officeType.MEDICAL_OFFICE
                    "Student Welfare Office" -> officeType.STUDENT_WELFARE_OFFICE
                    else -> officeType.GENERAL_OFFICE
                }

                // Call function to handle sending the request
                sendHelpRequest(selectedOffice, officeType, description)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun sendHelpRequest(officeName: String, officeType: officeType, description: String) {
        val currentUser = firebaseAuth.currentUser
        val senderId = currentUser?.uid ?: return

        val progressIndicator: LinearProgressIndicator =
            view?.findViewById(R.id.progress_indicator) ?: return
        progressIndicator.visibility = View.VISIBLE

        firebaseService.getAllUsers { users ->
            val receiver = users?.find { it.superuser } ?: return@getAllUsers
            val receiverId = receiver.userId ?: return@getAllUsers
            val sender = users.find { it.userId == senderId } ?: return@getAllUsers

            val phoneNumber = sender.phoneNumber ?: ""
            val senderName = currentUser.displayName ?: ""
            val receiverName = receiver.firstName + " " + receiver.lastName

            var snackBar =
                Snackbar.make(
                    requireView(),
                    "Sending help request...",
                    Snackbar.LENGTH_INDEFINITE
                )
            snackBar.show()
            // Log the sender object with all its properties
            Log.d(
                "ClientHomeFragment",
                "Sender: ${sender.userId}, Name: ${senderName}, Phone: ${sender.phoneNumber}"
            )

            val helpRequest = HelpRequest(
                requestId = UUID.randomUUID().toString(),
                senderId = senderId,
                senderName = formatter.formatName(senderName),
                receiverName = formatter.formatName(receiverName),
                receiverId = receiverId,
                requestDate = formatter.formatDateToString(Date()),
                officeId = "", // Initially blank, will be updated later
                description = formatter.formatName(description),
                phoneNumber = formatter.stripPhoneNumberFormatting(phoneNumber),
                countryCode = sender.countryCode,
                email = formatter.formatEmail(currentUser.email ?: ""),
                responseTime = "",
                processingTime = "",
                resolutionTime = ""
            )

            firebaseService.getOfficeByType(officeType.toString()) { existingOffice ->
                val office = existingOffice ?: Office(
                    officeId = UUID.randomUUID().toString(),
                    officeName = officeName,
                    officeType = officeType,
                    createdOn = formatter.formatDateToString(Date()),
                    requestId = "" // Initially blank, will be updated later
                )

                if (existingOffice == null) {
                    firebaseService.createOffice(office) { officeSuccess ->
                        if (officeSuccess) {
                            Log.d("ClientHomeFragment", "Office created successfully")
                        } else {
                            Toast.makeText(
                                context, "Failed to create office", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Log.d("ClientHomeFragment", "Office already exists")
                }

                // Update help request with the office ID
                helpRequest.officeId = office.officeId

                firebaseService.createHelpRequest(helpRequest) { helpRequestSuccess ->
                    if (helpRequestSuccess) {
                        // Update office with the request ID
                        office.requestId = helpRequest.requestId
                        firebaseService.updateOffice(office) { officeUpdateSuccess ->
                            if (officeUpdateSuccess) {
                                Log.d(
                                    "ClientHomeFragment",
                                    "Help request sent and office updated successfully"
                                )
                                // Send Notification to the Admin
                                CoroutineScope(Dispatchers.IO).launch {
                                    oneSignalNotifications.sendNotification(
                                        receiverId,
                                        "New Help Request",
                                        "You have a new help request from $senderName: " +
                                                "$description. Tap and navigate to Requests to Respond."
                                    )
                                }
                                snackBar.dismiss()
                                progressIndicator.visibility = View.GONE
                                snackBar = Snackbar.make(
                                    requireView(),
                                    "Help request sent successfully",
                                    Snackbar.LENGTH_SHORT
                                )
                                snackBar.show()
                            } else {
                                Log.e(
                                    "ClientHomeFragment",
                                    "Failed to update office with request ID"
                                )
                            }
                        }
                    } else {
                        Log.e("ClientHomeFragment", "Failed to create help request")
                        snackBar = Snackbar.make(
                            requireView(), "Failed to send help request", Snackbar.LENGTH_SHORT
                        )
                        snackBar.show()
                    }
                }
            }
        }
    }
}