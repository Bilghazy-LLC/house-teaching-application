package io.codelabs.digitutor.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
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

class TimeTableFragment : Fragment() {
    private lateinit var binding: FragmentTimetableBinding
    private var ward = ""

    private var adapter: TimetableAdapter? = null
    private val wards = ArrayList<String>(0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_timetable, container, false)
        return binding.root
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        val ward = args?.getParcelable<Ward?>(WardsActivity.WARD_EXTRA)
        debugLog("Arguments ward: $ward")
        if (ward != null) binding.ward = ward
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TimetableAdapter(requireActivity() as BaseActivity)
        binding.grid.adapter = adapter
        val lm = LinearLayoutManager(requireContext())
        binding.grid.layoutManager = lm
        binding.grid.itemAnimator = SlideInItemAnimator()
        binding.grid.setHasFixedSize(true)
        binding.grid.addItemDecoration(DividerItemDecoration(requireContext(), lm.orientation))
        loadDataFromDatabase()

        /// Navigate to add timetable activity
        binding.addNewTimetable.setOnClickListener { v ->
            requireActivity().startActivity(
                Intent(
                    requireContext(),
                    AddTimeTableActivity::class.java
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        loadDataFromDatabase()
    }

    private fun loadDataFromDatabase() {
        try {
            val snackbar = Snackbar.make(binding.root, "Error loading your wards", Snackbar.LENGTH_INDEFINITE)
            FirebaseDataSource.getUser(requireActivity(),
                (requireActivity() as BaseActivity).firestore,
                (requireActivity() as BaseActivity).prefs.key,
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
                                this@TimeTableFragment.wards.addAll(wards)
                                ward = wards[0]
                                loadWardInfo(ward)
                                loadWardTimetable(ward)
                                binding.pickWard.setOnClickListener { v ->
                                    (requireActivity() as BaseActivity).intentTo(
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
            val host = requireActivity() as BaseActivity
            FirebaseDataSource.getWard(host, ward, object : AsyncCallback<Ward> {
                override fun onError(error: String?) {
                    debugLog(error)
                }

                override fun onSuccess(response: Ward?) {
                    if (response != null) {
                        binding.ward = response
                        GlideApp.with(host)
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
            FirebaseDataSource.getTimetableForUser(
                requireActivity() as BaseActivity,
                ward,
                object : AsyncCallback<List<Timetable>> {
                    override fun onError(error: String?) {
                        TransitionManager.beginDelayedTransition(binding.fragmentContainer)
                        binding.grid.visibility = View.VISIBLE
                        binding.loading.visibility = View.GONE
                        snackbar.show()
                    }

                    override fun onSuccess(response: List<Timetable>?) {
                        if (response != null) {
                            // adapter.addData(response);
                        } else
                            snackbar.show()
                    }

                    override fun onStart() {
                        TransitionManager.beginDelayedTransition(binding.fragmentContainer)
                        binding.loading.visibility = View.VISIBLE
                        binding.grid.visibility = View.GONE
                    }

                    override fun onComplete() {
                        TransitionManager.beginDelayedTransition(binding.fragmentContainer)
                        binding.loading.visibility = View.GONE
                        binding.grid.visibility = View.VISIBLE
                    }
                })
        } catch (ex: Exception) {
            requireContext().debugLog(ex.localizedMessage)
        }

    }

    /* @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            ExtensionUtils.debugLog(requireContext(), data != null ? data.getParcelableExtra() : null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

}
