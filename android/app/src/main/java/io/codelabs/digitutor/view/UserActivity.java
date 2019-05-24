package io.codelabs.digitutor.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.data.BaseUser;
import io.codelabs.digitutor.data.model.Subject;
import io.codelabs.digitutor.data.model.Tutor;
import io.codelabs.digitutor.databinding.ActivityUserBinding;
import io.codelabs.digitutor.view.adapter.SubjectAdapter;
import io.codelabs.digitutor.view.fragment.SchedulesActivity;
import io.codelabs.digitutor.view.kotlin.MakeComplaintActivity;
import io.codelabs.recyclerview.GridItemDividerDecoration;
import io.codelabs.recyclerview.SlideInItemAnimator;
import io.codelabs.sdk.util.ExtensionUtils;

import java.util.List;

public class UserActivity extends BaseActivity {
    public static final String EXTRA_USER = "EXTRA_USER";
    public static final String EXTRA_USER_UID = "EXTRA_USER_UID";
    public static final String EXTRA_USER_TYPE = "EXTRA_USER_TYPE";

    private ActivityUserBinding binding;
    private SubjectAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize the adapter
        adapter = new SubjectAdapter(UserActivity.this, (subject, isLongClick) -> {
            // do nothing
        });
        binding.subjectsGrid.setAdapter(adapter);
        binding.subjectsGrid.setLayoutManager(new LinearLayoutManager(this));
        binding.subjectsGrid.setItemAnimator(new SlideInItemAnimator());
        binding.subjectsGrid.addItemDecoration(new GridItemDividerDecoration(this, R.dimen.divider_height, R.color.divider));
        binding.subjectsGrid.setHasFixedSize(true);

        if (getIntent().hasExtra(EXTRA_USER)) {
            binding.setUser(getIntent().getParcelableExtra(EXTRA_USER));
            ExtensionUtils.debugLog(this, binding.getUser().getType());
            getRequest(binding.getUser().getKey());
        } else if (getIntent().hasExtra(EXTRA_USER_UID)) {
            Snackbar snackbar = Snackbar.make(binding.container, "Fetching user information", Snackbar.LENGTH_INDEFINITE);
            FirebaseDataSource.getUser(this, firestore, getIntent().getStringExtra(EXTRA_USER_UID), getIntent().getStringExtra(EXTRA_USER_TYPE), new AsyncCallback<BaseUser>() {
                @Override
                public void onError(@Nullable String error) {
                    snackbar.setText(error == null ? "An error occurred while retrieving user information" : error);
                    snackbar.setDuration(BaseTransientBottomBar.LENGTH_LONG);
                }

                @Override
                public void onSuccess(@Nullable BaseUser response) {
                    if (response == null) return;
                    ExtensionUtils.debugLog(UserActivity.this, response.getType());
                    binding.setUser(response);
                    getRequest(response.getKey());
                }

                @Override
                public void onStart() {
                    snackbar.show();
                }

                @Override
                public void onComplete() {
                    snackbar.dismiss();
                }
            });
        }
    }

    /**
     * Check whether or not user has already sent a request to the tutor
     *
     * @param key The tutor's key
     */
    private void getRequest(String key) {
        // Snackbar to show notification on the screen for the current user
        Snackbar snackbar = Snackbar.make(binding.container, "", Snackbar.LENGTH_LONG);
        binding.tutorContent.setVisibility(binding.getUser().getType().equals(BaseUser.Type.TUTOR) ? View.VISIBLE : View.GONE);

        FirebaseDataSource.getSentRequests(UserActivity.this, firestore, prefs, key, new AsyncCallback<Boolean>() {
            @Override
            public void onError(@Nullable String error) {
                snackbar.setText(error == null ? "An unknown error occurred" : error).show();
            }

            @Override
            public void onSuccess(@Nullable Boolean response) {
                ExtensionUtils.debugLog(UserActivity.this, response);
                binding.requestButton.setEnabled(response != null && response);

                if (binding.getUser().getType().equals(BaseUser.Type.TUTOR)) {
                    // Get all subjects for this tutor
                    FirebaseDataSource.getTutorSubjects(UserActivity.this, firestore, key, new AsyncCallback<List<Subject>>() {
                        @Override
                        public void onError(@Nullable String error) {
                            snackbar.setText(error == null ? "An unknown error occurred" : error).show();
                        }

                        @Override
                        public void onSuccess(@Nullable List<Subject> response) {
                            if (response != null) {
                                adapter.addData(response);
                            }
                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void requestService(View view) {
        if (binding.getUser() != null && binding.getUser() instanceof Tutor) {
            ExtensionUtils.toast(getApplicationContext(), "Sending request...", false);
            FirebaseDataSource.requestService(firestore, prefs, binding.getUser().getKey(), new AsyncCallback<Void>() {
                @Override
                public void onError(@Nullable String error) {
                    ExtensionUtils.toast(getApplicationContext(), error, true);
                }

                @Override
                public void onSuccess(@Nullable Void response) {
                    ExtensionUtils.toast(getApplicationContext(), "Request sent successfully", false);
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onComplete() {
                    ExtensionUtils.toast(getApplicationContext(), "Request completed", false);
                }
            });
            finish();
        } else {
            ExtensionUtils.toast(this, "Cannot send request. Please make sure that this request is sent to a tutor instead", true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (prefs.getType().equals(BaseUser.Type.PARENT) ||
                prefs.getType().equals(BaseUser.Type.WARD)) {
            getMenuInflater().inflate(R.menu.user_menu, menu);
            if (menu instanceof MenuBuilder) ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_make_complaints:
                Bundle b = new Bundle(0);
                b.putParcelable(MakeComplaintActivity.EXTRA_TUTOR, getIntent().getParcelableExtra(EXTRA_USER));
                b.putString(MakeComplaintActivity.EXTRA_TUTOR_ID, getIntent().getStringExtra(EXTRA_USER_UID));
                intentTo(MakeComplaintActivity.class, b, false);

                return true;
            case R.id.menu_view_schedule:
                Bundle bundle = new Bundle(0);
                bundle.putParcelable(SchedulesActivity.EXTRA_TUTOR, getIntent().getParcelableExtra(EXTRA_USER));
                bundle.putString(SchedulesActivity.EXTRA_TUTOR_ID, getIntent().getStringExtra(EXTRA_USER_UID));
                intentTo(SchedulesActivity.class, bundle, false);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
