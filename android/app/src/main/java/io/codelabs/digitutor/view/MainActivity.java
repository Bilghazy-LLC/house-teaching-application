package io.codelabs.digitutor.view;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.data.BaseUser;

public class MainActivity extends BaseActivity {
    private String type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showLoginDialog(View view) {
        if (prefs.isLoggedIn()) {
            intentTo(HomeActivity.class, true);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a login option");
        builder.setItems(R.array.login_options, (dialog, which) -> {
            switch (which) {
                case 0:
                    type = BaseUser.Type.PARENT;
                    break;
                case 1:
                    type = BaseUser.Type.TUTOR;
                    break;
                default:
                    type = BaseUser.Type.WARD;
                    break;
            }

            dialog.dismiss();
            Bundle bundle = new Bundle();
            bundle.putString(LoginActivity.EXTRA_USER_TYPE, type);
            intentTo(LoginActivity.class, bundle, true);
        });
        builder.setCancelable(false);
        builder.setPositiveButton("Dismiss", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
