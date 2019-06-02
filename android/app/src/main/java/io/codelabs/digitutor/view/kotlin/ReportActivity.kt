package io.codelabs.digitutor.view.kotlin

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource.getSubject
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.data.BaseUser
import io.codelabs.digitutor.data.model.Report
import io.codelabs.digitutor.data.model.Subject
import io.codelabs.digitutor.data.model.Tutor
import io.codelabs.digitutor.databinding.ActivityReportBinding

class ReportActivity : BaseActivity() {
    private lateinit var binding: ActivityReportBinding
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_report)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        snackbar = Snackbar.make(binding.container, "Please wait while we fetch your data", Snackbar.LENGTH_INDEFINITE)

        if (intent.hasExtra(EXTRA_REPORT)) {
            binding.report = intent.getParcelableExtra(EXTRA_REPORT)
            getUserInfo((binding.report as Report).tutor)
        } else if (intent.hasExtra(EXTRA_REPORT_ID)) {

            FirebaseDataSource.getReport(
                this,
                firestore,
                intent.getStringExtra(EXTRA_REPORT_ID),
                object : AsyncCallback<Report?> {
                    override fun onSuccess(response: Report?) {
                        if (response != null) {
                            binding.report = response
                            getUserInfo(response.tutor)
                            getSubject(firestore, response.subject, object : AsyncCallback<Subject?> {
                                override fun onSuccess(response: Subject?) {
                                    if (response != null) {
                                        binding.subject = response
                                    }
                                }

                                override fun onError(error: String?) {

                                }
                            })
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
            this@ReportActivity,
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
        const val EXTRA_REPORT_ID = "REPORT_ID"
        const val EXTRA_REPORT = "REPORT"
    }

    fun returnUser(view: View) = onBackPressed()

}