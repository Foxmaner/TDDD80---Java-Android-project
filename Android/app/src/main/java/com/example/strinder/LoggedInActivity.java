package com.example.strinder;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.backend_related.ServerConnection;
import com.example.strinder.backend_related.User;
import com.example.strinder.backend_related.VolleyResponseListener;
import com.example.strinder.logged_in.AddActivityFragment;
import com.example.strinder.logged_in.FriendsFragment;
import com.example.strinder.logged_in.HomeFragment;
import com.example.strinder.logged_in.MessagesFragment;
import com.example.strinder.logged_in.ProfileFragment;
import com.example.strinder.logged_in.SettingsFragment;
import com.example.strinder.logged_in.handlers.LogoutHandler;
import com.example.strinder.private_data.CompletionListener;
import com.example.strinder.private_data.GoogleServices;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** This Activity class handles all the fragments and backend code that is used when a user
 * is logged in to the server.
 */
public class LoggedInActivity extends AppCompatActivity implements CompletionListener,
        VolleyResponseListener {

    private Toolbar toolbar;
    private String token;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        //Get user information and JWT Token.
        GoogleSignInAccount account = getIntent().getParcelableExtra("account");
        token = getIntent().getExtras().getString("token");

        user = new User(account.getGivenName(),account.getFamilyName(),account.getEmail(),
                account.getId(), String.valueOf(account.getPhotoUrl()));

        //Private information
        List<String> scopes = new ArrayList<>();

        scopes.add("https://www.googleapis.com/auth/user.gender.read");
        scopes.add("https://www.googleapis.com/auth/user.birthday.read");
        scopes.add(Scopes.PROFILE);
        GoogleServices services = new GoogleServices(this);

        services.requestPrivateData(account,scopes,"genders,birthdays",(person,
                                                                                  obj) -> {
            if(person.getBirthdays() != null) {
                user.setBirthday(person.getBirthdays().get(0).getDate());
            }

            if(person.getGenders() != null) {
                user.setGender(person.getGenders().get(0));
            }

        },user,this);

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.nav_top,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if(item.getItemId() == R.id.action_settings) {
            SettingsFragment fragment = SettingsFragment.newInstance(token);
            getSupportFragmentManager().beginTransaction().replace(R.id.loggedInView,fragment).commit();
            setHeaderText(getString(R.string.settings));
            return true;
        }

        return false;
    }

    /** Change the top navigation menu header text
     *
     * @param text - the new String of text that is to be displayed.
     */
    private void setHeaderText(final String text) {
        TextView view = toolbar.findViewById(R.id.fragmentName);
        view.setText(text);
    }

    /** This enables  the BottomNavListener to change fragment depending on what button
     * the user presses.
     * @param menuBar -  the BottomNavigationView object that the listener will be added to.
     */
    private void setBottomNavListener(final BottomNavigationView menuBar, final User user,
                                      final String token) {
        menuBar.setOnItemSelectedListener(item -> {

            Fragment fragment = null;
            final int id = item.getItemId();

            //We can't convert this to a switch case due to the ids not being final.
            if (id == R.id.home) {
                setHeaderText(getString(R.string.navbar_home));
                fragment = new HomeFragment();
            }
            else if (id == R.id.friends) {
                fragment = new FriendsFragment();
                setHeaderText(getString(R.string.navbar_friends));
            }
            else if (id == R.id.plus) {
                fragment = new AddActivityFragment();
                setHeaderText(getString(R.string.navbar_plus));
            }
            else if (id == R.id.messages) {
                fragment = new MessagesFragment();
                setHeaderText(getString(R.string.navbar_messages));
            }
            else if (id == R.id.profile) {
                fragment = ProfileFragment.newInstance(user,token);
                setHeaderText(getString(R.string.navbar_profile));
            }

            if(fragment != null) {
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.loggedInView, fragment).commit();

                return true;
            }

            return false;
        });
    }

    @Override
    public void onCompletion() {
        //Request ID
        ServerConnection connection = new ServerConnection(this);
        JSONObject object = new JSONObject();
        connection.sendStringJsonRequest("/user/"+user.getUsername(),object,
                Request.Method.GET,token,this);

    }

    @Override
    public void onResponse(Object response) {
        String idAsString = (String)response;
        int id = Integer.parseInt(idAsString);
        user.setId(id);

        //Top Nav
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar =  getSupportActionBar();

        //Remove app name from top nav.
        if(bar != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView view = toolbar.findViewById(R.id.fragmentName);
        view.setText(getString(R.string.navbar_home));

        BottomNavigationView menuBar = findViewById(R.id.navBar);
        menuBar.setSelectedItemId(R.id.home);

        setBottomNavListener(menuBar,user,token);
    }

    @Override
    public void onError(VolleyError error) {
        Toast.makeText(this,"Failed to get user id from server. ",
                Toast.LENGTH_SHORT).show();
        LogoutHandler logoutHandler = new LogoutHandler(this);
        //Try to logout, if it fails - throw an IllegalStateException.
        logoutHandler.tryLogout(token);
    }
}