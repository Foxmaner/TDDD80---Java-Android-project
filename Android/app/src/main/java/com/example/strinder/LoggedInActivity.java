package com.example.strinder;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.strinder.backend_related.storage.FirebaseServices;
import com.example.strinder.backend_related.tables.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

/**
 * This class extends {@link AppCompatActivity AppCompatActivity}.
 * This class handles all the different {@link androidx.fragment.app.Fragment Fragment} objects
 * and makes sure that every component works together.
 */
public class LoggedInActivity extends AppCompatActivity implements
        NavigationBarView.OnItemSelectedListener {

    private Toolbar toolbar;
    private User user;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        user = getIntent().getParcelableExtra("account");
        //Top Nav
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar =  getSupportActionBar();

        //Remove app name from top nav.
        if(bar != null) {
            bar.setDisplayShowTitleEnabled(false);
        }

        TextView view = toolbar.findViewById(R.id.fragmentName);
        view.setText(getString(R.string.navbar_home));

        BottomNavigationView menuBar = findViewById(R.id.navBar);
        menuBar.setSelectedItemId(R.id.home);

        FirebaseServices.getInstance().initialize(this);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.loggedInView);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            //Set default args for the HomeFragment!
            Bundle bundle = new Bundle();
            bundle.putParcelable("account",user);
            bundle.putInt("location",0);
            navController.setGraph(R.navigation.nav_graph,bundle);
            Log.i("Start Destination","Bundle created for start destination.");
            // Setup of BottomNavigationBar
            NavigationUI.setupWithNavController(menuBar,navController);
            menuBar.setOnItemSelectedListener(this);

        }

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
            Bundle bundle = new Bundle();
            bundle.putParcelable("account",user);
            navController.navigate(R.id.settingsScreen,bundle);
            setHeaderText(getString(R.string.settings));
            return true;
        }

        return false;
    }

    /** Change the top navigation menu header text.
     *
     * @param text - the new text of type {@link String String} that is to be displayed.
     */
    private void setHeaderText(final String text) {
        TextView view = toolbar.findViewById(R.id.fragmentName);
        view.setText(text);
    }


    /** This function is called after a post has been added
     * it changes the {@link androidx.fragment.app.Fragment Fragment} back to
     * {@link com.example.strinder.logged_in.HomeFragment HomeFragment}, and gives a confirmation,
     * according to the specified message.
     * @param message - the message of type {@link String String} that is to be displayed.
     * @param user - the logged-in {@link User User} object.
     */
    public void jumpToHome(final String message, User user){
        Bundle bundle = new Bundle();
        bundle.putParcelable("account",user);
        navController.navigate(R.id.homeScreen,bundle);

        Toast.makeText(this,message,
                Toast.LENGTH_SHORT).show();

        BottomNavigationView menuBar = findViewById(R.id.navBar);
        menuBar.setSelectedItemId(R.id.home);

        setHeaderText(getString(R.string.navbar_home));

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //This is executed when you press on one of the navigation buttons.

        final int id = item.getItemId();
        Bundle bundle = new Bundle();
        bundle.putParcelable("account",user);

        //We can't convert this to a switch case due to the ids not being final. (warning)
        if(id == R.id.home) {
            setHeaderText(getString(R.string.navbar_home));
            navController.navigate(R.id.homeScreen,bundle);
        }
        else if(id == R.id.follows) {
            setHeaderText(getString(R.string.navbar_followers));
            navController.navigate(R.id.followScreen,bundle);
        }
        else  if(id == R.id.plus) {
            setHeaderText(getString(R.string.navbar_plus));
            navController.navigate(R.id.activityScreen,bundle);
        }
        else if(id == R.id.notifications) {
            setHeaderText(getString(R.string.navbar_notifications));
            navController.navigate(R.id.notificationScreen,bundle);
        }
        else if(id == R.id.profile) {
            setHeaderText(getString(R.string.navbar_profile));
            navController.navigate(R.id.profileScreen,bundle);
        }
        else {
            return false;
        }

        return true;
    }
}