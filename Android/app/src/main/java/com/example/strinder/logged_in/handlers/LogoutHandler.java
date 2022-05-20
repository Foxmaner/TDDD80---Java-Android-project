package com.example.strinder.logged_in.handlers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.LoggedOutActivity;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONObject;

public class LogoutHandler implements VolleyResponseListener<String> {
    private final Activity activity;
    private ServerConnection connection;
    private User user;
    public LogoutHandler(final Activity activity) {
        this.activity = activity;
    }

    public void tryLogout(final User user) {
        if(activity != null) {
            this.user = user;
           GoogleSignInClient client = GoogleSignIn.getClient(activity,
                   GoogleSignInOptions.DEFAULT_SIGN_IN);

            client.signOut();

            //Logout from backend
            connection = new ServerConnection(activity);
            JSONObject json = new JSONObject();
            connection.sendStringJsonRequest("/user/logout",json, Request.Method.POST,
                    user.getAccessToken(),
                    this);

        }
        else {
            throw new IllegalStateException("Activity was null, failed to logout user.");
        }
    }

    @Override
    public void onResponse(String response) {
        Log.i("Successfully disconnected from server",
                "User is logged out,switching intent.");

        //If successful (code = 200), go back to main page.
        Intent myIntent = new Intent(activity, LoggedOutActivity.class);
        activity.startActivity(myIntent);
    }

    @Override
    public void onError(VolleyError error) {
        connection.maybeDoRefresh(error,user);
        Log.e("Logout error", "Failed to logout!");
        Toast.makeText(activity,"Failed to sign out. Please close the app",
                Toast.LENGTH_SHORT).show();
    }
}
