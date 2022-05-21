package com.example.strinder.logged_in;

import static android.app.Activity.RESULT_OK;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.storage.FirebaseCompletionListener;
import com.example.strinder.backend_related.storage.FirebaseServices;
import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.User;
import com.example.strinder.logged_in.adapters.PostAdapter;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;


/**
 * This class is a subclass of {@link Fragment Fragment}.
 * This class displays the logged-in {@link User User} object's profile.
 */
public class ProfileFragment extends Fragment implements FirebaseCompletionListener, VolleyResponseListener<String> {

    private ActivityResultLauncher<Intent> cameraActivityLauncher;
    private ActivityResultLauncher<Intent> uploadActivityLauncher;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        Bundle bundle = getArguments();
        if(bundle != null) {
            user =  bundle.getParcelable("account");
            //The "core" part of the profile.
            setCoreDetails(v);
            //The stats (shown below the details part)
            setStats(v);

            ImageButton cameraButton = v.findViewById(R.id.cameraButton);
            cameraButton.setOnClickListener(this::onCameraClick);
            ImageButton uploadButton = v.findViewById(R.id.uploadButton);
            uploadButton.setOnClickListener(this::onUploadClick);

            //The part that displays all the user's posts.
            RecyclerView recyclerView = v.findViewById(R.id.myPosts);

            PostAdapter adapter = new PostAdapter(this.getContext(),
                    user.getPosts(), Collections.singletonList(user), user,this);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

            recyclerView.setAdapter(adapter);


            //Camera intent launcher
            setCameraIntentLauncher();

            //Upload intent launcher
            setUploadIntentLauncher();

        }


