package io.codelabs.digitutor.view.kotlin

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.view.HomeActivity

class LandingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
    }

    fun searchTutor(view: View) =
        intentTo(SearchActivity::class.java, bundleOf(Pair<String, Any>(SearchActivity.SEARCH_TUTOR, true)), false)

    fun searchSubject(view: View) =
        intentTo(SearchActivity::class.java, bundleOf(Pair<String, Any>(SearchActivity.SEARCH_SUBJECT, true)), false)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.landing_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_home -> {
                intentTo(HomeActivity::class.java)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun navHome(view: View) = intentTo(HomeActivity::class.java)
}