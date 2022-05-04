package com.example.strinder.logged_in;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.LoggedInActivity;
import com.example.strinder.R;
import com.example.strinder.backend_related.ServerConnection;
import com.example.strinder.backend_related.tables.User;
import com.example.strinder.backend_related.VolleyResponseListener;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddActivityFragment extends Fragment implements View.OnClickListener, VolleyResponseListener<String> {

    private TextInputLayout titleInput;
    private TextInputLayout captionInput;
    private RadioGroup postSportTypeInput;
    private ServerConnection connection;
    private User user;


    public AddActivityFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view =  inflater.inflate(R.layout.fragment_add_activity, container, false);

        Bundle bundle = getArguments();

        if(bundle != null) {
            user =  bundle.getParcelable("account");
        }

        connection = new ServerConnection(view.getContext());
        titleInput = view.findViewById(R.id.textInputLayoutPostTitle);
        captionInput =  view.findViewById(R.id.textInputLayoutPostCaption);
        postSportTypeInput = view.findViewById(R.id.inputAddActivitySport);

        Button addActivityButton = view.findViewById(R.id.addActivityButton);
        addActivityButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        TextView postTitle = titleInput.getEditText();
        TextView postCaption = captionInput.getEditText();
        if(postTitle != null & postCaption != null) {
            String postTitleText = postTitle.getText().toString();
            String postCaptionText = postCaption.getText().toString();
            //int selectedRadioId = postSportTypeInput.getCheckedRadioButtonId();
            //RadioButton selectedButton = postSportTypeInput.findViewById(selectedRadioId);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("title", postTitleText);
                jsonObject.put("caption", postCaptionText);
            } catch (Exception e) {
                e.printStackTrace();
            }
            connection.sendStringJsonRequest("/post/add", jsonObject,
                    Request.Method.POST, user.getAccessToken(), this);

        }
    }

    @Override
    public void onResponse(String response) {
        //TODO Improve this
        //FIXME The code below is just a test
        //TrainingSession
        JSONObject object = new JSONObject();
        try {
            object.put("time",5.5f);
            object.put("postId",1);
            object.put("speedUnit", "km/h");
            object.put("speed",5f);
            object.put("exercise","Running");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        connection.sendStringJsonRequest("/session/set", object, Request.Method.POST, user.getAccessToken(), new VolleyResponseListener<String>() {
            @Override
            public void onResponse(String response) {

                LoggedInActivity activity = (LoggedInActivity) getActivity();

                if(activity != null) {
                    activity.jumpToHome("Added post!");
                }
            }

            @Override
            public void onError(VolleyError error) {
                System.out.println(error.networkResponse);
            }
        });

    }

    @Override
    public void onError(VolleyError error) {
        //TODO Improve this
        System.out.println("Error" + error);
        LoggedInActivity activity = (LoggedInActivity) getActivity();

        if(activity != null) {
            activity.jumpToHome("Failed to add post. Error: " + error);
        }
    }
}