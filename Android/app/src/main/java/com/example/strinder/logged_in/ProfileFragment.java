package com.example.strinder.logged_in;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.strinder.CompletionListener;
import com.example.strinder.GoogleServices;
import com.example.strinder.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements CompletionListener {

    private String token;
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
            token = bundle.getString("token");
            TextView firstLastName = v.findViewById(R.id.firstLastName);
            firstLastName.setText(account.getDisplayName());


            ImageView profileImage = v.findViewById(R.id.profileImage);
            Picasso.with(getContext())
                    .load(account.getPhotoUrl())
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(profileImage);


            List<String> scopes = new ArrayList<>();

            scopes.add("https://www.googleapis.com/auth/user.gender.read");
            scopes.add("https://www.googleapis.com/auth/user.addresses.read");
            scopes.add(Scopes.PROFILE);
            GoogleServices services = new GoogleServices(getActivity());

            services.requestPrivateData(account,scopes,"addresses,genders",(person, obj) -> {
                //Change GUI usage
                View view = (View)obj;
                TextView info = view.findViewById(R.id.city);
                String gender = "Unknown";
                String address = "Unknown";
                if(person.getGenders() != null) {
                    gender = person.getGenders().get(0).getFormattedValue();
                }

                if(person.getAddresses() != null) {
                    address = person.getGenders().get(0).getFormattedValue();
                }

                info.setText("Address: " + address + "  Gender: " + gender);


            },v,this);

        }

        return v;
    }

    @Override
    public void onCompletion() {
        System.out.println(token);
    }
}