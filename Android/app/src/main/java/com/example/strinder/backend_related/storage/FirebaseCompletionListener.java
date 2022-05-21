package com.example.strinder.backend_related.storage;

import com.google.firebase.storage.StorageMetadata;

/** This interface is used in combination with the
 * {@link com.example.strinder.backend_related.storage.FirebaseServices FirebaseServices}.
 * The only method in this functional interface is executed once the Firebase code is done
 * executing.
 */
@FunctionalInterface
public interface FirebaseCompletionListener {
    /** Executes once the code in {@link com.example.strinder.backend_related.storage.FirebaseServices}
     * is done executing.
     * @param object - a {@link StorageMetadata} object that gives relevant information
     *              to the action performed.
     */
    void onFinish(final StorageMetadata object);
}
