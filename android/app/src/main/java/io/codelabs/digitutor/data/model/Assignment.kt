package io.codelabs.digitutor.data.model

import io.codelabs.digitutor.data.BaseDataModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Assignment(
    override var key: String,
    val ward: String?,
    var comment: String,
    var filePath: String,
    val subject: String,
    val dateGiven: Long,
    val dateDue: Long
) : BaseDataModel {

    constructor() : this("", "", "", "", "", 0L, 0L)
}