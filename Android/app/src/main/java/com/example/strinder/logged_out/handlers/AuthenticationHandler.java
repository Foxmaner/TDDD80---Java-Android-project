package com.example.strinder.logged_out.handlers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.LoggedInActivity;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.private_data.CompletionListener;
import com.example.strinder.backend_related.private_data.GoogleServices;
import com.example.strinder.backend_related.tables.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Gender;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the authentication request to the server and handles the response
 * accordingly.
 */
public class AuthenticationHandler implements VolleyResponseListener<String>, CompletionListener {

    private final GoogleSignInAccount account;
    private final Activity activity;
    private final ServerConnection connection;
    private final String[] privateData;

    /** Initialize a RegisterHandler object
     *
     * @param account - the GoogleSignInAccount that is verified.
     * @param activity - the Activity.
     */
    public AuthenticationHandler(final GoogleSignInAccount account, final Activity activity) {
        this.account = account;
        this.activity = activity;
        this.connection = new ServerConnection(activity);
        this.privateData = new String[2];
    }

    /** Tries to authenticate Google Account with backend. If successful,
     * the activity will change.*/
    public void tryAuthentication() {

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
        Intent myIntent = new Intent(activity, LoggedInActivity.class);
        //Convert the response with GSON.
        Gson gson = new Gson();

        User user = gson.fromJson(response,User.class);

        //Send account to the intent
        myIntent.putExtra("account",user);

        activity.startActivity(myIntent);
    }

    @Override
    public void onError(VolleyError error) {
        Log.e("Volley Error",error.toString());
        Toast.makeText(activity,"Could not authenticate account with the " +
                activity.getString(R.string.app_name) + " server. " +
                "Please try again later.",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onCompletion() {

        JSONObject object = new JSONObject();
        try {
            object.put("idToken",account.getIdToken());

            Object gender = privateData[0] == null ? JSONObject.NULL : privateData[0];
            object.put("gender",gender);

            Object birthday = privateData[1] == null ? JSONObject.NULL : privateData[1];
            object.put("birthday",birthday);


        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        connection.sendStringJsonRequest("/authenticate",object, Request.Method.POST,
                null, this);
    }
}
