package io.codelabs.digitutor.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.data.BaseUser
import io.codelabs.digitutor.data.model.Report
import io.codelabs.digitutor.data.model.Subject
import io.codelabs.digitutor.databinding.FragmentWithListBinding
import io.codelabs.digitutor.databinding.ItemReportBinding
import io.codelabs.digitutor.view.adapter.viewholder.EmptyViewHolder
import io.codelabs.sdk.util.debugLog
import io.codelabs.sdk.view.BaseFragment

class ReportsFragment : BaseFragment() {

    private var binding: FragmentWithListBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_with_list, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.grid
    }


    class ReportAdapter constructor(private val context: BaseActivity) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private val dataSource: MutableList<Report> = mutableListOf()

        companion object {
            private const val EMPTY = R.layout.item_empty
            private const val DATA = R.layout.item_schedule
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                EMPTY -> EmptyViewHolder(inflater.inflate(EMPTY, parent, false))
                DATA -> ReportsViewHolder(
                    DataBindingUtil.inflate(
                        inflater,
                        DATA,
                        parent,
                        false
                    ) as ItemReportBinding
                )
                else -> throw IllegalArgumentException("invalid viewholder")
            }
        }

        override fun getItemViewType(position: Int): Int = if (dataSource.isNotEmpty()) DATA else EMPTY

        override fun getItemCount(): Int = if (dataSource.isNotEmpty()) dataSource.size else 1

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            when (getItemViewType(position)) {
                EMPTY -> (holder as EmptyViewHolder).shimmer.startShimmer()
                DATA -> bindReports(holder as ReportsViewHolder, dataSource[position])
            }
        }

        private fun bindReports(holder: ReportsViewHolder, report: Report) {
            val firestore = context.firestore

            FirebaseDataSource.getSubject(firestore, report.subject, object : AsyncCallback<Subject?> {
                override fun onSuccess(response: Subject?) {
                    if (response != null) {
                        FirebaseDataSource.getUser(
                            context,
                            firestore,
                            report.tutor,
                            BaseUser.Type.TUTOR,
                            object : AsyncCallback<BaseUser?> {
                                override fun onSuccess(user: BaseUser?) {
                                    if (user != null) holder.bind(response, user)
                                }

                                override fun onError(error: String?) {

                                }
                            })
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

        fun addData(reports: MutableList<Report>) {
            dataSource.clear()
            dataSource.addAll(reports)
            notifyDataSetChanged()
        }

        class ReportsViewHolder(private val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(subject: Subject, tutor: BaseUser) {
                binding.tutor = tutor
                binding.subject = subject
            }
        }
    }
}
