package io.codelabs.digitutor.data.model

import io.codelabs.digitutor.data.BaseDataModel
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * [Schedule] data model class
 */
@Parcelize
data class Schedule(
        override var key: String,
        val tutor: String,
        val subject: String,
        val ward: String,
        val startTime: Long,
        val endTime: Long,
        var status: Boolean = false,
        val days: Date = Date()
) : BaseDataModel {
    constructor() : this("", "", "", "", 0L, 0L)
}