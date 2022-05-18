package com.example.strinder.backend_related.storage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dropbox.core.v2.users.GetAccountArg;
import com.example.strinder.R;
import com.example.strinder.backend_related.tables.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FirebaseServices {

    private static FirebaseServices instance = null;
    private StorageReference storageRef;

    private FirebaseServices() {

    }

    public static FirebaseServices getInstance() {
        if(instance == null) {
            instance = new FirebaseServices();
        }

        return instance;
    }

    public void initialize(final Activity activity) {
        // Use the application default credentials
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInAnonymously().addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                FirebaseStorage storage = FirebaseStorage.getInstance();

                storageRef = storage.getReference();
            }
        });
    }

    public void saveImage(final Bitmap bitmap, final User user,
                          final FirebaseCompletionListener listener) {
        Thread thread = new Thread(() -> {

            StorageReference imageRef = storageRef.child("images/"+user.getUsername()+".png");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] data = outputStream.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(data);

            uploadTask.addOnFailureListener(exception -> {
                Log.e("Upload failed", "Failed to upload profile image.");

                // Handle unsuccessful uploads
                listener.onFinish(null);
            }).addOnSuccessListener(taskSnapshot -> {
                Log.i("Successful upload", "Successfully uploaded profile image");
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                listener.onFinish(taskSnapshot.getMetadata());
            });

        });

        thread.start();
    }

}

