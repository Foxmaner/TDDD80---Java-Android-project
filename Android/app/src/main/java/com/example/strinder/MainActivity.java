package com.example.strinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create a LoginFragment
        LoginFragment loginFragment = LoginFragment.newInstance();
        //Set the fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentView, loginFragment)
                .commit();

    }
}