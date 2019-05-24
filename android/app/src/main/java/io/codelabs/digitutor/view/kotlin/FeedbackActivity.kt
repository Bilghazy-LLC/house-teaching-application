package io.codelabs.digitutor.view.kotlin

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.data.BaseUser
import io.codelabs.digitutor.data.model.Feedback
import io.codelabs.digitutor.data.model.Tutor
import io.codelabs.digitutor.databinding.ActivityFeedbackBinding

class FeedbackActivity : BaseActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_feedback)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        snackbar = Snackbar.make(binding.container, "Please wait while we fetch your data", Snackbar.LENGTH_INDEFINITE)

        if (intent.hasExtra(EXTRA_FEEDBACK)) {
            binding.feedback = intent.getParcelableExtra(EXTRA_FEEDBACK)
            getUserInfo((binding.feedback as Feedback).tutor)
        } else if (intent.hasExtra(EXTRA_FEEDBACK_ID)) {

            FirebaseDataSource.getFeedback(
                this,
                firestore,
                intent.getStringExtra(EXTRA_FEEDBACK_ID),
                object : AsyncCallback<Feedback?> {
                    override fun onSuccess(response: Feedback?) {
                        if (response != null) {
                            binding.feedback = response
                            getUserInfo(response.tutor)
                        }
                    }

                    override fun onComplete() {
                        // do nothing
                    }

                    override fun onError(error: String?) {
                        snackbar.setText(error ?: "Error occurred")
                            .setDuration(BaseTransientBottomBar.LENGTH_LONG)
                            .show()
                    }

                    override fun onStart() {
                        snackbar.show()
                    }

                })
        }

    }

    private fun getUserInfo(tutor: String) {
        FirebaseDataSource.getUser(
            this@FeedbackActivity,
            firestore,
            tutor,
            BaseUser.Type.TUTOR,
            object : AsyncCallback<BaseUser?> {
                override fun onSuccess(response: BaseUser?) {
                    if (response != null) {
                        binding.tutor = response as? Tutor
                    }
                }

                override fun onComplete() {
                    if (snackbar.isShown) snackbar.dismiss()
                }

                override fun onError(error: String?) {
                    snackbar.setText(error ?: "Error occurred")
                        .setDuration(BaseTransientBottomBar.LENGTH_LONG)
                        .show()
                }

                override fun onStart() {
                    if (!snackbar.isShown) snackbar.show()
                }
            })
    }

    companion object {
        const val EXTRA_FEEDBACK_ID = "feedback_id"
        const val EXTRA_FEEDBACK = "feedback"
    }

    fun returnUser(view: View) = onBackPressed()

}