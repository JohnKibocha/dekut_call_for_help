// app/src/main/java/com/muriithi/dekutcallforhelp/components/ImageUploader.kt
package com.muriithi.dekutcallforhelp.components

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

/**
 * A class that uploads an image to Firebase Storage
 */

class ImageUploader {

    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    fun uploadImage(uri: Uri, callback: (String?) -> Unit) {
        val fileReference = storageReference.child("images/${uri.lastPathSegment}")
        fileReference.putFile(uri)
            .addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    callback(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}