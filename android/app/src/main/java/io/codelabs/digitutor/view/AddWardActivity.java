package io.codelabs.digitutor.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import java.util.Objects;

import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource;
import io.codelabs.digitutor.core.datasource.remote.WardCredentials;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.core.util.InputValidator;
import io.codelabs.digitutor.databinding.ActivityAddWardBinding;
import io.codelabs.sdk.glide.GlideApp;
import io.codelabs.sdk.util.ExtensionUtils;

/**
 * Search screen
 */
public class AddWardActivity extends BaseActivity {
    private ActivityAddWardBinding binding;
    private static final int RC_PROFILE = 3;
    // Image url
    private Uri imageUrl = Uri.EMPTY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_ward);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
            startActivityForResult(intent, RC_PROFILE);
        });

    }

    public void addWard(View view) {
        String username = Objects.requireNonNull(binding.username.getText()).toString();
        InputValidator validator = InputValidator.INSTANCE;

        if (imageUrl != null && validator.hasValidInput(username)) {
            FirebaseDataSource.uploadImage(storage, imageUrl, new AsyncCallback<String>() {
                @Override
                public void onError(@Nullable String error) {
                    ExtensionUtils.toast(AddWardActivity.this.getApplicationContext(), error, true);
                }

                @Override
                public void onSuccess(@Nullable String response) {
                    FirebaseDataSource.addWard(firestore, prefs, new WardCredentials(username, response), new AsyncCallback<Void>() {
                        @Override
                        public void onError(@Nullable String error) {
                            ExtensionUtils.toast(AddWardActivity.this.getApplicationContext(), "Your ward was added successfully...", false);
                        }

                        @Override
                        public void onSuccess(@Nullable Void response) {
                            ExtensionUtils.toast(AddWardActivity.this.getApplicationContext(), "Your ward was added successfully...", false);
                        }

                        @Override
                        public void onStart() {
                            ExtensionUtils.toast(AddWardActivity.this.getApplicationContext(), "Registering your ward...", false);
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onComplete() {

                }
            });
            finishAfterTransition();
        } else {
            ExtensionUtils.toast(AddWardActivity.this.getApplicationContext(), "Please add a photo of your ward and his/ her full name", true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PROFILE && resultCode == RESULT_OK) {
            imageUrl = data.getData();

            GlideApp.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .into(binding.profile);
        }
    }
}
