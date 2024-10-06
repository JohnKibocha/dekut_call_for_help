// app/src/main/java/com/muriithi/dekutcallforhelp/components/ImageRetriever.kt
package com.muriithi.dekutcallforhelp.components

import android.app.Activity
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

sealed class Component {
    class FragmentComponent(val fragment: Fragment) : Component()
    class ActivityComponent(val activity: Activity) : Component()
}

class ImageRetriever(private val component: Component) {

    private var imageUriCallback: ((Uri?) -> Unit)? = null

    private val getImage = when (component) {
        is Component.FragmentComponent -> (component.fragment as ActivityResultCaller).registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            imageUriCallback?.invoke(uri)
        }

        is Component.ActivityComponent -> (component.activity as ActivityResultCaller).registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            imageUriCallback?.invoke(uri)
        }
    }

    fun retrieveImage(callback: (Uri?) -> Unit) {
        imageUriCallback = callback
        getImage.launch("image/*")
    }
}