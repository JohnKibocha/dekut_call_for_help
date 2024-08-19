// app/src/main/java/com/muriithi/dekutcallforhelp/components/ImageDownloader.kt
package com.muriithi.dekutcallforhelp.components

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * A class that downloads an image
 * @param context The context that will be used to download the image from the internet
 */

class ImageDownloader(private val context: Context) {

    fun downloadImage(url: String, imageView: ImageView) {
        Glide.with(context).load(url).into(imageView)
    }
}