package io.codelabs.digitutor.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.transition.TransitionManager;
import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource;
import io.codelabs.digitutor.core.datasource.remote.LoginCredentials;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.core.util.InputValidator;
import io.codelabs.digitutor.data.BaseUser;
import io.codelabs.digitutor.databinding.ActivityRegisterBinding;
import io.codelabs.sdk.glide.GlideApp;
import io.codelabs.sdk.util.ExtensionUtils;

import java.util.Objects;

public class RegisterActivity extends BaseActivity {
    public static final String EXTRA_USER_TYPE = "EXTRA_USER_TYPE";
    private static final int RC_PROFILE = 3;
    private ActivityRegisterBinding binding;

    // Image url
    private Uri imageUrl = Uri.EMPTY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);

        binding.loading.setVisibility(View.GONE);

        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
            startActivityForResult(intent, RC_PROFILE);
        });
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

    private boolean isUploadProfile = true;

    public void createUser(View view) {
        String username = Objects.requireNonNull(binding.username.getText()).toString();
        String email = Objects.requireNonNull(binding.email.getText()).toString();
        String password = Objects.requireNonNull(binding.password.getText()).toString();

        InputValidator validator = InputValidator.INSTANCE;
        if (imageUrl != null && validator.isValidEmail(email) && validator.hasValidInput(username, password)) {
            FirebaseDataSource.uploadImage(storage, imageUrl, new AsyncCallback<String>() {
                @Override
                public void onError(@Nullable String error) {
                    ExtensionUtils.toast(RegisterActivity.this, error, true);
                }

                @Override
                public void onSuccess(@Nullable String response) {
                    // Create new user with profile information added
                    String type = getIntent().hasExtra(EXTRA_USER_TYPE) ? getIntent().getStringExtra(EXTRA_USER_TYPE) : BaseUser.Type.PARENT;
                    FirebaseDataSource.createUser(RegisterActivity.this, auth, firestore, new LoginCredentials(email, password, type),
                            type, username, new AsyncCallback<Void>() {
                                @Override
                                public void onError(@Nullable String error) {
                                    ExtensionUtils.toast(RegisterActivity.this, error, true);
                                }

                                @Override
                                public void onSuccess(@Nullable Void res) {
                                    ExtensionUtils.showConfirmationToast(RegisterActivity.this, null, username, getString(R.string.app_logged_in_as));

                                    // Store user locally
                                    String type = getIntent().hasExtra(EXTRA_USER_TYPE) ? getIntent().getStringExtra(EXTRA_USER_TYPE) : BaseUser.Type.PARENT;
                                    prefs.login(auth.getUid(), type);
                                    isUploadProfile = false;
                                    FirebaseDataSource.updateUserAvatar(RegisterActivity.this, firestore, type, auth.getUid(), response, isUploadProfile ? this : new AsyncCallback<Void>() {
                                        @Override
                                        public void onError(@Nullable String error) {
                                            ExtensionUtils.toast(RegisterActivity.this, error, true);
                                        }

                                        @Override
                                        public void onSuccess(@Nullable Void response) {
                                            // Store user locally
                                            prefs.login(auth.getUid(), getIntent().hasExtra(EXTRA_USER_TYPE) ? getIntent().getStringExtra(EXTRA_USER_TYPE) : BaseUser.Type.PARENT);

                                            new Handler().postDelayed(() -> {
                                                TransitionManager.beginDelayedTransition(binding.container);
                                                binding.loading.setVisibility(View.GONE);
                                                binding.content.setVisibility(View.VISIBLE);

                                                ExtensionUtils.showConfirmationToast(RegisterActivity.this, response, username, getString(R.string.app_logged_in_as));
                                                ExtensionUtils.debugLog(getApplicationContext(), "Current User UID: " + auth.getUid());
                                                intentTo(HomeActivity.class, true);
                                            }, 2000);
                                        }

                                        @Override
                                        public void onStart() {
                                            TransitionManager.beginDelayedTransition(binding.container);
                                            binding.loading.setVisibility(View.VISIBLE);
                                            binding.content.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onComplete() {
                                            TransitionManager.beginDelayedTransition(binding.container);
                                            binding.loading.setVisibility(View.GONE);
                                            binding.content.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }

                                @Override
                                public void onStart() {
                                    TransitionManager.beginDelayedTransition(binding.container);
                                    binding.loading.setVisibility(View.VISIBLE);
                                    binding.content.setVisibility(View.GONE);
                                }

                                @Override
                                public void onComplete() {
                                    TransitionManager.beginDelayedTransition(binding.container);
                                    binding.loading.setVisibility(View.GONE);
                                    binding.content.setVisibility(View.VISIBLE);
                                }
                            });
                }

                @Override
                public void onStart() {
                    TransitionManager.beginDelayedTransition(binding.container);
                    binding.loading.setVisibility(View.VISIBLE);
                    binding.content.setVisibility(View.GONE);
                }

                @Override
                public void onComplete() {
                    TransitionManager.beginDelayedTransition(binding.container);
                    binding.loading.setVisibility(View.GONE);
                    binding.content.setVisibility(View.VISIBLE);
                }
            });
        } else {
            FirebaseDataSource.createUser(this, auth, firestore, new LoginCredentials(email, password, getIntent().hasExtra(EXTRA_USER_TYPE) ? getIntent().getStringExtra(EXTRA_USER_TYPE) : BaseUser.Type.PARENT),
                    getIntent().hasExtra(EXTRA_USER_TYPE) ? getIntent().getStringExtra(EXTRA_USER_TYPE) : BaseUser.Type.PARENT, username,
                    new AsyncCallback<Void>() {
                        @Override
                        public void onError(@Nullable String error) {
                            ExtensionUtils.toast(RegisterActivity.this, error, true);
                        }

                        @Override
                        public void onSuccess(@Nullable Void response) {
                            ExtensionUtils.showConfirmationToast(RegisterActivity.this, null, username, getString(R.string.app_logged_in_as));

                            // Store user locally
                            prefs.login(auth.getUid(), getIntent().hasExtra(EXTRA_USER_TYPE) ? getIntent().getStringExtra(EXTRA_USER_TYPE) : BaseUser.Type.PARENT);

                            // Navigate to home screen
                            intentTo(HomeActivity.class, true);
                        }

                        @Override
                        public void onStart() {
                            TransitionManager.beginDelayedTransition(binding.container);
                            binding.loading.setVisibility(View.VISIBLE);
                            binding.content.setVisibility(View.GONE);
                        }

                        @Override
                        public void onComplete() {
                            TransitionManager.beginDelayedTransition(binding.container);
                            binding.loading.setVisibility(View.GONE);
                            binding.content.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    public void navLogin(View view) {
        intentTo(LoginActivity.class, true);
    }

    @Override
    public void onBackPressed() {
        navLogin(null);
    }
}
