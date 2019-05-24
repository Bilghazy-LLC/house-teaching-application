package io.codelabs.digitutor.core.util;

import androidx.annotation.Nullable;

/**
 * Handles callback results for any async process
 *
 * @param <Type> The required type for the process
 */
public interface AsyncCallback<Type> {

    void onError(@Nullable String error);

    void onSuccess(@Nullable Type response);

    void onStart();

    void onComplete();
}
