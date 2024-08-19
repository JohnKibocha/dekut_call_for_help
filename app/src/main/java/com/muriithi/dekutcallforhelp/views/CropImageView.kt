package com.muriithi.dekutcallforhelp.views

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.canhub.cropper.CropImageView
import com.muriithi.dekutcallforhelp.R

class CropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val cropImageView: CropImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_crop_image, this, true)
        cropImageView = findViewById(R.id.image_view_cropped_image)
    }

    fun setImageUri(uri: Uri) {
        cropImageView.setImageUriAsync(uri)
    }

    fun setImageBitmap(bitmap: Bitmap) {
        cropImageView.setImageBitmap(bitmap)
    }

    fun getCroppedImage(): Bitmap? {
        return cropImageView.getCroppedImage()
    }

    fun getCroppedImageAsync() {
        val cropped: Bitmap? = cropImageView.getCroppedImage()
    }

    fun setOnCropImageCompleteListener(listener: CropImageView.OnCropImageCompleteListener) {
        cropImageView.setOnCropImageCompleteListener(listener)
    }
}