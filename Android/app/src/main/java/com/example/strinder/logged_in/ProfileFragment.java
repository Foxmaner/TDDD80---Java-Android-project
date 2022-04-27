package com.example.strinder.logged_in;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.strinder.backend_related.User;
import com.example.strinder.private_data.CompletionListener;
import com.example.strinder.R;
import com.squareup.picasso.Picasso;


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
    public static ProfileFragment newInstance(final User user, final String token) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("account",user);
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
            User user =  bundle.getParcelable("account");
            token = bundle.getString("token");
            //First and last name
            TextView firstLastName = v.findViewById(R.id.firstLastName);
            firstLastName.setText(user.getFirstName());
            firstLastName.append(" ");
            firstLastName.append(user.getLastName());

            //Profile image
            ImageView profileImage = v.findViewById(R.id.profileImage);

            if(user.getPhotoUrl() != null) {
                Picasso.with(getContext())
                        .load(user.getPhotoUrl())
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .error(android.R.drawable.sym_def_app_icon)
                        .into(profileImage);
            }

            TextView gender = v.findViewById(R.id.gender);
            if(user.getGender() != null) {
                gender.setText(user.getGender());
            }
            TextView birthday = v.findViewById(R.id.birthday);
            if(user.getBirthday() != null) {
                birthday.setText(user.getBirthday());
            }

            System.out.println(user.getId());

        }

        return v;
    }

    @Override
    public void onCompletion() {
        System.out.println(token);
    }
}