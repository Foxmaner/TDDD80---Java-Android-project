package com.example.strinder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.strinder.logged_in.LoggedInFragment;
import com.example.strinder.logged_out.LoggedOutFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoggedOutFragment fragment = LoggedOutFragment.newInstance();

        getSupportFragmentManager().beginTransaction().add(R.id.mainView,fragment).commit();

    }
}