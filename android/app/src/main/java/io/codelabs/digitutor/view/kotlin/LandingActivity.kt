package io.codelabs.digitutor.view.kotlin

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity

class LandingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
    }

    fun searchTutor(view: View) = intentTo(SearchActivity::class.java, bundleOf(Pair<String, Any>(SearchActivity.SEARCH_TUTOR, true)), false)

    fun searchSubject(view: View) =
        intentTo(SearchActivity::class.java, bundleOf(Pair<String, Any>(SearchActivity.SEARCH_SUBJECT, true)), false)
}