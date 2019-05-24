package io.codelabs.digitutor.core.datasource.remote

import androidx.annotation.Nullable

/**
 * [Ward] credentials class
 * Helper class in creating a new ward
 */
data class WardCredentials(val name: String, @Nullable val avatar: String? = null)