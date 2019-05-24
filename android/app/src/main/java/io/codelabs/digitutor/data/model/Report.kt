package io.codelabs.digitutor.data.model

import io.codelabs.digitutor.data.BaseDataModel
import kotlinx.android.parcel.Parcelize

/**
 * [Report] data model class
 */
@Parcelize
data class Report(
        override var key: String,
        val tutor: String,
        val subject: String,
        val ward: String,
        val assignment: String,
        val marks: Int = 0
) : BaseDataModel {
    constructor() : this("", "", "", "", "")
}