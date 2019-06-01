package io.codelabs.digitutor.view.kotlin

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.data.model.Subject
import io.codelabs.digitutor.data.model.Tutor
import io.codelabs.digitutor.databinding.ActivityTutorsBinding
import io.codelabs.digitutor.view.UserActivity
import io.codelabs.digitutor.view.adapter.UsersAdapter
import io.codelabs.recyclerview.GridItemDividerDecoration
import io.codelabs.recyclerview.SlideInItemAnimator
import io.codelabs.sdk.util.debugLog

class TutorsActivity : BaseActivity() {
    private lateinit var binding: ActivityTutorsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutors)

        val adapter = UsersAdapter(applicationContext) { item, _ ->
            intentTo(UserActivity::class.java, Bundle().apply {
                putParcelable(UserActivity.EXTRA_USER, item)
                putString(UserActivity.EXTRA_USER_TYPE, item.type)
                putString(UserActivity.EXTRA_USER_UID, item.key)
            }, false)
        }

        binding.grid.adapter = adapter
        binding.grid.setHasFixedSize(true)
        binding.grid.itemAnimator = SlideInItemAnimator()
        binding.grid.addItemDecoration(GridItemDividerDecoration(this, R.dimen.divider_height, R.color.divider))
        binding.grid.layoutManager = LinearLayoutManager(this)

        FirebaseDataSource.getAllTutors(this, firestore, prefs, object : AsyncCallback<MutableList<Tutor>?> {
            override fun onSuccess(response: MutableList<Tutor>?) {
                response?.forEach {
                    FirebaseDataSource.getTutorSubjects(
                        this@TutorsActivity,
                        firestore,
                        it.key,
                        object : AsyncCallback<MutableList<Subject>?> {
                            override fun onSuccess(subjRes: MutableList<Subject>?) {
                                val filter =
                                    subjRes?.filter { subject -> subject.name == intent.getStringExtra(SUBJECT_QUERY) }

                                if (filter != null && filter.isNotEmpty()) adapter.addData(response)
                            }

                            override fun onError(error: String?) {
                                debugLog(error)
                            }
                        })
                }
            }

            override fun onError(error: String?) {
                debugLog(error)
            }
        })
    }

    companion object {
        const val SUBJECT_QUERY = "SUBJECT_QUERY"
    }

}