package io.codelabs.digitutor.core;

import android.app.Application;

import com.google.firebase.FirebaseApp;

/**
 * {@link Application} subclass
 */
public class HomeTutorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase SDK
        FirebaseApp app = FirebaseApp.initializeApp(this);
        System.out.println(app.getName());
    }
}
