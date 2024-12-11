// app/src/main/java/com/muriithi/dekutcallforhelp/OfficeFragment.kt
package com.muriithi.dekutcallforhelp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class OfficeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_office, container, false)
        return view
    }

}