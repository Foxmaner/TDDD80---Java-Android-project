package com.example.strinder;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LoggedInActivity extends AppCompatActivity {

    private static final String URL = "http://10.0.2.2:5000";
    private String firstName,lastName,email,photoUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        //Get user information
        Bundle bundle = getIntent().getExtras();
        firstName = bundle.getString("firstName");
        lastName = bundle.getString("lastName");
        email = bundle.getString("email");
        if(bundle.containsKey("photo")) {
            photoUrl = bundle.getString("photo");
        }

        //Top Nav
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView menuBar = findViewById(R.id.navBar);
        menuBar.setSelectedItemId(R.id.home);

        handleUser();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.nav_top,menu);
        return true;
    }

    private void handleUser() {
        //Check with networking if the user exists in the db, otherwise create a user.
        //TODO This works with HTTP, because of the network_security_config.xml. I needed this
        //TODO for it to work locally. In HTTPS, the server parsed the data wrong. We have to check
        //TODO if this error occurs on heroku or not. Also, we need to have this code "global"
        //TODO later.
        
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = URL + "/add";
        JSONObject json = new JSONObject();
        try {
            json.put("first_name",firstName);
            json.put("last_name",lastName);
            json.put("username",firstName);
            json.put("password","TestPassword123");
            String jsonString = json.toString();
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println("SUCCESS!");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("ERROR!");
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    return jsonString.getBytes(StandardCharsets.UTF_8);
                }
            };

            requestQueue.add(request);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }




        //requestQueue.add(request);

    }

}