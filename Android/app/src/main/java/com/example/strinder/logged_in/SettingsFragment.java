package com.example.strinder.logged_in;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.LoggedInActivity;
import com.example.strinder.LoggedOutActivity;
import com.example.strinder.R;
import com.example.strinder.backend_related.ServerConnection;
import com.example.strinder.backend_related.VolleyResponseListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, VolleyResponseListener {
    private GoogleSignInClient client;
    private String token;
    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @param token - the authentication token needed when logged in.
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance(final String token) {
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("token",token);
        settingsFragment.setArguments(bundle);
        return settingsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(this.getContext() != null) {
            client = GoogleSignIn.getClient(this.getContext(), GoogleSignInOptions.DEFAULT_SIGN_IN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        Bundle bundle = getArguments();
        if(bundle != null) {
            token = bundle.getString("token");
        }

        Button signOut = v.findViewById(R.id.signOutBtn);
        signOut.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.signOutBtn) {
            client.signOut();

            //Logout from backend
            ServerConnection connection = new ServerConnection(this.getContext());
            JSONObject json = new JSONObject();
            connection.sendStringJsonRequest("/user/logout",json, Request.Method.POST,token,
                    this);
        }
    }

    @Override
    public void onResponse(Object response) {
        //If successful (code = 200), go back to main page.
        Intent myIntent = new Intent(this.getActivity(), LoggedOutActivity.class);
        startActivity(myIntent);
    }

    @Override
    public void onError(VolleyError error) {
        //TODO Make this better. We cant just do this?
        Toast.makeText(this.getContext(),"Failed to sign out",Toast.LENGTH_SHORT).show();
    }
}