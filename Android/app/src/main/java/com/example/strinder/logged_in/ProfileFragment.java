package com.example.strinder.logged_in;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.strinder.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance(final GoogleSignInAccount account, final String token) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("account",account);
        bundle.putString("token",token);
        profileFragment.setArguments(bundle);

        return profileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        Bundle bundle = getArguments();
        if(bundle != null) {
            GoogleSignInAccount account =  bundle.getParcelable("account");
            String token = bundle.getString("token");
            TextView firstLastName = v.findViewById(R.id.firstLastName);
            firstLastName.setText(account.getDisplayName());


            ImageView profileImage = v.findViewById(R.id.profileImage);
            Picasso.with(getContext())
                    .load(account.getPhotoUrl())
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(profileImage);


            //DEN HÄR KODEN ÄR FORTFARANDE SÖNDER.

            if(this.getContext() != null) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        HttpTransport httpTransport = new NetHttpTransport();
                        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                        List<String> scopes = new ArrayList<>();
                        /*
                        scopes.add("https://www.googleapis.com/auth/user.phonenumbers.read");
                        scopes.add("https://www.googleapis.com/auth/user.gender.read");
                        scopes.add("https://www.googleapis.com/auth/user.addresses.read");
                        scopes.add("https://www.googleapis.com/auth/user.birthday.read");
                        */
                        scopes.add(Scopes.PROFILE);

                        // STEP 2
                        GoogleAccountCredential credential =
                                GoogleAccountCredential.usingOAuth2(getContext(), scopes);
                        credential.setSelectedAccount(
                                new Account(account.getEmail(), "com.google"));

                        PeopleService service = new PeopleService.Builder(httpTransport, jsonFactory, credential)
                                .setApplicationName(getString(R.string.app_name))
                                .build();


                        try {
                            //Det funkar, vi får ut något - men vi får inte all info vi vill ha. Permissions fel. Se utkommenterade rader ovanför.
                            Person response = service.people().get("people/me").setPersonFields("phoneNumbers").execute();
                            System.out.println(response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });

                thread.start();
            }





        }

        return v;
    }

}