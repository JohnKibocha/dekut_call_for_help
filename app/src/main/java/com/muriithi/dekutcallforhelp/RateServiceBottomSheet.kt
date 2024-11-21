// RateServiceBottomSheet.kt
package com.muriithi.dekutcallforhelp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.muriithi.dekutcallforhelp.beans.Rating
import com.muriithi.dekutcallforhelp.databases.FirebaseService
import com.muriithi.dekutcallforhelp.databinding.LayoutRateServiceBottomSheetBinding
import com.muriithi.dekutcallforhelp.viewmodels.HelpRequestViewModel
import com.muriithi.dekutcallforhelp.viewmodels.HelpRequestViewModelFactory
import java.util.UUID

class RateServiceBottomSheet : BottomSheetDialogFragment() {

    private var firebaseService = FirebaseService()
    private var _binding: LayoutRateServiceBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HelpRequestViewModel by viewModels {
        HelpRequestViewModelFactory(arguments?.getString(ARG_USER_ID))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = LayoutRateServiceBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val requestId = arguments?.getString(ARG_REQUEST_ID)
        val officeId = arguments?.getString(ARG_OFFICE_ID)

        binding.submitRatingButton.setOnClickListener {
            val officeRating = binding.rateOfficeRatingBar.rating
            val responseRating = binding.rateResponseRatingBar.rating
            val officeReview = binding.officeReview.text.toString()
            val responseReview = binding.responseReview.text.toString()

            if (officeRating == 0f || responseRating == 0f) {
                Toast.makeText(
                    context,
                    "Please provide ratings for both sections",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // check if the passed request id exists in the rating node and prevent the user from rating the same request twice
            firebaseService.getRatingById(requestId ?: "") { currentRating ->
                if (currentRating != null) {
                    Toast.makeText(
                        context,
                        "You have already rated this request",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@getRatingById
                }

                val rating = Rating(
                    ratingId = UUID.randomUUID().toString(),
                    userId = viewModel.currentUserId ?: "",
                    officeId = officeId ?: "",
                    requestId = requestId ?: "",
                    officeRating = officeRating,
                    requestRating = responseRating,
                    officeReview = officeReview,
                    requestReview = responseReview
                )

                viewModel.saveRating(rating) { success ->
                    if (success) {
                        Snackbar.make(view, "Rating saved successfully", Snackbar.LENGTH_SHORT)
                            .show()
                        dismiss()
                    } else {
                        Snackbar.make(view, "Failed to save rating", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_USER_ID = "userId"
        private const val ARG_REQUEST_ID = "requestId"
        private const val ARG_OFFICE_ID = "officeId"

        fun newInstance(
            userId: String,
            requestId: String,
            officeId: String
        ): RateServiceBottomSheet {
            val fragment = RateServiceBottomSheet()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            args.putString(ARG_REQUEST_ID, requestId)
            args.putString(ARG_OFFICE_ID, officeId)
            fragment.arguments = args
            return fragment
        }
    }
}

