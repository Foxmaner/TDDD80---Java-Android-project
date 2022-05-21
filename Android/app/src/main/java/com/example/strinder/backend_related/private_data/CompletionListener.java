package com.example.strinder.backend_related.private_data;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.List;

/** This is used in combination with the GoogleServices class. This should be implemented by
 * classes that are interested in when the code that is sent into
 * {@link GoogleServices#requestPrivateData(GoogleSignInAccount, List, String, ThreadCode,
 * Object, CompletionListener)} is done executing.
 */
@FunctionalInterface
public interface CompletionListener {
    /** Is run after the last snippet of code that is sent into the
     * {@link GoogleServices#requestPrivateData(GoogleSignInAccount, List, String, ThreadCode,
     * Object, CompletionListener)} method.
     */
    void onCompletion();
}
