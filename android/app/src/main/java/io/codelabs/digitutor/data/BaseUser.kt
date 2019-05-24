package io.codelabs.digitutor.data

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import io.codelabs.digitutor.R
import io.codelabs.sdk.glide.GlideApp

/**
 * Base class for all user data models
 */
interface BaseUser : BaseDataModel {
    var email: String?
    var name: String?
    var avatar: String?
    var type: String
    var token: String?

    /**
     * Stores the user's type
     */
    object Type {
        const val PARENT = "parent"
        const val TUTOR = "tutor"
        const val WARD = "ward"
    }

    /*companion object {
        @BindingAdapter("imageUrl", "error")
        fun loadAvatar(imageView: ImageView, imageUrl: String, error: Drawable) {
            GlideApp.with(imageView.context)
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(error)
                    .into(imageView)
        }
    }*/
}