package com.example.strinder.backend_related.storage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.strinder.backend_related.tables.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/** This class handles all the communication with Firebase, which we use to store uploaded images.
 */
public class FirebaseServices {

    private static FirebaseServices instance = null;
    private StorageReference storageRef;

    /** Initializes a FirebaseServices object. */
    private FirebaseServices() {

    }

    public static FirebaseServices getInstance() {
        if(instance == null) {
            instance = new FirebaseServices();
        }

        return instance;
    }

    /** Initialize the instance and sign into Firebase anonymously.
     *
     * @param activity - an {@link Activity Activity} object.
     */
    public void initialize(final Activity activity) {
        // Use the application default credentials
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInAnonymously().addOnCompleteListener(activity, task -> {

            FirebaseStorage storage = FirebaseStorage.getInstance();

            storageRef = storage.getReference();
        });
    }

    /** Saves an image in the database. It is named after the user's username and the image data
     * is passed as a {@link Bitmap Bitmap} object.
     * @param bitmap - the {@link Bitmap Bitmap} data.
     * @param user - the {@link User User} object.
     * @param listener - a {@link FirebaseCompletionListener FirebaseCompletionListener} that
     *                 allows us to execute code after the method. This is mainly needed because
     *                 the method runs on a separate thread.
     */
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

