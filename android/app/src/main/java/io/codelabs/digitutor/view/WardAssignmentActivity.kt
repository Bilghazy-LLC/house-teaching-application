package io.codelabs.digitutor.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.data.model.Assignment
import io.codelabs.digitutor.data.model.Subject
import io.codelabs.digitutor.data.model.Tutor
import io.codelabs.digitutor.databinding.ActivityWardAssignmentBinding
import io.codelabs.digitutor.databinding.ItemAssignmentBinding
import io.codelabs.digitutor.view.adapter.viewholder.EmptyViewHolder
import io.codelabs.recyclerview.SlideInItemAnimator
import io.codelabs.sdk.util.debugLog

class WardAssignmentActivity : BaseActivity() {

    private lateinit var binding: ActivityWardAssignmentBinding
    private lateinit var adapter: AssignmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ward_assignment)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        setSupportActionBar(binding.toolbar)

        adapter = AssignmentAdapter(this)
        binding.grid.adapter = adapter
        binding.grid.itemAnimator = SlideInItemAnimator()
        val lm = LinearLayoutManager(this).also {
            binding.grid.layoutManager = it
        }
        binding.grid.addItemDecoration(DividerItemDecoration(this, lm.orientation))
        binding.grid.setHasFixedSize(true)
        loadAssignments()
    }

    private fun loadAssignments() {
        FirebaseDataSource.getAllTutors(this, firestore, prefs, object : AsyncCallback<MutableList<Tutor>?> {
            override fun onSuccess(tutors: MutableList<Tutor>?) {
                if (tutors == null) {
                    debugLog("Response is nul for all tutors")
                    return
                }

                tutors.forEach {
                    FirebaseDataSource.getTutorSubjects(
                        this@WardAssignmentActivity,
                        firestore,
                        it.key,
                        object : AsyncCallback<MutableList<Subject>?> {
                            override fun onSuccess(subjects: MutableList<Subject>?) {
                                if (subjects == null) {
                                    debugLog("Subjects are null")
                                    return
                                }

                                subjects.forEach { subject ->
                                    FirebaseDataSource.getAllAssignments(
                                        this@WardAssignmentActivity,
                                        firestore,
                                        prefs,
                                        subject.key/*null*/,
                                        it.key,
                                        object : AsyncCallback<MutableList<Assignment>?> {
                                            override fun onSuccess(assignments: MutableList<Assignment>?) {
                                                if (assignments != null) {
                                                    debugLog(assignments)
                                                    assignments.forEach { assignment -> adapter.addAssignment(assignment) }
                                                }
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

            }

            override fun onError(error: String?) {
                debugLog(error)
            }
        })
    }


    class AssignmentAdapter constructor(private val context: BaseActivity) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private val dataSource: MutableList<Assignment> = mutableListOf()

        companion object {
            private const val EMPTY = R.layout.item_empty
            private const val DATA = R.layout.item_assignment
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                EMPTY -> EmptyViewHolder(inflater.inflate(EMPTY, parent, false))
                DATA -> AssignmentViewholder(
                    DataBindingUtil.inflate(
                        inflater,
                        DATA,
                        parent,
                        false
                    ) as ItemAssignmentBinding
                )
                else -> throw IllegalArgumentException("invalid viewholder")
            }
        }

        override fun getItemViewType(position: Int): Int = if (dataSource.isNotEmpty()) DATA else EMPTY

        override fun getItemCount(): Int = if (dataSource.isNotEmpty()) dataSource.size else 1

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            when (getItemViewType(position)) {
                EMPTY -> (holder as EmptyViewHolder).shimmer.startShimmer()
                DATA -> bindSchedules(holder as AssignmentViewholder, dataSource[position])
            }
        }

        private fun bindSchedules(holder: AssignmentViewholder, assignment: Assignment) {
            val firestore = context.firestore

            FirebaseDataSource.getSubject(firestore, assignment.subject, object : AsyncCallback<Subject?> {
                override fun onSuccess(response: Subject?) {
                    if (response != null) {
                        holder.bind(response, assignment)
                        holder.binding.download.setOnClickListener {
                            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(assignment.filePath)
                            })
                        }
                    }
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

        fun addData(assignments: MutableList<Assignment>) {
            dataSource.clear()
            dataSource.addAll(assignments)
            notifyDataSetChanged()
        }

        fun addAssignment(assignment: Assignment) {
            dataSource.add(assignment)
            notifyDataSetChanged()
        }

        class AssignmentViewholder(val binding: ItemAssignmentBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(subject: Subject, assignment: Assignment) {
                binding.assignment = assignment
                binding.subject = subject
            }
        }
    }

}
