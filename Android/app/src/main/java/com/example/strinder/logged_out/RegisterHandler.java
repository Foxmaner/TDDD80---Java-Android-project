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
    private final ServerConnection connection;

    /** Initialize a RegisterHandler object
     *
     * @param account - the GoogleSignInAccount that is verified.
     * @param activity - the Activity.
     */
    public RegisterHandler(final GoogleSignInAccount account, final Activity activity) {
        this.account = account;
        this.loginHandler = new LoginHandler(account,activity);
        this.activity = activity;
        this.connection = new ServerConnection(activity);
    }

    /** Try to register the account, if this returns error code 409, we will try to login with
     * the given details through a LoginHandler.
     * @see LoginHandler
     */
    public void tryRegister() {
        JSONObject jsonObject = new JSONObject();
        try {
            String firstName = "Unknown";
            String lastName = "Unknown";
            //If these fail, we can sadly not create an account.
            String username = null;
            String password = null;

            if(account.getGivenName() != null) {
                firstName =  account.getGivenName();
            }
            if(account.getFamilyName() != null) {
                lastName =  account.getFamilyName();
            }
            if(account.getId() != null) {
                username =  account.getId();
                password = account.getId();
            }

            jsonObject.put("first_name", firstName);
            jsonObject.put("last_name",lastName);
            jsonObject.put("username", username);
            //The password is set the google id, then salted and hashed on the server side.
            jsonObject.put("password",password);
            //Send a request and let the listener (this) handle what to do.
            connection.sendStringJsonRequest("/add", jsonObject, Request.Method.POST, null,this);

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
