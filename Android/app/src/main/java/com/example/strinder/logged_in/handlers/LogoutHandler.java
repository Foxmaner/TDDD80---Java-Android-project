package com.example.strinder.logged_in.handlers;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.LoggedOutActivity;
import com.example.strinder.backend_related.ServerConnection;
import com.example.strinder.backend_related.VolleyResponseListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONObject;

public class LogoutHandler implements VolleyResponseListener<String> {
    private final Activity activity;

    public LogoutHandler(final Activity activity) {
        this.activity = activity;
    }

    public void tryLogout(final String token) {
        if(activity != null) {
           GoogleSignInClient client = GoogleSignIn.getClient(activity,
                   GoogleSignInOptions.DEFAULT_SIGN_IN);

            client.signOut();

            //Logout from backend
            ServerConnection connection = new ServerConnection(activity);
            JSONObject json = new JSONObject();
            connection.sendStringJsonRequest("/user/logout",json, Request.Method.POST,token,
                    this);

        }
        else {
            throw new IllegalStateException("Activity was null, failed to logout user.");
        }
    }

    @Override
    public void onResponse(String response) {
        //If successful (code = 200), go back to main page.
        Intent myIntent = new Intent(activity, LoggedOutActivity.class);
        activity.startActivity(myIntent);
    }

    @Override
    public void onError(VolleyError error) {
        //TODO Make this better. We cant just do this?
        Toast.makeText(activity,"Failed to sign out",Toast.LENGTH_SHORT).show();
    }
}
