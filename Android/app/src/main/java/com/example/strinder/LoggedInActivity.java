package com.example.strinder;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.strinder.logged_in.AddActivityFragment;
import com.example.strinder.logged_in.FriendsFragment;
import com.example.strinder.logged_in.HomeFragment;
import com.example.strinder.logged_in.MessagesFragment;
import com.example.strinder.logged_in.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** This Activity class handles all the fragments and backend code that is used when a user
 * is logged in to the server.
 */
public class LoggedInActivity extends AppCompatActivity {

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

        menuBar.setOnItemSelectedListener(item -> {
            // do stuff
            if (item.getItemId()==R.id.home){
                HomeFragment home = new HomeFragment();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.loggedInView, home).commit();
            }else if(item.getItemId()==R.id.friends){
                FriendsFragment friends = new FriendsFragment();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.loggedInView, friends).commit();
            }else if(item.getItemId()==R.id.plus){
                AddActivityFragment addActivity = new AddActivityFragment();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.loggedInView, addActivity).commit();
            }else if(item.getItemId()==R.id.messages){
                MessagesFragment messages = new MessagesFragment();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.loggedInView, messages).commit();
            }else if(item.getItemId()==R.id.profile){
                ProfileFragment profile = new ProfileFragment();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.loggedInView, profile).commit();
            }

            return true;
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.nav_top,menu);
        return true;
    }


}