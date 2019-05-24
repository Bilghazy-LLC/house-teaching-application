package io.codelabs.digitutor.view.kotlin

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.core.util.InputValidator
import io.codelabs.digitutor.data.BaseUser
import io.codelabs.digitutor.data.model.Complaint
import io.codelabs.digitutor.databinding.ActivityMakeComplaintBinding
import io.codelabs.sdk.util.debugLog
import io.codelabs.sdk.util.toast

/**
 * Make a new [Complaint]
 */
class MakeComplaintActivity : BaseActivity() {
    private lateinit var binding: ActivityMakeComplaintBinding

    companion object {
        const val EXTRA_TUTOR = "tutor"
        const val EXTRA_TUTOR_ID = "tutor_id"
    }

    private var tutorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_make_complaint)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        debugLog("Tutor for complaint: ${intent.getParcelableExtra<BaseUser>(EXTRA_TUTOR)}")
        debugLog("Tutor ID for complaint: ${intent.getStringExtra(EXTRA_TUTOR_ID)}")

        if (intent.hasExtra(EXTRA_TUTOR)) {
            tutorId = intent.getParcelableExtra<BaseUser>(EXTRA_TUTOR).key
        } else if (intent.hasExtra(EXTRA_TUTOR_ID)) tutorId = intent.getStringExtra(EXTRA_TUTOR_ID)

        debugLog("TutorID for complaint: $tutorId")
    }

    fun send(view: View) {
        val message = binding.complaint.text.toString()
        if (InputValidator.hasValidInput(message)) {
            FirebaseDataSource.postComplaint(
                this,
                prefs.key,
                tutorId,
                message,
                object : AsyncCallback<Void?> {
                    override fun onSuccess(response: Void?) {
                        toast("Complaint sent to tutor successfully")
                        debugLog("Complaint sent successfully")
                    }

                    override fun onComplete() {
                        debugLog("Sending of complaint completed")
                    }

                    override fun onError(error: String?) {
                        debugLog(error)
                    }

                    override fun onStart() {
                        toast("Sending complaint...")
                        finishAfterTransition()
                    }
                })
        } else {
            toast("Enter a message first...")
        }
    }

}