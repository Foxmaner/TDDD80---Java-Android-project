package com.example.strinder.logged_out;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.LoggedInActivity;
import com.example.strinder.ServerConnection;
import com.example.strinder.VolleyResponseListener;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginHandler implements VolleyResponseListener {

    private final GoogleSignInAccount account;
    private final Activity activity;
    public LoginHandler(final GoogleSignInAccount account, final Activity activity) {
        this.account = account;
        this.activity = activity;
    }

    public void tryLogin() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", account.getGivenName());
            //TODO Fix password issue. We can't get the google password...
            jsonObject.put("password","TestPassword");
            //Send a request and let the listener (this) handle what to do.
            ServerConnection.sendStringJsonRequest(activity, "/user/login", jsonObject,
                    Request.Method.POST, this);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResponse(Object response) {
        //TODO We need to pass the Token as well!
        Intent myIntent = new Intent(activity, LoggedInActivity.class);
        myIntent.putExtra("email", account.getEmail());
        myIntent.putExtra("firstName", account.getGivenName());
        myIntent.putExtra("lastName", account.getFamilyName());

        if (account.getPhotoUrl() != null) {
            myIntent.putExtra("photo", account.getPhotoUrl().toString());
        }

        activity.startActivity(myIntent);
    }

    @Override
    public void onError(VolleyError error) {
        Log.e("Volley Login Error", error.toString());
        Toast.makeText(activity, "Failed to login. Please try again later"
                ,Toast.LENGTH_SHORT).show();

    }
}
