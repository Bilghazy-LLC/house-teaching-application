package io.codelabs.digitutor.core.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.datasource.local.UserSharedPreferences;

/**
 * Base class for all {@link android.app.Activity}s
 */
public abstract class BaseActivity extends io.codelabs.sdk.view.BaseActivity {
    // Get instance of the user's shared preferences
    public UserSharedPreferences prefs;
    public FirebaseAuth auth = FirebaseAuth.getInstance();
    public FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    public StorageReference storage = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = UserSharedPreferences.getInstance(this);
    }

    /**
     * Navigate to other activities
     *
     * @param target Activity to navigate to
     */
    public void intentTo(Class<? extends Activity> target) {
        startActivity(new Intent(getApplicationContext(), target));
    }

    public void intentTo(Class<? extends Activity> target, boolean finish) {
        startActivity(new Intent(getApplicationContext(), target));
        if (finish) finishAfterTransition();
    }

    public void intentTo(Class<? extends Activity> target, Bundle bundle, boolean finish) {
        Intent intent = new Intent(getApplicationContext(), target);
        intent.putExtras(bundle);
        startActivity(intent);
        if (finish) finishAfterTransition();
    }

    /**
     * Fragment Helper
     */
    public void addFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, fragment, fragment.getClass().getName()).commitNow();
    }
}
