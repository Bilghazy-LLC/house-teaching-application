package io.codelabs.digitutor.view.kotlin

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.core.util.Constants
import io.codelabs.digitutor.data.BaseUser
import io.codelabs.digitutor.data.model.Ward
import io.codelabs.digitutor.databinding.ActivityWardsBinding
import io.codelabs.digitutor.view.adapter.UsersAdapter
import io.codelabs.recyclerview.GridItemDividerDecoration
import io.codelabs.recyclerview.SlideInItemAnimator
import io.codelabs.sdk.util.debugLog

class WardsActivity : BaseActivity() {
    private lateinit var binding: ActivityWardsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wards)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        val adapter = UsersAdapter(applicationContext) { item, _ ->
            if (prefs.type == BaseUser.Type.TUTOR) {
                //todo: update progress report
                val v = layoutInflater.inflate(R.layout.dialog_progress_update, null, false)
                MaterialDialog(this).show {
                    title(text = "Student progress report")
                    customView(view = v)
                    positiveButton(text = "Send") {
                        it.dismiss()

                        firestore.collection(Constants.REPORTS)
                    }
                }
            } else {
                setResult(WARD_EXTRA_CODE, Intent().apply {
                    putExtra(WARD_EXTRA, item)
                })
                finishAfterTransition()
            }
        }

        binding.grid.adapter = adapter
        binding.grid.setHasFixedSize(true)
        binding.grid.itemAnimator = SlideInItemAnimator()
        binding.grid.addItemDecoration(GridItemDividerDecoration(this, R.dimen.divider_height, R.color.divider))
        binding.grid.layoutManager = LinearLayoutManager(this)

        FirebaseDataSource.getWards(this, prefs.key, object : AsyncCallback<MutableList<Ward>?> {
            override fun onSuccess(response: MutableList<Ward>?) {
                adapter.addData(response)
            }

            override fun onError(error: String?) {
                debugLog(error)
            }
        })

    }

    companion object {
        const val WARD_EXTRA = "WARD_EXTRA"
        const val WARD_EXTRA_CODE = 2
    }
}