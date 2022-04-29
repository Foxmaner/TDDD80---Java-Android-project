package com.example.strinder.logged_in;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.ServerConnection;
import com.example.strinder.backend_related.User;
import com.example.strinder.backend_related.VolleyResponseListener;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddActivityFragment extends Fragment implements View.OnClickListener, VolleyResponseListener {

    private TextInputLayout titleInput;
    private TextInputLayout captionInput;
    private RadioGroup postSportTypeInput;
    private ServerConnection connection;
    private String token;
    private User user;


    public AddActivityFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AddActivityFragment newInstance(final User user, final String token) {
        AddActivityFragment fragment = new AddActivityFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("account",user);
        bundle.putString("token",token);
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
            token = bundle.getString("token");
        }

        connection = new ServerConnection(view.getContext());
        titleInput = (TextInputLayout) view.findViewById(R.id.textInputLayoutPostTitle);
        captionInput = (TextInputLayout) view.findViewById(R.id.textInputLayoutPostCaption);
        postSportTypeInput = (RadioGroup) view.findViewById(R.id.inputAddActivitySport);

        Button addActivityButton = (Button) view.findViewById(R.id.addActivityButton);
        addActivityButton.setOnClickListener((View.OnClickListener) this);

        return view;
    }

    @Override
    public void onClick(View view) {
        System.out.println("cooler");
        String postTitle = titleInput.getEditText().getText().toString();
        String postCaption = captionInput.getEditText().getText().toString();
        int selectedRadioId = postSportTypeInput.getCheckedRadioButtonId();
        RadioButton selectedButton = (RadioButton) postSportTypeInput.findViewById(selectedRadioId);
        String postSport = selectedButton.getText().toString();
        System.out.println(postSport);




        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", postTitle);
            jsonObject.put("caption", postCaption);
        }catch(Exception e){
            System.out.println(e);
        }
        connection.sendStringJsonRequest("/add/" + user.getId(), jsonObject, Request.Method.POST, token, this);


    }

    @Override
    public void onResponse(Object response) {
        System.out.println("Success" + response);
    }

    @Override
    public void onError(VolleyError error) {
        System.out.println("Error" + error);
    }
}