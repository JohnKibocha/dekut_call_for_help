// app/src/main/java/com/muriithi/dekutcallforhelp/components/ImageParser.kt
package com.muriithi.dekutcallforhelp.components

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * A class that parses an image
 * @param context The context that will be used to parse the image
 */

class ImageParser(private val context: Context) {

    fun parseImage(uri: Uri, imageView: ImageView) {
        Glide.with(context).load(uri).into(imageView)
    }
}