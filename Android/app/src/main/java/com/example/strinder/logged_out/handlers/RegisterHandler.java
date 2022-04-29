package com.example.strinder.logged_out.handlers;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.backend_related.ServerConnection;
import com.example.strinder.backend_related.VolleyResponseListener;
import com.example.strinder.private_data.CompletionListener;
import com.example.strinder.private_data.GoogleServices;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Gender;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** This class handles the register request to the server. */
public class RegisterHandler implements VolleyResponseListener<String>, CompletionListener {

    private static final int CONFLICT_STATUS_CODE = 409;
    private final LoginHandler loginHandler;
    private final GoogleSignInAccount account;
    private final Activity activity;
    private final ServerConnection connection;
    private final String[] privateData;

    /** Initialize a RegisterHandler object
     *
     * @param account - the GoogleSignInAccount that is verified.
     * @param activity - the Activity.
     */
    public RegisterHandler(final GoogleSignInAccount account, final Activity activity) {
        this.account = account;
        this.loginHandler = new LoginHandler(activity);
        this.activity = activity;
        this.connection = new ServerConnection(activity);
        this.privateData = new String[2];
    }

    /** Try to register the account, if this returns error code 409, we will try to login with
     * the given details through a LoginHandler.
     * @see LoginHandler
     */
    public void tryRegister() {

        //Private information
        List<String> scopes = new ArrayList<>();

        scopes.add("https://www.googleapis.com/auth/user.gender.read");
        scopes.add("https://www.googleapis.com/auth/user.birthday.read");
        scopes.add(Scopes.PROFILE);
        GoogleServices services = new GoogleServices(activity);

        services.requestPrivateData(account,scopes,"genders,birthdays",(person,
                                                                              obj) -> {

            if(person.getGenders() != null) {
                Gender gender = person.getGenders().get(0);
                privateData[0] = gender.getFormattedValue();
            }

            if(person.getBirthdays() != null) {
                Date date = person.getBirthdays().get(0).getDate();
                if(date.getYear() != null && date.getMonth() != null && date.getDay() != null) {
                    String year = date.getYear().toString();
                    String month = date.getMonth().toString();
                    String day = date.getDay().toString();

                    privateData[1] = year + "/" + month + "/" + day;
                }

            }


        },null,this);

    }

    @Override
    public void onResponse(String response) {
        loginHandler.tryLogin(account);
    }

    @Override
    public void onError(VolleyError error) {

        if(error.networkResponse != null && error.networkResponse.statusCode == CONFLICT_STATUS_CODE) {
            Log.i("Volley Register Failed","Account already exists. Performing login action.");
            loginHandler.tryLogin(account);

        }
        else {
            Log.e("Volley Register Error", error.toString());
            Toast.makeText(activity, "Error: " +  error + ". Please try again later.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCompletion() {

        //Try to register account to the Strinder database
        JSONObject jsonObject = new JSONObject();
        try {
            String firstName = "Unknown";
            String lastName = "Unknown";
            String email = "Unknown";
            String gender = privateData[0] == null ? "Unknown" : privateData[0];
            String birthday = privateData[1] == null ? "Unknown" : privateData[1];

            //If these fail, we can sadly not create an account.
            String username = null;
            String password = null;

            if(account.getGivenName() != null) {
                firstName =  account.getGivenName();
            }
            if(account.getFamilyName() != null) {
                lastName =  account.getFamilyName();
            }
            if(account.getEmail() != null) {
                email = account.getEmail();
            }
            if(account.getId() != null) {
                username =  account.getId();
                password = account.getId();
            }


            jsonObject.put("first_name", firstName);
            jsonObject.put("last_name",lastName);
            jsonObject.put("username", username);
            jsonObject.put("email",email);
            jsonObject.put("gender",gender);
            jsonObject.put("photo_url", account.getPhotoUrl() == null ? JSONObject.NULL : account.getPhotoUrl());
            jsonObject.put("birthday",birthday);
            //The password is set the google id, then salted and hashed on the server side.
            jsonObject.put("password",password);
            //Send a request and let the listener (this) handle what to do.
            connection.sendStringJsonRequest("/add", jsonObject, Request.Method.POST,
                    null,this);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
