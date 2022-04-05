package com.example.strinder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.strinder.logged_out.LoginFragment;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;


public class LoggedOutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_out);


        LoginFragment fragment = LoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.mainView,fragment).commit();

        //Connect FireBase to Google API
        // Your server's client ID, not your Android client ID.
        // Only show accounts previously used to sign in.
        BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                .build();

    }


}