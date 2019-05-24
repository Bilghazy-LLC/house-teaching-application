package io.codelabs.digitutor.data.model

import io.codelabs.digitutor.data.BaseDataModel
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * [Timetable] data model class
 */
@Parcelize
data class Timetable(
        override var key: String,
        val ward: String,
        val tutor: String,
        val subject: String,
        var day: Date = Date(),
        var time: Long = System.currentTimeMillis()
) : BaseDataModel {
    constructor() : this("", "", "", "")
}