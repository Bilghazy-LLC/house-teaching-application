package io.codelabs.digitutor.data.model

import io.codelabs.digitutor.data.BaseDataModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Student(
    override var key: String,
    var parent: String,
    var subject: String
) : BaseDataModel {
    constructor() : this("", "", "")
}