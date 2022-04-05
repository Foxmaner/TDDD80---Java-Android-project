package com.example.strinder.logged_in;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.strinder.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoggedInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoggedInFragment extends Fragment {

    //TODO https://www.youtube.com/watch?v=fODp1hZxfng&ab_channel=CodePalace
    /* We have to solve issue when it comes to not showing the menu when logging in.*/
    public LoggedInFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment HomeFragment.
     */
    public static LoggedInFragment newInstance() {
        return new LoggedInFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_logged_in, container, false);

        return view;
    }
}