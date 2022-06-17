package com.devkeyapps.myramjas.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.ByteArrayOutputStream

fun Uri.toBitmap(context: Context): Bitmap {
    return when {
        Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
            context.contentResolver,
            this
        )
        else -> {
            val source = ImageDecoder.createSource(context.contentResolver, this)
            ImageDecoder.decodeBitmap(source)
        }
    }
}

fun Bitmap.toUri(context: Context): Uri {
    val bytes = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(
        context.contentResolver,
        this,
        Math.random().toString(),
        null
    )
    return Uri.parse(path.toString())
}

fun Bitmap.compressBitmap(quality: Int): Bitmap {
    // Initialize a new ByteArrayStream
    val stream = ByteArrayOutputStream()

    // Compress the bitmap with JPEG format and quality 50%
    this.compress(Bitmap.CompressFormat.JPEG, quality, stream)

    val byteArray = stream.toByteArray()

    // Finally, return the compressed bitmap
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}
