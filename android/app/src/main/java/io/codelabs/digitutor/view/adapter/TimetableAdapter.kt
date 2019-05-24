package io.codelabs.digitutor.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.data.model.Subject
import io.codelabs.digitutor.data.model.Timetable
import io.codelabs.digitutor.databinding.ItemTimetableBinding
import io.codelabs.digitutor.view.adapter.viewholder.EmptyViewHolder
import io.codelabs.sdk.util.debugLog

class TimetableAdapter constructor(private val context: BaseActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val dataSource: MutableList<Timetable> = mutableListOf()

    companion object {
        private const val EMPTY = R.layout.item_empty
        private const val DATA = R.layout.item_timetable
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            EMPTY -> EmptyViewHolder(inflater.inflate(EMPTY, parent, false))
            DATA -> TimetableViewHolder(
                DataBindingUtil.inflate(
                    inflater,
                    DATA,
                    parent,
                    false
                ) as ItemTimetableBinding
            )
            else -> throw IllegalArgumentException("invalid viewholder")
        }
    }

    override fun getItemViewType(position: Int): Int = if (dataSource.isNotEmpty()) DATA else EMPTY

    override fun getItemCount(): Int = if (dataSource.isNotEmpty()) dataSource.size else 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            EMPTY -> (holder as EmptyViewHolder).shimmer.startShimmer()
            DATA -> bindSchedules(holder as TimetableViewHolder, dataSource[position])
        }
    }

    private fun bindSchedules(holder: TimetableViewHolder, timetable: Timetable) {
        val firestore = context.firestore

        FirebaseDataSource.getSubject(firestore, timetable.subject, object : AsyncCallback<Subject?> {
            override fun onSuccess(response: Subject?) {
                if (response != null) holder.bind(response, timetable, context)
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

    fun addData(timetables: MutableList<Timetable>) {
        dataSource.clear()
        dataSource.addAll(timetables)
        notifyDataSetChanged()
    }

    class TimetableViewHolder(private val binding: ItemTimetableBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: Subject, timetable: Timetable, context: Context) {
            binding.timetable = timetable
            binding.subject = subject
            binding.context = context
        }
    }
}