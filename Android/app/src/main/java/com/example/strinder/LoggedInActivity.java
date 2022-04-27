package com.example.strinder;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.strinder.logged_in.AddActivityFragment;
import com.example.strinder.logged_in.FriendsFragment;
import com.example.strinder.logged_in.HomeFragment;
import com.example.strinder.logged_in.MessagesFragment;
import com.example.strinder.logged_in.ProfileFragment;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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

    private GoogleSignInAccount account;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        //Get user information and JWT Token.
        account = getIntent().getParcelableExtra("account");
        token = getIntent().getExtras().getString("token");

        //Top Nav
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView menuBar = findViewById(R.id.navBar);
        menuBar.setSelectedItemId(R.id.home);

        setBottomNavListener(menuBar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.nav_top,menu);
        return true;
    }

    /** This enables  the BottomNavListener to change fragment depending on what button
     * the user presses.
     * @param menuBar -  the BottomNavigationView object that the listener will be added to.
     */
    private void setBottomNavListener(final BottomNavigationView menuBar) {
        menuBar.setOnItemSelectedListener(item -> {

            Fragment fragment = null;
            final int id = item.getItemId();

            //We can't convert this to a switch case due to the ids not being final.
            if (id == R.id.home) {
                fragment = new HomeFragment();
            }
            else if (id == R.id.friends) {
                fragment = new FriendsFragment();
            }
            else if (id == R.id.plus) {
                fragment = AddActivityFragment.newInstance(account,token);
            }
            else if (id == R.id.messages) {
                fragment = new MessagesFragment();
            }
            else if (id == R.id.profile) {
                fragment = new ProfileFragment();
            }

            if(fragment != null) {
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.loggedInView, fragment).commit();

                return true;
            }

            return false;
        });
    }

}