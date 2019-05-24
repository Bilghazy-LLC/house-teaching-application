package io.codelabs.digitutor.core.datasource.remote;

import android.util.Patterns;

/**
 * Login credentials class
 */
public class LoginCredentials {
    private String email, password, type;

    public LoginCredentials(String email, String password, String type) {
        this.email = email;
        this.password = password;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

    boolean validate() {
        if (email == null || password == null) return false;
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length() > 5;
    }
}
