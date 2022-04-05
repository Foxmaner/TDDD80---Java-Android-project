package com.example.strinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.strinder.logged_out.LoginFragment;

public class LoggedOutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_out);

        LoginFragment fragment = LoginFragment.newInstance();

        getSupportFragmentManager().beginTransaction().add(R.id.mainView,fragment).commit();



    }

}