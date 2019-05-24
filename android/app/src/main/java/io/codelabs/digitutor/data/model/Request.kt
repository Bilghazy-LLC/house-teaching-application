package io.codelabs.digitutor.data.model

import io.codelabs.digitutor.data.BaseDataModel
import kotlinx.android.parcel.Parcelize

/**
 * [Parent] requests for [Tutor] data model class
 */
@Parcelize
data class Request(
        override var key: String,
        val parent: String,
        val tutor: String,
        val timestamp: Long = System.currentTimeMillis()
) : BaseDataModel {

    constructor() : this("", "", "", 0L)
}