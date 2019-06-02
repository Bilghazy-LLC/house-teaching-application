package io.codelabs.digitutor.view.kotlin

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.databinding.ActivityAddTimetableBinding

class AddTimeTableActivity : BaseActivity() {

    private lateinit var binding: ActivityAddTimetableBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_timetable)


    }

}