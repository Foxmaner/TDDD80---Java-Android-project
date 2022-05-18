package com.example.strinder.backend_related.storage;

import com.google.firebase.storage.StorageMetadata;

public interface FirebaseCompletionListener {
    void onFinish(final StorageMetadata object);
}
