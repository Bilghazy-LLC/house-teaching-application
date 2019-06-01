package io.codelabs.digitutor.view

import android.os.Bundle
import android.view.View
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity

class LandingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
    }

    fun searchTutor(view: View) {}
    fun searchSubject(view: View) {}
}