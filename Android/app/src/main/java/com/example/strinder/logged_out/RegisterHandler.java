package com.example.strinder.logged_out;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.ServerConnection;
import com.example.strinder.VolleyResponseListener;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;
/** This class handles the register request to the server. */
public class RegisterHandler implements VolleyResponseListener {

    private static final int CONFLICT_STATUS_CODE = 409;
    private final LoginHandler loginHandler;
    private final GoogleSignInAccount account;
    private final Activity activity;

    /** Initialize a RegisterHandler object
     *
     * @param account - the GoogleSignInAccount that is verified.
     * @param activity - the Activity.
     */
    public RegisterHandler(final GoogleSignInAccount account, final Activity activity) {
        this.account = account;
        this.loginHandler = new LoginHandler(account,activity);
        this.activity = activity;
    }

    /** Try to register the account, if this returns error code 409, we will try to login with
     * the given details through a LoginHandler.
     * @see LoginHandler
     */
    public void tryRegister() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("first_name", account.getGivenName());
            jsonObject.put("last_name",account.getFamilyName());
            jsonObject.put("username", account.getGivenName());
            //TODO Fix password issue. We can't get the google password...
            jsonObject.put("password","TestPassword");
            //Send a request and let the listener (this) handle what to do.
            ServerConnection.sendStringJsonRequest(activity, "/add", jsonObject, Request.Method.POST, this);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResponse(Object response) {
        loginHandler.tryLogin();
    }

    @Override
    public void onError(VolleyError error) {

        if(error.networkResponse != null && error.networkResponse.statusCode == CONFLICT_STATUS_CODE) {
            Log.i("Volley Register Failed","Account already exists. Performing login action.");
            loginHandler.tryLogin();
        }
        else {
            Log.e("Volley Register Error", error.toString());
            Toast.makeText(activity, "Error: " +  error + ". Please try again later.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}