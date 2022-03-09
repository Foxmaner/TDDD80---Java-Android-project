package com.example.strinder;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {


    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment register.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        Button registerBtn = view.findViewById(R.id.registerBtn);
        EditText inputFirstName = view.findViewById(R.id.registerFirstName);
        EditText inputLastName = view.findViewById(R.id.registerLastName);
        EditText inputUserName = view.findViewById(R.id.registerUserName);
        EditText inputEmail = view.findViewById(R.id.registerEmail);
        EditText inputPassword = view.findViewById(R.id.registerPassword);
        TextView toLoginText = view.findViewById(R.id.registerLoginText);

        //TODO Fix this long if
        registerBtn.setOnClickListener((v) -> {
            if (!inputFirstName.getText().toString().trim().equals("") &&
                    !inputLastName.getText().toString().trim().equals("") &&
                    !inputUserName.getText().toString().trim().equals("") &&
                    !inputEmail.getText().toString().trim().equals("") &&
                    !inputPassword.getText().toString().trim().equals("")) {

                System.out.println("Register!");
            }
        });

        toLoginText.setOnClickListener((v) -> {
            //To Login we go
            if(getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragmentView, LoginFragment.newInstance()).commit();

            }
        });


        return view;
    }
}