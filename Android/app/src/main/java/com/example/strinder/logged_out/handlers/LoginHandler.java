package com.example.strinder.logged_out.handlers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.LoggedInActivity;
import com.example.strinder.backend_related.ServerConnection;
import com.example.strinder.backend_related.User;
import com.example.strinder.backend_related.VolleyResponseListener;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/** This class handles the login request to the server. */
public class LoginHandler implements VolleyResponseListener<String> {

    private final Activity activity;
    private final ServerConnection connection;

    /** Initialize a LoginHandler object
     *
     * @param activity - the Activity.
     */
    public LoginHandler(final Activity activity) {
        this.activity = activity;
        this.connection = new ServerConnection(activity);
    }
    //FIXME Sometimes the client receives two responses from the server. Check this further..
    /** Try to login to the server with the given GoogleSignInAccount data. */
    public void tryLogin(final GoogleSignInAccount account) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", account.getId());
            jsonObject.put("password",account.getId());
            //Send a request and let the listener (this) handle what to do.
            connection.sendStringJsonRequest("/user/login", jsonObject,
                    Request.Method.POST, null,this);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResponse(String response) {
        Intent myIntent = new Intent(activity, LoggedInActivity.class);
        System.out.println(response);
        //TODO WE NEED TO IMPLEMENT A POST AND FRIEND CLASS! See user.
        //Convert the response with GSON.
        Gson gson = new Gson();
        User user = gson.fromJson(response,User.class);
        //Send account to the intent
        myIntent.putExtra("account",user);

        activity.startActivity(myIntent);
    }

    @Override
    public void onError(VolleyError error) {
        Log.e("Volley Login Error", error.toString());
        Toast.makeText(activity, "Error: " + error + ". Please try again later"
                ,Toast.LENGTH_SHORT).show();

    }
}
