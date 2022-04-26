package com.example.strinder.logged_out.handlers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.LoggedInActivity;
import com.example.strinder.backend_related.ServerConnection;
import com.example.strinder.backend_related.VolleyResponseListener;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;

/** This class handles the login request to the server. */
public class LoginHandler implements VolleyResponseListener {

    private final GoogleSignInAccount account;
    private final Activity activity;
    private final ServerConnection connection;
    private final GoogleSignInClient client;

    /** Initialize a LoginHandler object
     *
     * @param account - the verified GoogleSignInAccount.
     * @param activity - the Activity.
     */
    public LoginHandler(final GoogleSignInAccount account, final Activity activity,
                        final GoogleSignInClient client) {
        this.account = account;
        this.activity = activity;
        this.connection = new ServerConnection(activity);
        this.client = client;
    }

    /** Try to login to the server with the given GoogleSignInAccount data. */
    public void tryLogin() {
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
    public void onResponse(Object response) {
        Intent myIntent = new Intent(activity, LoggedInActivity.class);
        //Convert the response with GSON.
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String,String>>(){}.getType();
        Map<String,String > json = gson.fromJson(response.toString(),type);
        String accessToken = json.get("access_token");
        myIntent.putExtra("token",accessToken);
        //Send account
        myIntent.putExtra("account",account);

        activity.startActivity(myIntent);
    }

    @Override
    public void onError(VolleyError error) {
        Log.e("Volley Login Error", error.toString());
        Toast.makeText(activity, "Error: " + error + ". Please try again later"
                ,Toast.LENGTH_SHORT).show();

    }
}
