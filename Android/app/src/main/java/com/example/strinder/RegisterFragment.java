package com.example.strinder;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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
        EditText inputFirstName = view.findViewById(R.id.inputRegisterFirstName);
        EditText inputLastName = view.findViewById(R.id.inputRegisterLastName);
        EditText inputUserName = view.findViewById(R.id.inputRegisterUserName);
        EditText inputEmail = view.findViewById(R.id.inputRegisterEmail);
        EditText inputPassword = view.findViewById(R.id.inputRegisterPassword);

        registerBtn.setOnClickListener((v) -> {
            if (inputFirstName.getText().toString().equals("") &&
                    inputLastName.getText().toString().equals("") &&
                    inputUserName.getText().toString().equals("") &&
                    inputEmail.getText().toString().equals("") &&
                    inputPassword.getText().toString().equals("")) {
                System.out.println("Success");
            }
        });


        return view;
    }
}