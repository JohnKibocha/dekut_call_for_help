// HelpRequestViewModelFactory.kt
package com.muriithi.dekutcallforhelp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HelpRequestViewModelFactory(private val currentUserId: String?) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HelpRequestViewModel::class.java)) {
            return HelpRequestViewModel(currentUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}