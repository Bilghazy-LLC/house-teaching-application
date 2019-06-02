package io.codelabs.digitutor.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.data.BaseUser
import io.codelabs.digitutor.data.model.Parent
import io.codelabs.digitutor.data.model.Timetable
import io.codelabs.digitutor.data.model.Ward
import io.codelabs.digitutor.databinding.FragmentTimetableBinding
import io.codelabs.digitutor.view.adapter.TimetableAdapter
import io.codelabs.digitutor.view.kotlin.AddTimeTableActivity
import io.codelabs.digitutor.view.kotlin.WardsActivity
import io.codelabs.recyclerview.SlideInItemAnimator
import io.codelabs.sdk.glide.GlideApp
import io.codelabs.sdk.util.debugLog
import java.util.*

class TimeTableActivity : BaseActivity() {
    private lateinit var binding: FragmentTimetableBinding

    private var adapter: TimetableAdapter? = null
    private val wards = ArrayList<String>(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_timetable)

        adapter = TimetableAdapter(this)
        binding.grid?.adapter = adapter
        val lm = LinearLayoutManager(this)
        binding.grid?.layoutManager = lm
        binding.grid?.itemAnimator = SlideInItemAnimator()
        binding.grid?.setHasFixedSize(true)
        binding.grid?.addItemDecoration(DividerItemDecoration(this, lm.orientation))
        loadDataFromDatabase()

        /// Navigate to add timetable activity
        binding.addNewTimetable?.setOnClickListener { v ->
            intentTo(AddTimeTableActivity::class.java)
        }
    }

    private fun loadDataFromDatabase() {
        try {
            val snackbar = Snackbar.make(binding.root, "Error loading your wards", Snackbar.LENGTH_INDEFINITE)
            FirebaseDataSource.getUser(this, firestore, prefs.key,
                BaseUser.Type.PARENT,
                object : AsyncCallback<BaseUser> {
                    override fun onError(error: String?) {
                        snackbar.show()
                    }

                    override fun onSuccess(response: BaseUser?) {
                        if (response is Parent) {
                            val wards = response.wards
                            if (wards.isEmpty()) {
                                snackbar.show()
                            } else {
                                this@TimeTableActivity.wards.addAll(wards)
                                loadWardInfo(wards[0])
                                loadWardTimetable(wards[0])
                                binding.pickWard?.setOnClickListener { _ ->
                                    intentTo(
                                        WardsActivity::class.java,
                                        WardsActivity.WARD_EXTRA_CODE
                                    )
                                }
                            }
                        }
                    }

                    override fun onStart() {

                    }

                    override fun onComplete() {

                    }
                })
        } catch (ex: Exception) {
            debugLog(ex.localizedMessage)
        }

    }

    private fun loadWardInfo(ward: String) {
        try {
            FirebaseDataSource.getWard(this, ward, object : AsyncCallback<Ward> {
                override fun onError(error: String?) {
                    debugLog(error)
                }

                override fun onSuccess(response: Ward?) {
                    if (response != null) {
                        binding.ward = response
                        GlideApp.with(this@TimeTableActivity)
                            .load(response.avatar)
                            .circleCrop()
                            .placeholder(R.drawable.avatar_placeholder)
                            .error(R.drawable.avatar_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(binding.userAvatar as ImageView)
                    }
                }

                override fun onStart() {

                }

                override fun onComplete() {

                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun loadWardTimetable(ward: String) {
        val snackbar = Snackbar.make(binding.root, "Error loading your wards", Snackbar.LENGTH_INDEFINITE)

        try {
            FirebaseDataSource.getTimetableForUser(this, ward, object : AsyncCallback<MutableList<Timetable>> {
                override fun onError(error: String?) {
                    TransitionManager.beginDelayedTransition(binding.fragmentContainer)
                    binding.grid?.visibility = View.VISIBLE
                    binding.loading?.visibility = View.GONE
                    snackbar.show()
                }

                override fun onSuccess(response: MutableList<Timetable>?) {
                    if (response != null) {
                        adapter?.addData(response)
                    } else
                        snackbar.show()
                }

                override fun onStart() {
                    TransitionManager.beginDelayedTransition(binding.fragmentContainer)
                    binding.loading?.visibility = View.VISIBLE
                    binding.grid?.visibility = View.GONE
                }

                override fun onComplete() {
                    TransitionManager.beginDelayedTransition(binding.fragmentContainer)
                    binding.loading?.visibility = View.GONE
                    binding.grid?.visibility = View.VISIBLE
                }
            })
        } catch (ex: Exception) {
            debugLog(ex.localizedMessage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val ward = data?.getParcelableExtra<Ward?>(WardsActivity.WARD_EXTRA)
        debugLog("Ward from intent: $ward")

        if (ward != null && ward.key.isNotEmpty()) {
            binding.ward = ward

            GlideApp.with(this)
                .load(ward.avatar)
                .circleCrop()
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.userAvatar as ImageView)
            loadWardTimetable(ward.key)
        } else {
            debugLog("Ward is null")
        }
    }

}
