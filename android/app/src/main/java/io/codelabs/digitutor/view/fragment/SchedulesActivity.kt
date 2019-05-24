package io.codelabs.digitutor.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.snackbar.Snackbar
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.data.BaseUser
import io.codelabs.digitutor.data.model.Schedule
import io.codelabs.digitutor.data.model.Subject
import io.codelabs.digitutor.databinding.ActivitySchedulesBinding
import io.codelabs.digitutor.databinding.ItemScheduleBinding
import io.codelabs.digitutor.view.adapter.viewholder.EmptyViewHolder
import io.codelabs.recyclerview.SlideInItemAnimator
import io.codelabs.sdk.util.debugLog
import io.codelabs.sdk.util.toast

class SchedulesActivity : BaseActivity() {

    private lateinit var binding: ActivitySchedulesBinding

    private var startDate: Long? = null
    private var endDate: Long? = null
    private var subject: Subject? = null
    private var ward: String = ""
    private val subjects = mutableListOf<Subject>()
    private val names = mutableListOf<String>()
    private lateinit var adapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_schedules)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { v -> onBackPressed() }

        binding.isTutor = prefs.type == BaseUser.Type.TUTOR
        debugLog("User is tutor: " + binding.isTutor)

        // Setup recyclerview
        adapter = ScheduleAdapter(this)
        binding.schedulesGrid.setHasFixedSize(false)
        binding.schedulesGrid.adapter = adapter
        val lm = LinearLayoutManager(this)
        binding.schedulesGrid.layoutManager = lm
        binding.schedulesGrid.itemAnimator = SlideInItemAnimator()
        binding.schedulesGrid.addItemDecoration(DividerItemDecoration(this, lm.orientation))

        loadData()
    }

    fun pickSchedule(view: View) {
        MaterialDialog(this).show {
            title(text = "Select a subject")
            listItemsSingleChoice(items = names, waitForPositiveButton = false) { dialog, index, _ ->
                dialog.dismiss()
                val subject = subjects[index]
                debugLog(subject)
                this@SchedulesActivity.subject = subject

                if (startDate != null && endDate != null) {
                    debugLog("Start: $startDate. End: $endDate. Subject: ${subject.key}. Ward: $ward")
                    post(subject)
                } else {
                    MaterialDialog(this@SchedulesActivity).show {
                        title(text = "Set Start Date...")
                        dateTimePicker(
                            show24HoursView = true,
                            requireFutureDateTime = true
                        ) { dialog, datetime ->
                            dialog.dismiss()
                            startDate = datetime.timeInMillis
                            pickEndDate()
                        }
                    }
                }
            }
            cancelable(true)
            cancelOnTouchOutside(false)
        }
    }

    private fun pickEndDate() {
        // Pick an end date
        MaterialDialog(this@SchedulesActivity).show {
            title(text = "Set End Date...")
            dateTimePicker(
                show24HoursView = true,
                requireFutureDateTime = true
            ) { dialog, datetime ->
                dialog.dismiss()
                endDate = datetime.timeInMillis
                post(subject!!)
            }
        }
    }

    private fun post(subject: Subject) {
        FirebaseDataSource.postSchedule(
            this,
            ward,
            subject.key,
            startDate!!,
            endDate!!,
            object : AsyncCallback<Void?> {

                override fun onSuccess(response: Void?) {
                    toast("Schedule added successfully")
                }

                override fun onComplete() {

                }

                override fun onError(error: String?) {
                    toast(error, true)
                }

                override fun onStart() {
                    toast("Adding schedule...")
                    finishAfterTransition()
                }
            })
    }

    private fun loadData() {
        FirebaseDataSource.fetchAllSchedules(
            this,
            firestore,
            if (prefs.type == BaseUser.Type.TUTOR) prefs.key else intent.getStringExtra(EXTRA_TUTOR_ID)
                ?: intent.getParcelableExtra<BaseUser>(
                    EXTRA_TUTOR
                ).key,
            object : AsyncCallback<MutableList<Schedule>?> {

                override fun onSuccess(response: MutableList<Schedule>?) {
                    debugLog("Schedules: $response")
                    if (response != null) adapter.addData(response)
                }

                override fun onComplete() {

                }

                override fun onError(error: String?) {
                    Snackbar.make(binding.container, error!!, Snackbar.LENGTH_INDEFINITE).apply {
                        setAction("Dismiss") {
                            dismiss()
                        }
                        show()
                    }
                }

                override fun onStart() {
                    debugLog("Fetching all schedules...")
                }
            })


        FirebaseDataSource.getTutorSubjects(this, firestore, prefs.key, object : AsyncCallback<List<Subject>> {
            override fun onError(error: String?) {
                toast("Error loading subjects.\n$error")
                debugLog("Error loading subjects.\n$error")
            }

            override fun onSuccess(response: List<Subject>?) {
                if (response != null && response.isNotEmpty()) {
                    subjects.addAll(response)

                    for ((_, name) in response) {
                        names.add(name)
                    }
                    debugLog(names)
                } else {
                    FirebaseDataSource.fetchAllSubjects(this@SchedulesActivity, firestore, this)
                }
            }

            override fun onStart() {
                if (names.isEmpty()) debugLog("Loading all subjects")
            }

            override fun onComplete() {
                if (names.isNotEmpty()) debugLog("Loaded successfully")
            }
        })

    }

    companion object {
        const val EXTRA_TUTOR = "EXTRA_TUTOR"
        const val EXTRA_TUTOR_ID = "EXTRA_TUTOR_ID"
    }

    class ScheduleAdapter constructor(private val context: BaseActivity) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private val dataSource: MutableList<Schedule> = mutableListOf()

        companion object {
            private const val EMPTY = R.layout.item_empty
            private const val DATA = R.layout.item_schedule
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                EMPTY -> EmptyViewHolder(inflater.inflate(EMPTY, parent, false))
                DATA -> SchedulesViewHolder(
                    DataBindingUtil.inflate(
                        inflater,
                        DATA,
                        parent,
                        false
                    ) as ItemScheduleBinding
                )
                else -> throw IllegalArgumentException("invalid viewholder")
            }
        }

        override fun getItemViewType(position: Int): Int = if (dataSource.isNotEmpty()) DATA else EMPTY

        override fun getItemCount(): Int = if (dataSource.isNotEmpty()) dataSource.size else 1

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            when (getItemViewType(position)) {
                EMPTY -> (holder as EmptyViewHolder).shimmer.startShimmer()
                DATA -> bindSchedules(holder as SchedulesViewHolder, dataSource[position])
            }
        }

        private fun bindSchedules(holder: SchedulesViewHolder, schedule: Schedule) {
            val firestore = context.firestore

            FirebaseDataSource.getSubject(firestore, schedule.subject, object : AsyncCallback<Subject?> {
                override fun onSuccess(response: Subject?) {
                    if (response != null) holder.bind(response, schedule)
                }

                override fun onComplete() {

                }

                override fun onError(error: String?) {
                    debugLog("Unable to retrieve subject details")
                }

                override fun onStart() {
                    debugLog("Loading subject for schedule")
                }
            })

        }

        fun addData(schedules: MutableList<Schedule>) {
            dataSource.clear()
            dataSource.addAll(schedules)
            notifyDataSetChanged()
        }

        class SchedulesViewHolder(private val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(subject: Subject, schedule: Schedule) {
                binding.schedule = schedule
                binding.subject = subject
            }
        }
    }
}