        return v;
    }

    /** This method sum the amount of likes on a given {@link List<Post> List<Post>} object.
     *
     * @param posts - the {@link List<Post> List<Post>} object.
     * @return the sum as an integer.
     */
    private int sumLikes(final List<Post> posts) {
        int sum = 0;

        try {
            for (Post post : posts) {
                sum += post.getLikes().size();
            }
        }
        catch(NumberFormatException e ){
            e.printStackTrace();
        }

        return sum;
    }

    /** This method sum the amount of hours exercised on a given {@link List<Post> List<Post>}
     * object.
     *
     * @param posts - the {@link List<Post> List<Post>} object.
     * @return the sum as an integer.
     */
    private float sumHours(final List<Post> posts) {
        float sum = 0;
        try {
            for (Post post : posts) {
                if(post.getTrainingSession() != null) {
                    String time = post.getTrainingSession().getElapsedTime();
                    String[] splitTime = time.split(":");
                    int hours = Integer.parseInt(splitTime[0]);
                    int minutes = Integer.parseInt(splitTime[1]);

                    sum += hours + minutes/ 60f;

                }
            }
        }
        catch(NumberFormatException e ){
            e.printStackTrace();
        }

        return sum;
    }

    /** This method sets the different 'stats' of type {@link View View} objects to its correct
     *  value.
     *
     * @param v - the {@link View View} object given in the
     *            {@link ProfileFragment#onCreateView(LayoutInflater, ViewGroup, Bundle)} method.
     */
    private void setStats(final View v) {
        //The part that displays the user stats.
        TextView activities = v.findViewById(R.id.amountOfActivities);
        activities.setText(getString(R.string.amountOfActivities));
        activities.append(Integer.toString(user.getPosts().size()));

        TextView hours = v.findViewById(R.id.amountOfHours);
        hours.setText(getString(R.string.amountOfHours));
        DecimalFormat df = new DecimalFormat("0.00");
        String hoursString = df.format(sumHours(user.getPosts()));
        hours.append(hoursString);

        TextView likes = v.findViewById(R.id.amountOfLikes);
        likes.setText(getString(R.string.amountOfLikesText));
        likes.append(Integer.toString(sumLikes(user.getPosts())));
    }

    /** This method sets the core details of the profile, that being the different {@link View View}
     * objects, to its correct values.
     * @param v - the {@link View View} object given in the
     *            {@link ProfileFragment#onCreateView(LayoutInflater, ViewGroup, Bundle)} method.
     */
    private void setCoreDetails(final View v) {
        TextView biography = v.findViewById(R.id.biography);
        biography.setText(R.string.biography_base_text);
        //Biography is never null. (It is initially set in database)
        biography.append(user.getBiography());

        //First and last name
        TextView firstLastName = v.findViewById(R.id.firstLastName);
        firstLastName.setText(user.getFirstName());
        firstLastName.append(" ");
        firstLastName.append(user.getLastName());

        ImageView profileImage = v.findViewById(R.id.profileImage);

        if(user.getPhotoUrl() != null) {
            //This ensures that the image always is set to the newly uploaded one. Picasso ignores (by default) identical URLs.
            if(getActivity() != null) {
                Picasso.with(getActivity().getApplicationContext()).
                        invalidate(user.getPhotoUrl());
            }

            Picasso.with(getActivity()).load(user.getPhotoUrl())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(profileImage);
        }

        TextView gender = v.findViewById(R.id.gender);
        gender.setText(R.string.gender_base_text);
        if(user.getGender() != null)
            gender.append(user.getGender());
        else
            gender.append("Unknown");

        TextView birthday = v.findViewById(R.id.birthday);
        birthday.setText(R.string.birthday_base_text);
        if(user.getBirthday() != null)
            birthday.append(user.getBirthday());
        else
            birthday.append("Unknown");

    }

    /** Handles what happens when you press the button with a camera on it.
     *
     * @param cameraView - the {@link ImageButton ImageButton} object's {@link View View}.
     */
    private void onCameraClick(View cameraView) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            if(cameraActivityLauncher != null)
                cameraActivityLauncher.launch(takePictureIntent);

        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this.getContext(), "Your phone does not support this action.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Handles what happens when you press the button with a cloud on it.
     *
     * @param uploadView - the {@link ImageButton ImageButton} object's {@link View View}.
     */
    private void onUploadClick(View uploadView) {
        Intent uploadImageIntent = new Intent();
        try {
            uploadImageIntent.setType("image/*");
            uploadImageIntent.setAction(Intent.ACTION_GET_CONTENT);

            if (uploadActivityLauncher != null) {
                uploadActivityLauncher.launch(uploadImageIntent);
            }
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this.getContext(), "Your phone does not support this action.",
                Toast.LENGTH_SHORT).show();
        }


    }

    /** This defines how the {@link ProfileFragment#onCameraClick(View)} should act. That being,
     * what should happen when the user presses the {@link ImageButton ImageButton}.
     */
    private void setCameraIntentLauncher() {
        cameraActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");

                        if(bitmap != null) {
                            FirebaseServices.getInstance().saveImage(bitmap, user, this);
                        }

                    }
                    else {
                        Toast.makeText(this.getContext(),"Failed to open camera. Please try" +
                                " again later", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** This defines how the {@link ProfileFragment#onUploadClick(View)} should act. That being,
     * what should happen when the user presses the {@link ImageButton ImageButton}.
     */
    private void setUploadIntentLauncher() {
        uploadActivityLauncher = registerForActivityResult(new ActivityResultContracts.
                StartActivityForResult(), (result) -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if(getActivity() != null) {
                    ImageDecoder.Source source = ImageDecoder.createSource(getActivity().
                            getContentResolver(), uri);
                    Bitmap bitmap = null;
                    try {
                        bitmap = ImageDecoder.decodeBitmap(source);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(),"Failed to decode data to bitmap.",
                                Toast.LENGTH_SHORT).show();
                        Log.e("Decode Bitmap Failed","Failed to decode bitmap.");
                    }

                    if(bitmap != null) {

                        FirebaseServices.getInstance().saveImage(bitmap,user,this);
                    }

                }
            }
        });
    }


    @Override
    public void onFinish(StorageMetadata data) {
        if(data != null) {
            StorageReference ref = data.getReference();

            if(ref != null) {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    user.setPhotoUrl(uri.toString());

                    user.uploadData(getContext(), this);
                });
            }

        }
        else {
            Toast.makeText(this.getContext(),"Failed to upload image.",Toast.LENGTH_SHORT).
                    show();
        }


    }

    @Override
    public void onResponse(String response) {
        Log.i("Successfully saved link",
                "Link was uploaded to database");
        Toast.makeText(getContext(),"Successfully saved link in database",
                Toast.LENGTH_SHORT).show();

        getParentFragmentManager().beginTransaction().detach(ProfileFragment.this).commit();

        getParentFragmentManager().beginTransaction().attach(ProfileFragment.this).commit();
    }

    @Override
    public void onError(VolleyError error) {
        ServerConnection connection = new ServerConnection(getContext());
        connection.maybeDoRefresh(error,user);
        Log.e("Failed to save link",
                "Link failed to upload to database");

        Toast.makeText(getContext(),"Failed to save link in database",
                Toast.LENGTH_SHORT).show();
    }

}