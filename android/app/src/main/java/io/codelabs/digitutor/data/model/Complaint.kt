package io.codelabs.digitutor.data.model

import io.codelabs.digitutor.data.BaseDataModel
import kotlinx.android.parcel.Parcelize

/**
 * [Complaint] data model
 */
@Parcelize
data class Complaint(
        override var key: String,
        val parent: String,
        val tutor: String,
        var description: String,
        val timestamp: Long = System.currentTimeMillis()
) : BaseDataModel {
    constructor() : this("", "", "", "")
}