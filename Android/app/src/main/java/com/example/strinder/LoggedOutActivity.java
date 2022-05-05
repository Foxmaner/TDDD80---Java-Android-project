package com.example.strinder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.strinder.logged_out.LoginFragment;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;

/**
 * This activity class handles all fragments that are available for users that are NOT logged in
 * on the server.
 * */
public class LoggedOutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_out);

        //Set the FragmentContainerView to a LoginFragment.
        LoginFragment fragment = LoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.mainView,fragment).commit();
        //Connect FireBase to Google API
        // Your server's client ID, not your Android client ID.
        // Only show accounts previously used to sign in.
        BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true)
                        .build());

    }


}