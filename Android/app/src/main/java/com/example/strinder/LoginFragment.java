package com.example.strinder;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button loginBtn = view.findViewById(R.id.loginButton);
        EditText username = view.findViewById(R.id.loginUsername);
        EditText password = view.findViewById(R.id.loginPassword);

        //Handle login
        loginBtn.setOnClickListener((v) -> {
            String usernameInput = username.getText().toString();
            String passwordInput = password.getText().toString();
            if(usernameInput.equals("") || usernameInput.contains(" ")) {
                username.setBackgroundResource(R.drawable.edt_err);
            }
            else if(passwordInput.equals("") || passwordInput.contains(" ")){
                password.setBackgroundResource(R.drawable.edt_err);
            }
            else {
                //Everything went fine.
                username.setBackgroundResource(R.drawable.edt_normal);
                password.setBackgroundResource(R.drawable.edt_normal);
            }

        });

        return view;
    }

    //This will most likely be used later, if not - remove it.
    @SuppressWarnings("unused")
    private void showAlertDialog(final String title, final String msg) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this.getContext());

        dlgAlert.setMessage(msg);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();

    }
}