package io.codelabs.digitutor.data.model

import io.codelabs.digitutor.data.BaseDataModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Feedback(
    override var key: String,
    val parent: String,
    val tutor: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) : BaseDataModel {

    constructor() : this("", "", "", "")
}