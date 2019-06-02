package io.codelabs.digitutor.view

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.core.util.extractNames
import io.codelabs.digitutor.data.model.Subject
import io.codelabs.digitutor.databinding.ActivityAvailableBinding
import io.codelabs.sdk.util.debugLog
import io.codelabs.sdk.util.toast

class AvailableDaysActivity : BaseActivity() {

    private lateinit var binding: ActivityAvailableBinding
    private val subjects = mutableListOf<Subject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_available)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        setSupportActionBar(binding.toolbar)
        setupSpinners()
    }

    private fun setupSpinners() {
        val daysAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, resources.getStringArray(R.array.days))
        binding.avDow.adapter = daysAdapter
        val timeAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, resources.getStringArray(R.array.time))
        binding.avStart.adapter = timeAdapter
        binding.avEnd.adapter = timeAdapter

        FirebaseDataSource.getTutorSubjects(this, firestore, prefs.key, object : AsyncCallback<MutableList<Subject>?> {
            override fun onSuccess(response: MutableList<Subject>?) {
                if (response != null) {
                    subjects.addAll(response)
                    val subjectsAdapter =
                        ArrayAdapter(
                            this@AvailableDaysActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            response.extractNames()
                        )
                    binding.avSubject.adapter = subjectsAdapter
                    binding.loaded = true
                }
            }

            override fun onError(error: String?) {

            }
        })
    }

    fun save(view: View) {
        if (binding.loaded as Boolean) {
            val day = binding.avDow.selectedItem.toString()
            val start = binding.avStart.selectedItem.toString()
            val end = binding.avEnd.selectedItem.toString()
            var subject = ""
            subjects.forEach {
                if (it.name == binding.avSubject.selectedItem.toString()) subject = it.key
            }

            toast("Adding schedule...")
            FirebaseDataSource.postSchedule(
                this,
                "",
                subject,
                start,
                end,
                day,
                prefs.key,
                object : AsyncCallback<Void?> {
                    override fun onSuccess(response: Void?) {
                        toast("Schedule added successfully...")
                    }

                    override fun onError(error: String?) {
                        try {
                            toast(error, true)
                        } catch (e: Exception) {
                            debugLog(e.localizedMessage)
                        }
                    }
                })
            finishAfterTransition()
        }
    }

}
