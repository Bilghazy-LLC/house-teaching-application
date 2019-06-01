package io.codelabs.digitutor.view.kotlin

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.gms.tasks.Tasks
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.core.util.Constants
import io.codelabs.digitutor.core.util.OnClickListener
import io.codelabs.digitutor.data.BaseDataModel
import io.codelabs.digitutor.data.BaseUser
import io.codelabs.digitutor.data.model.Complaint
import io.codelabs.digitutor.data.model.Subject
import io.codelabs.digitutor.data.model.Tutor
import io.codelabs.digitutor.databinding.ActivitySearchBinding
import io.codelabs.digitutor.view.ComplaintActivity
import io.codelabs.digitutor.view.UserActivity
import io.codelabs.digitutor.view.adapter.SearchAdapter
import io.codelabs.recyclerview.GridItemDividerDecoration
import io.codelabs.recyclerview.SlideInItemAnimator
import io.codelabs.sdk.util.debugLog
import io.codelabs.sdk.util.toast
import io.codelabs.util.ImeUtils
import kotlinx.coroutines.launch

/**
 * Search screen
 */
class SearchActivity : BaseActivity(), OnClickListener<BaseDataModel> {

    // Get the data binding class
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the data binding class
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)

        adapter = SearchAdapter(this, this)
        binding.searchResults.adapter = adapter
        binding.searchResults.layoutManager = LinearLayoutManager(this)
        binding.searchResults.addItemDecoration(
            GridItemDividerDecoration(
                this,
                R.dimen.divider_height,
                R.color.divider
            )
        )
        binding.searchResults.itemAnimator = SlideInItemAnimator()
        binding.searchResults.setHasFixedSize(true)
        setupSearchView()
    }

    private fun setupSearchView() {
        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        binding.searchView.setSearchableInfo(manager.getSearchableInfo(componentName))

        binding.searchView.queryHint = getString(R.string.search_hint)
        binding.searchView.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
        binding.searchView.imeOptions =
            binding.searchView.imeOptions or EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_FLAG_NO_FULLSCREEN
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchFor(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == null || TextUtils.isEmpty(newText)) {
                    clearResults()
                } /*else searchFor(newText);*/
                return true
            }
        })
        onNewIntent(intent)

        when {
            intent.hasExtra(SEARCH_SUBJECT) -> {
                ioScope.launch {
                    val subjects =
                        Tasks.await(firestore.collection(Constants.SUBJECTS).get()).toObjects(Subject::class.java)
                    debugLog(subjects)
                    uiScope.launch {
                        binding.loading.visibility = View.GONE
                        binding.searchResults.visibility = View.VISIBLE
                        adapter.addData(subjects)
                    }
                }
            }

            intent.hasExtra(SEARCH_TUTOR) -> {
                ioScope.launch {
                    val tutors = Tasks.await(firestore.collection(Constants.TUTORS).get()).toObjects(Tutor::class.java)
                    debugLog(tutors)
                    uiScope.launch {
                        binding.loading.visibility = View.GONE
                        binding.searchResults.visibility = View.VISIBLE
                        adapter.addData(tutors)
                    }
                }
            }
        }
    }

    private fun searchFor(query: String) {
        clearResults()
        binding.loading.visibility = View.VISIBLE

        // Perform search on the background thread
        ioScope.launch {
            FirebaseDataSource.searchFor(firestore, query, object : AsyncCallback<List<BaseDataModel>> {
                override fun onError(error: String?) {
                    toast(error, true)
                }

                override fun onSuccess(response: List<BaseDataModel>?) {
                    // present query results on the foreground thread
                    uiScope.launch {
                        debugLog(response)
                        binding.loading.visibility = View.GONE
                        binding.searchResults.visibility = View.VISIBLE

                        when {
                            intent.hasExtra(SEARCH_SUBJECT) -> adapter.addData(response?.filter { it is Subject })
                            intent.hasExtra(SEARCH_TUTOR) -> adapter.addData(response?.filter { it is Tutor })
                            else -> adapter.addData(response)
                        }
                    }
                }

                override fun onStart() {}

                override fun onComplete() {}
            })
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && intent.hasExtra(SearchManager.QUERY)) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            if (query.isNotEmpty()) {
                binding.searchView.setQuery(query, false)
                searchFor(query)
            }
        }
    }

    private fun clearResults() {
        TransitionManager.beginDelayedTransition(binding.container)
        binding.searchResults.visibility = View.GONE
        binding.loading.visibility = View.GONE
        binding.resultsScrim.visibility = View.GONE

    }

    // This is to close the search activity
    fun dismiss(view: View?) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onBackPressed() {
        // this is to avoid accidental closure of the search activity when results are being fetched or query is being entered
        if (binding.searchView.hasFocus())
            ImeUtils.hideIme(binding.searchView)
        else
            dismiss(null)
    }

    override fun onClick(item: BaseDataModel?, isLongClick: Boolean) {
        if (!isLongClick) {
            when (item) {
                is BaseUser -> {
                    // Pass the user's information on to the next activity
                    intentTo(UserActivity::class.java, Bundle().apply {
                        putParcelable(UserActivity.EXTRA_USER, item)
                        putString(UserActivity.EXTRA_USER_TYPE, item.type)
                        putString(UserActivity.EXTRA_USER_UID, item.key)
                    }, false)
                }

                is Subject -> {
                    intentTo(
                        TutorsActivity::class.java,
                        bundleOf(Pair<String, Any>(TutorsActivity.SUBJECT_QUERY, item.name)),
                        false
                    )
                }

                is Complaint -> {
                    intentTo(
                        ComplaintActivity::class.java,
                        bundleOf(Pair<String, Any>(ComplaintActivity.EXTRA_COMPLAINT, item)),
                        false
                    )
                }
            }
        }
    }

    companion object {
        const val SEARCH_SUBJECT = "search_subject"
        const val SEARCH_TUTOR = "search_tutor"
    }
}
