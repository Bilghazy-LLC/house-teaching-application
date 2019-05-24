package io.codelabs.digitutor.data.model

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import com.afollestad.materialdialogs.bottomsheets.GridItem
import com.google.firebase.iid.FirebaseInstanceId
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.util.Constants
import io.codelabs.digitutor.data.BaseUser
import io.codelabs.sdk.glide.GlideApp
import kotlinx.android.parcel.Parcelize


/**
 * [Ward] data model class
 */
@Parcelize
data class Ward(
    override var email: String?,
    override var name: String?,
    override var avatar: String?,
    override var key: String,
    override var token: String?,
    override val title: String = name ?: "",
    override var type: String = BaseUser.Type.WARD
) : BaseUser, GridItem {

    override fun populateIcon(imageView: ImageView) {
        imageView.setImageDrawable(null)
    }

    constructor() : this("", "", "", "",  FirebaseInstanceId.getInstance().token)

    companion object {
        @JvmStatic
        @BindingAdapter("imageUrl", "error")
        fun loadWardAvatar(imageView: ImageView, @Nullable imageUrl: String?, error: Drawable) {
            GlideApp.with(imageView.context)
                .load(imageUrl ?: Constants.DEFAULT_AVATAR_URL)
                .circleCrop()
                .placeholder(R.drawable.avatar_placeholder)
                .error(error)
                .into(imageView)
        }
    }
}