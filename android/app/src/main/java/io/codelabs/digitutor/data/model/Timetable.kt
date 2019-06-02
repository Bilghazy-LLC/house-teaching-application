package io.codelabs.digitutor.data.model

import io.codelabs.digitutor.data.BaseDataModel
import kotlinx.android.parcel.Parcelize

/**
 * [Timetable] data model class
 */
@Parcelize
data class Timetable(
    override var key: String,
    val ward: String,
    val tutor: String,
    val subject: String,
    var day: String,
    var startTime: String,
    var endTime: String
) : BaseDataModel {
    constructor() : this("", "", "", "", "", "", "")
}