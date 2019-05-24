package io.codelabs.digitutor.view;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.transition.TransitionManager;

import java.util.Objects;

import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.databinding.ActivityResetBinding;
import io.codelabs.sdk.util.ExtensionUtils;

public class ResetPassword extends BaseActivity {
    private ActivityResetBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset);
        binding.loading.setVisibility(View.GONE);
    }

    public void resetUserPassword(View view) {
        String email = Objects.requireNonNull(binding.email.getText()).toString();

        FirebaseDataSource.resetPassword(this, auth, email, new AsyncCallback<Void>() {
            @Override
            public void onError(@Nullable String error) {
                ExtensionUtils.toast(ResetPassword.this, error, true);
            }

            @Override
            public void onSuccess(@Nullable Void response) {
                ExtensionUtils.toast(ResetPassword.this, "Password reset link sent successfully", true);
                navLogin(null);
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

    public void navLogin(View view) {
        intentTo(LoginActivity.class, true);
    }
}
