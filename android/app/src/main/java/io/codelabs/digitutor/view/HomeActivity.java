package io.codelabs.digitutor.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.core.util.Constants;
import io.codelabs.digitutor.data.BaseUser;
import io.codelabs.digitutor.data.model.Parent;
import io.codelabs.digitutor.databinding.ActivityHomeBinding;
import io.codelabs.digitutor.view.fragment.*;
import io.codelabs.digitutor.view.kotlin.*;
import io.codelabs.sdk.glide.GlideApp;
import io.codelabs.sdk.util.ExtensionUtils;
import io.codelabs.widget.CircularImageView;

import java.util.HashMap;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Home activity class
 */
public class HomeActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        setSupportActionBar(binding.bottomAppBar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, R.string.open_drawer, R.string.close_drawer);
        binding.drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Sync navigation view state
        setupHeaderView();
        binding.navView.setNavigationItemSelectedListener(this);

        // Set FAB icon & click action
        if (BaseUser.Type.PARENT.equals(prefs.getType())) {
            binding.toolbar.setTitle(getString(R.string.tutors));
            addFragment(new TutorsFragment());
            binding.fab.setImageDrawable(getResources().getDrawable(R.drawable.twotone_group_add_24px));
            binding.fab.setOnClickListener(v -> intentTo(AddWardActivity.class));
        } else {
            binding.toolbar.setTitle(getString(R.string.clients));
            addFragment(new ClientsFragment());
            binding.fab.setImageDrawable(getResources().getDrawable(R.drawable.twotone_assignment_24px));
            binding.fab.setOnClickListener(v -> {
                intentTo(AssignmentActivity.class);
            });
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult().getToken();
                sendRegistrationToServer(token);
            } else {
                ExtensionUtils.debugLog(getApplicationContext(), "Token could not be retrieved");
            }
        });

        FirebaseDataSource.getCurrentUser(this, firestore, prefs, new AsyncCallback<BaseUser>() {
            @Override
            public void onError(@Nullable String error) {

            }

            @Override
            public void onSuccess(@Nullable BaseUser response) {
                if (response instanceof Parent) {
                    if (((Parent) response).getWards().isEmpty()) showDialog();
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

    private void setupHeaderView() {
        // Get live data from database
        if (prefs.getType() != null) {
            getUser();
        }
    }

    private void sendRegistrationToServer(@Nullable String token) {
        if (prefs.isLoggedIn()) {
            String type = prefs.getType();
            ExtensionUtils.debugLog(this, token);

            // Create a map of the new token and time updated
            HashMap<String, Object> hashMap = new HashMap<>(0);
            hashMap.put("token", token);
            hashMap.put("updatedAt", System.currentTimeMillis());

            // Send data to the database server
            try {
                firestore.collection(type.equals(BaseUser.Type.PARENT) ? Constants.PARENTS : Constants.TUTORS)
                        .document(prefs.getKey())
                        .update(hashMap)
                        .addOnCompleteListener(task -> ExtensionUtils.debugLog(getApplicationContext(), "Token updated : " + token))
                        .addOnFailureListener(e -> ExtensionUtils.debugLog(getApplicationContext(), e.getLocalizedMessage()));
            } catch (Exception e) {
                ExtensionUtils.debugLog(getApplicationContext(), e.getLocalizedMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUser();
    }

    private void getUser() {
        // Get fields from header view
        View headerView = binding.navView.getHeaderView(0);
        CircularImageView avatar = headerView.findViewById(R.id.header_avatar);
        TextView username = headerView.findViewById(R.id.header_username);
        TextView type = headerView.findViewById(R.id.header_user_type);

        username.setText(prefs.getKey());
        type.setText(String.format("Logged in as: %s", prefs.getType()));

        // Get user information after some delay. this is to help resolve the issue of not finding the user's key in time
        try {
            FirebaseDataSource.getCurrentUser(this, firestore, prefs, new AsyncCallback<BaseUser>() {
                @Override
                public void onError(@Nullable String error) {
                    ExtensionUtils.toast(HomeActivity.this, error, true);
                }

                @Override
                public void onSuccess(@Nullable BaseUser response) {
                    if (response == null) {
                        ExtensionUtils.toast(HomeActivity.this, "Cannot find this user. PLease re-authenticate this account", true);
                        return;
                    }

                    // Load user's profile image with Glide
                    GlideApp.with(HomeActivity.this)
                            .load(response.getAvatar() == null || TextUtils.isEmpty(response.getAvatar()) ? Constants.DEFAULT_AVATAR_URL : response.getAvatar())
                            .circleCrop()
                            .placeholder(R.drawable.avatar_placeholder)
                            .error(R.drawable.ic_player)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .transition(withCrossFade())
                            .into(avatar);

                    username.setText(response.getName());
                    type.setText(/*String.format("Logged in as: %s", response.getType().toLowerCase())*/ response.getEmail());
                    ExtensionUtils.debugLog(HomeActivity.this, response);

                    Bundle bundle = new Bundle(0);
                    bundle.putParcelable(UserActivity.EXTRA_USER, response);
                    bundle.putString(UserActivity.EXTRA_USER_TYPE, response.getType());
                    headerView.setOnClickListener(v -> intentTo(UserActivity.class, bundle, false));
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onComplete() {

                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setTitle("Almost done...")
                .setMessage("The final step here is for you to add at least one ward. Tap ok to get started.")
                .setCancelable(true)
                .setPositiveButton("Add ward", (dialog1, which) -> {
                    dialog1.dismiss();
                    intentTo(AddWardActivity.class);
                })
                .setNegativeButton("Dismiss", (dialog1, which) -> dialog1.dismiss())
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (prefs.isLoggedIn()) {

            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ExtensionUtils.debugLog(HomeActivity.this, task.getResult() != null ? task.getResult().getToken() : "Token was null");
                    if (task.getResult() != null) sendRegistrationToServer(task.getResult().getToken());
                } else {
                    ExtensionUtils.debugLog(HomeActivity.this, "unable to get token");
                }
            }).addOnFailureListener(e -> ExtensionUtils.debugLog(HomeActivity.this, e.getLocalizedMessage()));
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(prefs.isLoggedIn() && prefs.getType().equals(BaseUser.Type.PARENT) ? R.menu.parent_bottom_bar_menu : R.menu.tutor_bottom_bar_menu, menu);
            if (menu instanceof MenuBuilder) ((MenuBuilder) menu).setOptionalIconsVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                intentTo(SearchActivity.class);
                break;
            case R.id.menu_tutor_requests:
                binding.toolbar.setTitle(getString(R.string.requests));
                addFragment(new RequestsFragment());
                break;
            case R.id.menu_tutor_feedback:
                binding.toolbar.setTitle(getString(R.string.feedback));
                addFragment(new FeedbackFragment());
                break;
            case R.id.menu_view_schedule:
                intentTo(SchedulesActivity.class);
                break;
            case R.id.menu_view_timetable:
                intentTo(TimeTableActivity.class);
                break;
            case R.id.menu_complaints:
                binding.toolbar.setTitle(getString(R.string.complaints));
                addFragment(new ComplaintsFragment());
                break;
            case R.id.menu_subjects:
                intentTo(AddSubjectActivity.class);
                break;
            case R.id.menu_make_complaints:
                intentTo(MakeComplaintActivity.class);
                break;
            case R.id.menu_add_days:
                intentTo(AvailableDaysActivity.class);
                break;
            case R.id.menu_assignments:
                intentTo(WardAssignmentActivity.class);
                break;
            case R.id.menu_students:
                Bundle bundle = new Bundle(0);
//                bundle.putString(WardsActivity.WARD_EXTRA, );
                intentTo(WardsActivity.class, bundle, false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_logout:
                Snackbar.make(binding.drawer, getString(R.string.logout), Snackbar.LENGTH_LONG).setAction("Logout", v -> {
                    auth.signOut();
                    prefs.logout();
                    intentTo(MainActivity.class, true);
                }).show();
                break;
            case R.id.menu_home:
                if (BaseUser.Type.PARENT.equals(prefs.getType())) {
                    binding.toolbar.setTitle(getString(R.string.tutors));
                    addFragment(new TutorsFragment());
                } else {
                    binding.toolbar.setTitle(getString(R.string.clients));
                    addFragment(new ClientsFragment());
                }
                break;
        }

        binding.drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }
}
