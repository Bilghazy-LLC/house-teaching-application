package io.codelabs.digitutor.core.util

import io.codelabs.digitutor.data.BaseUser
import io.codelabs.digitutor.data.model.Subject

fun MutableList<Subject>.extractNames(): MutableList<String> = mutableListOf<String>().apply {
    this@extractNames.forEach {
        add(it.name)
    }
}

fun MutableList<BaseUser>.getUsernames(): MutableList<String> = mutableListOf<String>().apply {
    this@getUsernames.forEach {
        if (!it.name.isNullOrEmpty()) add(it.name!!)
    }
}