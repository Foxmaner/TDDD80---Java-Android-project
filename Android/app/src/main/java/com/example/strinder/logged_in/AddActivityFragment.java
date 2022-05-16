package com.example.strinder.logged_in;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.LoggedInActivity;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.TrainingSession;
import com.example.strinder.backend_related.tables.User;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This Fragment handles the process of adding posts/activities to the logged in user's account.
 * Use the {@link AddActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddActivityFragment extends Fragment implements LocationListener {

    private static final int REQUEST = 112;
    private EditText title, caption,elapsedTime,distance,speed;
    private Spinner activities,speedUnit,distanceUnit;
    private ServerConnection connection;
    private User user;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private LocationManager locationManager;
    private Location location;
    private Button addPostButton;


    public AddActivityFragment() {
        // Required empty public constructor
    }


    public static AddActivityFragment newInstance(final User user) {
        AddActivityFragment fragment = new AddActivityFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("account",user);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() != null) {
            locationManager = (LocationManager) getActivity().
                    getSystemService(Context.LOCATION_SERVICE);

            //Ask for permission if we don't have it.
            String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION};

            if (!hasPermissions(getActivity(), PERMISSIONS)) {
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, REQUEST );

            }
            else {
                Log.i("Permission","Permission was not granted.");
                //TODO Fix here
            }

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view =  inflater.inflate(R.layout.fragment_add_activity, container, false);


        connection = new ServerConnection(getContext());

        Bundle bundle = getArguments();

        if(bundle != null) {
            user =  bundle.getParcelable("account");

            //Get the fields.
            title = view.findViewById(R.id.postTitle);
            caption = view.findViewById(R.id.postCaption);
            activities = view.findViewById(R.id.postActivity);
            elapsedTime = view.findViewById(R.id.postElapsedTime);
            distance = view.findViewById(R.id.postDistance);
            distanceUnit = view.findViewById(R.id.postDistanceUnit);
            speed = view.findViewById(R.id.postSpeed);
            speedUnit = view.findViewById(R.id.postSpeedUnit);

            addPostButton = view.findViewById(R.id.addPostButton);
            addPostButton.setEnabled(false);
            addPostButton.setOnClickListener(this::onSubmit);

            ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.activities, R.layout.spinner_item);

            ArrayAdapter<CharSequence> speedUnitAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.speedUnits, R.layout.spinner_item);

            ArrayAdapter<CharSequence> distanceUnitAdapter = ArrayAdapter.
                    createFromResource(getContext(), R.array.distanceUnits, R.layout.spinner_item);

            //set the view for the Drop down list
            activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            speedUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            distanceUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //set the ArrayAdapter to the spinner
            activities.setAdapter(activityAdapter);
            distanceUnit.setAdapter(distanceUnitAdapter);
            speedUnit.setAdapter(speedUnitAdapter);

        }


        return view;
    }
    /** Is executed when the user presses the "Save Post" button.
     * @param view - the View object.
     * */
    private void onSubmit(View view) {
        String postTitle = title.getText().toString();
        String postCaption = caption.getText().toString();
        String postActivity = activities.getSelectedItem().toString();
        String postDistance = distance.getText().toString();
        String postDistanceUnit = distanceUnit.getSelectedItem().toString();
        String postSpeed = speed.getText().toString();
        String postSpeedUnit = speedUnit.getSelectedItem().toString();
        String postElapsedTime = elapsedTime.getText().toString();

        if(isDataFormattedCorrect(postTitle,postCaption,postActivity,postDistance,postDistanceUnit,
                postSpeed,postSpeedUnit,postElapsedTime)) {

            //Get location data
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            //The data is formatted correctly
            JSONObject object = new JSONObject();
            try {
                object.put("title",postTitle);
                object.put("caption",postCaption);
                object.put("latitude",latitude);
                object.put("longitude",longitude);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            connection.sendStringJsonRequest("/post/add", object,
                    Request.Method.POST, user.getAccessToken(), new VolleyResponseListener<String>() {

                        @Override
                        public void onResponse(String response) {
                            Gson gson = new Gson();
                            Post post = gson.fromJson(response,Post.class);
                            user.getPosts().add(post);

                            //TrainingSession
                            JSONObject object = new JSONObject();
                            try {
                                object.put("time",postElapsedTime);
                                object.put("postId",post.getId());
                                object.put("speedUnit", postSpeedUnit);
                                object.put("speed",postSpeed);
                                object.put("exercise",postActivity);
                                object.put("distance",postDistance);
                                object.put("distanceUnit",postDistanceUnit);

                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }

                            connection.sendStringJsonRequest("/session/set", object,
                                    Request.Method.POST, user.getAccessToken(),
                                    new VolleyResponseListener<String>() {

                                        @Override
                                        public void onResponse(String response) {
                                            TrainingSession session = gson.fromJson(response,
                                                    TrainingSession.class);
                                            post.setTrainingSession(session);

                                            //FIXME Fix this later on? Make a listener or something.
                                            LoggedInActivity activity = (LoggedInActivity) getActivity();

                                            if(activity != null) {
                                                activity.jumpToHome("Your Post Was Successfully" +
                                                        " Added!");
                                            }
                                        }

                                        @Override
                                        public void onError(VolleyError error) {
                                            Log.e("Adding activity", error.toString());
                                            user.getPosts().remove(post);
                                            Toast.makeText(getContext(),"Failed to add activity" +
                                                            "stats to the post.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        @Override
                        public void onError(VolleyError error) {
                            Log.e("Adding Post", error.toString());
                            Toast.makeText(getContext(),"Failed To Add Post",
                                    Toast.LENGTH_SHORT).show();
                        }

                    });

        }
        else {
            Log.e("Form Fields Error", "Fields are not correctly formatted," +
                    " thus a post could not be made");
            Toast.makeText(getContext(),"The form is not formatted correctly, please alter" +
                    " the fields and try again!",Toast.LENGTH_SHORT).show();
        }

    }

    /** Returns if the data is correctly formatted or not
     *
     * @param postTitle - the title as a String
     * @param postCaption - the caption as a String
     * @param postActivity - the activity as a String
     * @param postDistance - the distance as a String
     * @param postDistanceUnit - the distance unit as a String
     * @param postSpeed - the speed as a String
     * @param postSpeedUnit - the speed unit as a String
     * @param postElapsedTime - the elapsed time as a String
     * @return true or false depending on if they are correctly formatted or not.
     */
    private boolean isDataFormattedCorrect(final String postTitle, final String postCaption,
                                           final String postActivity,
                                           final String postDistance, final String postDistanceUnit,
                                           final String postSpeed, final String postSpeedUnit,
                                           final String postElapsedTime) {

        return !postTitle.isEmpty() && !postCaption.isEmpty() && !postActivity.isEmpty()
                && !postDistance.isEmpty() && !postDistanceUnit.isEmpty() && !postSpeed.isEmpty()
                && !postSpeedUnit.isEmpty() && !postElapsedTime.isEmpty() &&
                postElapsedTime.matches("^\\d{2}:\\d{2}$");
    }


    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("GPS","Checking position of device.");
        addPostButton.setEnabled(true);

        this.location = location;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400,
                    1, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.removeUpdates(this);
        }
    }



}