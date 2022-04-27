package com.example.strinder.logged_in;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.strinder.R;
import com.google.android.material.textfield.TextInputLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddActivityFragment extends Fragment implements View.OnClickListener{

    private TextInputLayout titleInput;
    private TextInputLayout captionInput;
    private RadioGroup postSportTypeInput;

    public AddActivityFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddActivityFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddActivityFragment newInstance(String param1, String param2) {
        AddActivityFragment fragment = new AddActivityFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view =  inflater.inflate(R.layout.fragment_add_activity, container, false);

        titleInput = (TextInputLayout) view.findViewById(R.id.textInputLayoutPostTitle);
        captionInput = (TextInputLayout) view.findViewById(R.id.textInputLayoutPostCaption);
        postSportTypeInput = (RadioGroup) view.findViewById(R.id.inputAddActivitySport);

        Button addActivityButton = (Button) view.findViewById(R.id.addActivityButton);
        addActivityButton.setOnClickListener((View.OnClickListener) this);

        return view;
    }

    @Override
    public void onClick(View view) {
        System.out.println("cooler");
        String postTitle = titleInput.getEditText().getText().toString();
        String postCaption = captionInput.getEditText().getText().toString();
        int selectedRadioId = postSportTypeInput.getCheckedRadioButtonId();
        RadioButton selectedButton = (RadioButton) postSportTypeInput.findViewById(selectedRadioId);
        String postSport = selectedButton.getText().toString();
        System.out.println(postSport);
    }
}