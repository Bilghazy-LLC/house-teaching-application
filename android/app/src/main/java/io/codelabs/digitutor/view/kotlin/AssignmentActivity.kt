package io.codelabs.digitutor.view.kotlin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.snackbar.Snackbar
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.base.BaseActivity
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource
import io.codelabs.digitutor.core.util.AsyncCallback
import io.codelabs.digitutor.core.util.InputValidator
import io.codelabs.digitutor.data.model.Parent
import io.codelabs.digitutor.data.model.Subject
import io.codelabs.digitutor.data.model.Ward
import io.codelabs.digitutor.databinding.ActivityAddAssignmentBinding
import io.codelabs.digitutor.view.adapter.UsersAdapter
import io.codelabs.recyclerview.GridItemDividerDecoration
import io.codelabs.recyclerview.SlideInItemAnimator
import io.codelabs.sdk.util.debugLog
import io.codelabs.sdk.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Assignments screen
 * Allows a tutor to send assignment to his or her students
 */
class AssignmentActivity : BaseActivity() {

    private lateinit var binding: ActivityAddAssignmentBinding
    private var filePath: Uri? = Uri.EMPTY
    private val names = mutableListOf<String>()
    private val subjects = mutableListOf<Subject>()
    private val wards = mutableListOf<Ward>()
    private var ward: String? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var subject: Subject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_assignment)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.showList = true
        setupTutor()
        setupClients()
    }

    private fun setupClients() {
        val adapter = UsersAdapter(this) { item, isLongClick ->
            if (!isLongClick) {
                ward = item.key
                toast("${item.name} selected as Parent")
            }
        }
        binding.wardsGrid.layoutManager = LinearLayoutManager(this)
        binding.wardsGrid.itemAnimator = SlideInItemAnimator()
        binding.wardsGrid.addItemDecoration(GridItemDividerDecoration(this, R.dimen.divider_height, R.color.divider))
        binding.wardsGrid.adapter = adapter
        binding.wardsGrid.setHasFixedSize(true)
        FirebaseDataSource.getAllClients(this, firestore, prefs, object : AsyncCallback<MutableList<Parent>?> {
            override fun onSuccess(response: MutableList<Parent>?) {
                if (response != null) adapter.addData(response)
            }

            override fun onComplete() {

            }

            override fun onError(error: String?) {

            }

            override fun onStart() {

            }
        })
    }

    private fun setupTutor() {
        // Snackbar to show notification on the screen for the current user
        val snackbar = Snackbar.make(binding.container, "Loading all subjects for you", Snackbar.LENGTH_LONG)

        FirebaseDataSource.getTutorSubjects(this, firestore, prefs.key, object : AsyncCallback<List<Subject>> {
            override fun onError(error: String?) {
                snackbar.setText(error ?: "An unknown error occurred").show()
            }

            override fun onSuccess(response: List<Subject>?) {
                if (response != null && response.isNotEmpty()) {
                    subjects.addAll(response)

                    for ((_, name) in response) {
                        names.add(name)
                    }
                    debugLog(names)
                } else {
                    FirebaseDataSource.fetchAllSubjects(this@AssignmentActivity, firestore, this)
                }
            }

            override fun onStart() {
                if (names.isEmpty()) snackbar.show()
            }

            override fun onComplete() {
                if (names.isNotEmpty()) snackbar.setText("Subjects loaded successfully").show()
            }
        })
    }

    fun sendAssignment(view: View?) {
        if (filePath != null) {
            MaterialDialog(this).show {
                title(text = "Select a subject")
                listItemsSingleChoice(items = names, waitForPositiveButton = false) { dialog, index, _ ->
                    dialog.dismiss()
                    val subject = subjects[index]
                    debugLog(subject)
                    this@AssignmentActivity.subject = subject

                    if (InputValidator.hasValidInput(binding.comments.text.toString())) {
                        if (startDate != null && endDate != null) {
                            debugLog("Start: $startDate. End: $endDate. Subject: ${subject.key}. Ward: $ward")
                            post(subject)
                        } else {
                            MaterialDialog(this@AssignmentActivity).show {
                                title(text = "Set Start Date...")
                                dateTimePicker(
                                    show24HoursView = true,
                                    requireFutureDateTime = true
                                ) { dialog, datetime ->
                                    dialog.dismiss()
                                    startDate = datetime.timeInMillis
                                    pickEndDate()
                                }
                            }
                        }
                    } else toast("Please enter a comment to this assignment...", true)
                }
                cancelable(true)
                cancelOnTouchOutside(false)
            }
        } else {
            toast("Please attach an assignment file first", true)
        }

    }

    private fun post(subject: Subject) {
        ioScope.launch {
            FirebaseDataSource.sendAssignment(firestore,
                storage,
                prefs,
                binding.comments.text.toString(),
                filePath.toString(),
                ward,
                subject.key,
                startDate!!,
                endDate!!,
                object : AsyncCallback<Void?> {
                    override fun onSuccess(response: Void?) {
                        debugLog("Assignment uploaded successfully")
                    }

                    override fun onComplete() {

                    }

                    override fun onError(error: String?) {
                        debugLog(error)
                    }

                    override fun onStart() {
                        debugLog("Sending assignment to ward")
                    }
                })
        }
        finishAfterTransition()
    }

    private fun pickEndDate() {
        // Pick an end date
        MaterialDialog(this@AssignmentActivity).show {
            title(text = "Set End Date...")
            dateTimePicker(
                show24HoursView = true,
                requireFutureDateTime = true
            ) { dialog, datetime ->
                dialog.dismiss()
                endDate = datetime.timeInMillis
                post(subject!!)
            }
        }
    }

    /*private fun pickWard() {
        MaterialDialog(this@AssignmentActivity, BottomSheet()).show {
            gridItems(
                items = wards,
                waitForPositiveButton = false
            ) { dialog, _, item ->
                dialog.dismiss()
                ward = item.key
                debugLog(item)
                if (subject == null) {
                    setupTutor()
                    return@gridItems
                }

                post(subject!!)
            }
        }
    }*/

    fun pickAssignment(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"

        // Set file types
        val mimeTypes = arrayOf(
            "application/pdf",
            "text/plain",
            "application/vnd.ms-word",
            "application/vnd.ms-excel",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        )
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, RC_FILE)
        } else
            toast("No application available for this action", true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                filePath = data.data
                debugLog(filePath)
                binding.fileUrl = "File attached successfully"
            } else
                toast("Unable to pick file", false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.refresh_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_refresh) {
            names.clear()
            subjects.clear()
            setupTutor()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val RC_FILE = 22
    }
}
