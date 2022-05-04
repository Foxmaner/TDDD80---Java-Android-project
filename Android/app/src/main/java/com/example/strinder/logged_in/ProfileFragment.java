package com.example.strinder.logged_in;

import static android.app.Activity.RESULT_OK;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.example.strinder.R;
import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.User;
import com.example.strinder.logged_in.handlers.PostRecyclerViewAdapter;
import com.example.strinder.private_data.CompletionListener;
import com.squareup.picasso.Picasso;

import java.time.LocalTime;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements CompletionListener {

    private ActivityResultLauncher<Intent> activityLauncher;
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance(final User user) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("account",user);
        profileFragment.setArguments(bundle);

        return profileFragment;
    }

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
            User user =  bundle.getParcelable("account");
            //The "core" part of the profile.

            //First and last name
            TextView firstLastName = v.findViewById(R.id.firstLastName);
            firstLastName.setText(user.getFirstName() == null ? "Unknown" : user.getFirstName());
            firstLastName.append(" ");
            firstLastName.append(user.getLastName() == null ? "Unknown" : user.getLastName());

            //Profile image
            ImageView profileImage = v.findViewById(R.id.profileImage);

            if(user.getPhotoUrl() != null) {
                Picasso.with(getContext())
                        .load(user.getPhotoUrl())
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

            TextView biography = v.findViewById(R.id.biography);
            biography.setText(R.string.biography_base_text);
            //Biography is never null. (It is initially set in database)
            biography.append(user.getBiography());

            //The part that displays the user stats.
            TextView activities = v.findViewById(R.id.amountOfActivities);
            activities.setText(getString(R.string.amountOfActivities));
            activities.append(Integer.toString(user.getPosts().size()));
            TextView hours = v.findViewById(R.id.amountOfHours);
            hours.setText(getString(R.string.amountOfHours));
            hours.append(Integer.toString(sumHours(user.getPosts())));

            TextView likes = v.findViewById(R.id.amountOfLikes);
            likes.setText(getString(R.string.amountOfLikesText));
            likes.append(Integer.toString(sumLikes(user.getPosts())));

            ImageButton cameraButton = v.findViewById(R.id.cameraButton);
            cameraButton.setOnClickListener(this::onCameraClick);
            ImageButton uploadButton = v.findViewById(R.id.uploadButton);
            uploadButton.setOnClickListener(this::onUploadClick);

            //The part that displays all the user's posts.
            RecyclerView recyclerView = v.findViewById(R.id.myPosts);

            PostRecyclerViewAdapter adapter = new PostRecyclerViewAdapter(this.getContext(),
                    user.getPosts());

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

            recyclerView.setAdapter(adapter);


            //Camera intent launcher
            setCameraIntentLauncher();

        }


        return v;
    }

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

    private int sumHours(final List<Post> posts) {
        int sum = 0;
        try {
            for (Post post : posts) {
                String time = post.getTrainingSession().getElapsedTime();
                LocalTime localTime = LocalTime.parse(time);
                if(localTime != null)
                    sum += localTime.getHour();
            }
        }
        catch(NumberFormatException e ){
            e.printStackTrace();
        }

        return sum;
    }

    /** Handles what happens when you press the button with a camera on it */
    private void onCameraClick(View cameraView) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            if(activityLauncher != null)
                activityLauncher.launch(takePictureIntent);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(this.getContext(), "Your phone does not support this action.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Handles what happens when you press the button with a cloud on it */
    private void onUploadClick(View uploadView) {

    }

    private void setCameraIntentLauncher() {
        activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        System.out.println("Opened Camera!");

                    }
                    else {
                        Toast.makeText(this.getContext(),"Failed to open camera. Please try" +
                                "again later", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCompletion() {
    }
}