package io.codelabs.digitutor.view;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import com.google.android.material.snackbar.Snackbar;
import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.data.BaseUser;
import io.codelabs.digitutor.databinding.ActivityComplaintBinding;

import java.util.Objects;

public class ComplaintActivity extends BaseActivity {
    public static final String EXTRA_COMPLAINT = "extra_complaint";
    public static final String EXTRA_COMPLAINT_STATE = "extra_complaint_state";
    private ActivityComplaintBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_complaint);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (getIntent().hasExtra(EXTRA_COMPLAINT)) {
            binding.setComplaint(getIntent().getParcelableExtra(EXTRA_COMPLAINT));
            if (binding.getComplaint() != null) loadTutor();
        }
    }

    private void loadTutor() {
        Snackbar snackbar = Snackbar.make(binding.container, "Fetching Tutor information", Snackbar.LENGTH_LONG);
        FirebaseDataSource.getUser(this, firestore, binding.getComplaint().getTutor(), BaseUser.Type.TUTOR, new AsyncCallback<BaseUser>() {
            @Override
            public void onError(@Nullable String error) {
                snackbar.setText(Objects.requireNonNull(error)).show();
            }

            @Override
            public void onSuccess(@Nullable BaseUser response) {
                if (response != null) {
                    binding.setTutor(response);
                }
            }

            @Override
            public void onStart() {
                snackbar.show();
            }

            @Override
            public void onComplete() {
                if (snackbar.isShown()) snackbar.show();
            }
        });
    }
}
