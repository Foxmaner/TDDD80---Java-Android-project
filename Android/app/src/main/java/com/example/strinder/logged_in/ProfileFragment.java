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
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.storage.DbxCompletionListener;
import com.example.strinder.backend_related.storage.DropBoxServices;
import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.User;
import com.example.strinder.logged_in.adapters.PostAdapter;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements DbxCompletionListener,
        VolleyResponseListener<String> {

    private ActivityResultLauncher<Intent> cameraActivityLauncher;
    private ActivityResultLauncher<Intent> uploadActivityLauncher;
    private User user;

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
                    user.getPosts(), Collections.singletonList(user), user,getParentFragmentManager());

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

    private float sumHours(final List<Post> posts) {
        float sum = 0;
        try {
            for (Post post : posts) {
                String time = post.getTrainingSession().getElapsedTime();
                LocalTime localTime = LocalTime.parse(time);
                if(localTime != null)
                    sum += localTime.getHour() + localTime.getMinute() / 60f;
            }
        }
        catch(NumberFormatException e ){
            e.printStackTrace();
        }

        return sum;
    }

    private void setStats(final View v) {
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
        DecimalFormat df = new DecimalFormat("0.00");
        String hoursString = df.format(sumHours(user.getPosts()));
        hours.append(hoursString);

        TextView likes = v.findViewById(R.id.amountOfLikes);
        likes.setText(getString(R.string.amountOfLikesText));
        likes.append(Integer.toString(sumLikes(user.getPosts())));
    }

    private void setCoreDetails(final View v) {
        //First and last name
        TextView firstLastName = v.findViewById(R.id.firstLastName);
        firstLastName.setText(user.getFirstName() == null ? "Unknown" : user.getFirstName());
        firstLastName.append(" ");
        firstLastName.append(user.getLastName() == null ? "Unknown" : user.getLastName());

        ImageView profileImage = v.findViewById(R.id.profileImage);

        if(user.getPhotoUrl() != null) {
            //This ensures that the image always is set to the newly uploaded one. Picasso ignores (by default) identical URLs.
            if(getActivity() != null) {
                Picasso.with(getActivity().getApplicationContext()).
                        invalidate("https://www.dropbox.com/s/g3ybnjebb26s51t/"+
                                user.getUsername()+".png?raw=1");
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

    /** Handles what happens when you press the button with a camera on it */
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

    /** Handles what happens when you press the button with a cloud on it */
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

    private void setCameraIntentLauncher() {
        cameraActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        //Convert to InputStream
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                        byte[] bitMapData = bos.toByteArray();
                        ByteArrayInputStream bs = new ByteArrayInputStream(bitMapData);

                        DropBoxServices.getInstance().saveImage(bs,user,this);

                    }
                    else {
                        Toast.makeText(this.getContext(),"Failed to open camera. Please try" +
                                "again later", Toast.LENGTH_SHORT).show();
                    }
                });
    }

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
                        //Convert to InputStream
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                        byte[] bitMapData = bos.toByteArray();
                        ByteArrayInputStream bs = new ByteArrayInputStream(bitMapData);

                        DropBoxServices.getInstance().saveImage(bs, user, this);
                    }

                }
            }
        });
    }



    @Override
    public void onFinish(Object object) {

        Boolean wasUploaded = (Boolean)object;

        if(wasUploaded) {
            DropBoxServices.getInstance().getLinkToImage(user, result -> {
                String response = (String) result;
                if(response != null) {

                    user.setPhotoUrl(response);

                    user.uploadData(getContext(), this);
                }
                else {
                    Toast.makeText(getContext(),"Could not find path to the uploaded image.",
                            Toast.LENGTH_SHORT).show();
                }

            });
        }
        else {
            Toast.makeText(this.getContext(),"Failed to upload image.",Toast.LENGTH_SHORT).
                    show();
        }


    }

    @Override
    public void onResponse(String response) {
        Log.i("Upload Success", "Image was successfully uploaded");
        getParentFragmentManager().beginTransaction().
                replace(R.id.loggedInView,ProfileFragment.newInstance(user)).commit();
    }

    @Override
    public void onError(VolleyError error) {
        Log.e("Upload Image Error", error.toString());
        Toast.makeText(getContext(),"Failed to upload image to " +
                "database",Toast.LENGTH_SHORT).show();
    }
}