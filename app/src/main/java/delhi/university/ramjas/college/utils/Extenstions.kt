package delhi.university.ramjas.college.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import delhi.university.ramjas.college.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

fun View.snack(message :String,context: Context){
    val snackBarView = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
    val view = snackBarView.view
    val params = view.layoutParams as FrameLayout.LayoutParams
    params.topMargin = dpToPx(75,context)
    params.gravity = Gravity.TOP
    view.layoutParams = params
    //view.background = ContextCompat.getDrawable(context,R.drawable.custom_drawable) // for custom background
    snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    snackBarView.show()

}

fun dpToPx(dp: Int, context: Context): Int {
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

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

//load image in ImageView from URL
fun ImageView.loadUrl(url: String) {
    Glide.with(context)
        .load(url)
        .placeholder(R.drawable.card_gradient)
        .into(this)
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
