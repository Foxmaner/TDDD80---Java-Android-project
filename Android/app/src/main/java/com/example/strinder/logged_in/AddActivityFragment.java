package com.example.strinder.logged_in;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
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

import java.io.IOException;

/**
 * This class is a subclass of {@link Fragment Fragment}.
 * This Fragment handles the process of adding posts/activities to the logged in user's account.
 * In order to add a {@link Post Post} object to the logged in User's account and the database, a
 * GPS connection is required.
 */
public class AddActivityFragment extends Fragment implements LocationListener {

    private EditText title, caption,elapsedTime,distance,speed;
    private Spinner activities,speedUnit,distanceUnit;
    private ServerConnection connection;
    private User user;
    private LocationManager locationManager;
    private Location location;
    private Button addPostButton;
    private TextView locationText;
    private Geocoder geocoder;

    //We have to suppress, Android Studio keeps telling us
    //we don't ask for permission in Manifest - but we do.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() != null) {

            Bundle bundle = getArguments();

            if(bundle != null) {
                user = bundle.getParcelable("account");
            }

            locationManager = (LocationManager) getActivity().
                    getSystemService(Context.LOCATION_SERVICE);

            connection = new ServerConnection(getContext());
            requestPermissions();

            geocoder = new Geocoder(getActivity());

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view =  inflater.inflate(R.layout.fragment_add_activity, container, false);

        setFields(view);

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

                                            LoggedInActivity activity = (LoggedInActivity)
                                                    getActivity();

                                            if(activity != null) {
                                                activity.jumpToHome("Your Post Was" +
                                                        " Successfully Added!",user);
                                            }
                                        }

                                        @Override
                                        public void onError(VolleyError error) {
                                            connection.maybeDoRefresh(error,user);
                                            Log.e("Failed to add training session to activity",
                                                    error.toString());

                                            user.getPosts().remove(post);

                                            handleError(post);

                                        }
                                    });
                        }

                        @Override
                        public void onError(VolleyError error) {
                            connection.maybeDoRefresh(error,user);
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

    /** This method is executed when the request to set a session for a {@link Post Post} object
     * fails.
     * @param post - the {@link Post Post} object.
     */
    private void handleError(final Post post) {

        //We need to remove the Post we created.
        connection.sendStringJsonRequest("/del/post/"+
                        post.getId(),new JSONObject(),
                Request.Method.DELETE,
                user.getAccessToken(),
                new VolleyResponseListener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.i("Internal Server Error",
                                "Failed to add exercise data -> " +
                                        "had to remove the entire post.");

                        Toast.makeText(getContext(),"Internal Server Error." +
                                " Failed to set exercise data", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.i("Internal Server Error",
                                "Failed to add exercise data but failed to delete post");
                        Toast.makeText(getContext(),"Internal Server Error, Remove Created Post" +
                                "Manually",Toast.LENGTH_LONG).show();
                    }
                }
        );
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
                postElapsedTime.matches("^[0-9][0-9]:[0-5][0-9]$");
    }


    private static boolean hasPermission(Context context, String... permissions) {
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
        String address;

        try {
            address = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(),1).
                    get(0).getAddressLine(0);

            locationText.setText(String.format("You are located at: %s", address));
        }
        catch (IOException e) {
            Log.e("Geocode Error", "Geocoder failed to get address");
            e.printStackTrace();
        }

        addPostButton.setEnabled(true);

        this.location = location;
    }

    //Suppress because we have the permissions, android studio thinks otherwise...
    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        if (hasPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.i("GPS Resume", "Resume call");
            //GPS_PROVIDER or NETWORK_PROVIDER
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 450,
                    1, this);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (hasPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.i("GPS Pause", "Pause call");
            locationManager.removeUpdates(this);
        }
    }

    private void requestPermissions() {
        //Ask for permission if we don't have it.
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        ActivityResultLauncher<String[]> requestPermissions = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                requestedPermissions -> requestedPermissions.forEach((key, value) -> {
                    if (!value) {
                        //If we do not have permission, show a dialog.
                        Toast.makeText(getContext(), "Location Permissions Required. \n" +
                                        "Please Activate It In The App Settings",
                                Toast.LENGTH_SHORT).show();
                    }
        }));

        if(!hasPermission(getActivity(), permissions)) {
            //If we do not, request it.
            requestPermissions.launch(permissions);
        }
        else {
            //If we have permission, show a dialog.
            Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        }

    }

    /** Sets all the {@link View View} fields and settings related to them.
     *
     * @param view - the {@link View View} object given in
     * {@link AddActivityFragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    private void setFields(final View view) {
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

        locationText = view.findViewById(R.id.locationText);
    }



}