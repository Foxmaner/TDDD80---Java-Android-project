package com.example.strinder.logged_in;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.strinder.R;
import com.example.strinder.logged_in.handlers.LogoutHandler;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {
    private GoogleSignInClient client;
    private String token;
    private LogoutHandler logoutHandler;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @param token - the authentication token needed when logged in.
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance(final String token) {
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("token",token);
        settingsFragment.setArguments(bundle);
        return settingsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logoutHandler = new LogoutHandler(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        Bundle bundle = getArguments();
        if(bundle != null) {
            token = bundle.getString("token");
        }

        Button signOut = v.findViewById(R.id.signOutBtn);
        signOut.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.signOutBtn) {
            logoutHandler.tryLogout(token);
        }
    }


}