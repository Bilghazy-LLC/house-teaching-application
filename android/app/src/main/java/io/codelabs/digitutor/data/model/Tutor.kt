package io.codelabs.digitutor.data.model

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import com.google.firebase.iid.FirebaseInstanceId
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.util.Constants
import io.codelabs.digitutor.data.BaseUser
import io.codelabs.sdk.glide.GlideApp
import kotlinx.android.parcel.Parcelize

/**
 * [Tutor] data model class
 */
@Parcelize
data class Tutor(override var email: String?,
                 override var name: String?,
                 override var avatar: String?,
                 override var key: String,
                 override var token: String?,
                 var blocked: Boolean = false,
                 var rating: Double = 1.0,
                 override var type: String = BaseUser.Type.TUTOR) : BaseUser {
    constructor() : this("", "", "", "", FirebaseInstanceId.getInstance().token)

    companion object {
        @JvmStatic
        @BindingAdapter("imageUrl", "error")
        fun loadTutorAvatar(imageView: ImageView, @Nullable imageUrl: String?, error: Drawable) {
            GlideApp.with(imageView.context)
                    .load(imageUrl ?: Constants.DEFAULT_AVATAR_URL)
                    .circleCrop()
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(error)
                    .into(imageView)
        }
    }
}