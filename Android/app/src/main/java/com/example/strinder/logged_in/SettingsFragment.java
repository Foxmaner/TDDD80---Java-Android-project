package com.example.strinder.logged_in;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.User;
import com.example.strinder.logged_in.handlers.LogoutHandler;


/**
 * This class is a subclass of {@link Fragment Fragment}.
 * This class displays the logged-in {@link User User} object's details and allows the user
 * to change these.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {
    private User user;
    private LogoutHandler logoutHandler;
    private  EditText firstName,lastName,gender,biography, birthday;

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
            user = bundle.getParcelable("account");
        }

        Button signOut = v.findViewById(R.id.signOutBtn);
        signOut.setOnClickListener(this);

        firstName = v.findViewById(R.id.editFirstName);
        lastName = v.findViewById(R.id.editLastName);
        gender = v.findViewById(R.id.editGender);
        biography = v.findViewById(R.id.editBiography);
        birthday = v.findViewById(R.id.editBirthday);

        Button submitButton = v.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(this::onSubmit);

        firstName.setText(user.getFirstName());
        lastName.setText(user.getLastName());
        biography.setText(user.getBiography());

        /*
            Set the hint to the existing birthday, but if it is null set it so that
            the user understands the format.
         */

        if(user.getBirthday() != null)
            birthday.setText(user.getBirthday());

        if(user.getGender() != null) {
            gender.setText(user.getGender());
        }

        return v;
    }

    /** This is executed when the user presses the {@link android.widget.Button Button}
     *
     * @param view - the {@link Button Button} object's {@link View View}.
     */
    private void onSubmit(final View view) {
        String newFirstName = firstName.getText().toString();
        String newLastName = lastName.getText().toString();
        String newGender = gender.getText().toString();
        String newBiography = biography.getText().toString();
        String newBirthday = birthday.getText().toString();

        if(newFirstName.length() > 0 && newLastName.length() > 0 && newGender.length() > 0 &&
            newBiography.length() > 0 && newBirthday.length() > 0) {
            //We do this because different devices handle the date type different.
            if(newBirthday.matches("^\\d{4}/\\d{2}/\\d{2}$"))
                user.setBirthday(newBirthday);
            else if(newBirthday.matches("^\\d{4}\\d{2}\\d{2}$")) {
                user.setBirthday(newBirthday.substring(0,4) + "/" + newBirthday.substring(4,6) +
                        "/" + newBirthday.substring(6,8));
            }
            //The date is wrongly formatted.
            else {
                Toast.makeText(getContext(),"Some details are wrong formatted. Can't be saved.",
                        Toast.LENGTH_SHORT).show();
                return;
            }


            user.setFirstName(newFirstName);
            user.setLastName(newLastName);
            user.setBiography(newBiography);
            user.setGender(newGender);

            user.uploadData(getContext(), new VolleyResponseListener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("Upload Details", "Successfully uploaded details.");
                    Toast.makeText(getContext(),"Successfully uploaded details",
                            Toast.LENGTH_SHORT).show();

                    firstName.setText(user.getFirstName());
                    lastName.setText(user.getLastName());
                    birthday.setText(user.getBirthday());
                    biography.setText(user.getBiography());
                    gender.setText(user.getGender());

                }

                @Override
                public void onError(VolleyError error) {
                    ServerConnection connection = new ServerConnection(getContext());
                    connection.maybeDoRefresh(error,user);
                    Log.e("Upload Details",error.toString());
                    Toast.makeText(getContext(),"Details were correctly formatted, " +
                            "but failed to upload to the database",Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(getContext(),"Some details are wrong formatted. Can't be saved.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** This is executed when the user presses the "Sign Out" {@link Button Button}.
     *
     * @param view - the {@link Button Button} object's {@link View View}.
     */
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.signOutBtn) {
            logoutHandler.tryLogout(user);
        }
    }


}