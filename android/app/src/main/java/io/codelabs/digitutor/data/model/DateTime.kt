package io.codelabs.digitutor.data.model

import io.codelabs.digitutor.data.BaseDataModel
import kotlinx.android.parcel.Parcelize

/**
 * Data model for day and time
 */
@Parcelize
data class DateTime(
    override var key: String,
    var day: String,
    var time: String
) : BaseDataModel {
    constructor() : this("", "", "")
}