package io.codelabs.digitutor.data

import android.os.Parcelable

/**
 * Base model class for all data models in the application
 */
interface BaseDataModel : Parcelable {
    var key: String
}