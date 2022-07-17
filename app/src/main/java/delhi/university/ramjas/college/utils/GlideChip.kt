package delhi.university.ramjas.college.utils

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.material.chip.Chip
import com.bumptech.glide.request.target.Target



class GlideChip(context:Context) : Chip(context) {


    /**
     * Set an image from an URL for the [Chip] using [com.bumptech.glide.Glide]
     * @param url icon URL
     * @param errDrawable error icon if Glide return failed response
     */
    fun setIconUrl(url: String?, errDrawable: Drawable?): GlideChip {
        Glide.with(this)
            .load(url)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    chipIcon = errDrawable
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    chipIcon = resource
                    return false
                }
            }).preload()
        return this
    }
}