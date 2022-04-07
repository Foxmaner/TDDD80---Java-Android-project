package com.example.strinder.logged_out;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.strinder.LoggedInActivity;
import com.example.strinder.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);


        //Source: https://developers.google.com/identity/sign-in/android/sign-in

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile().requestEmail()
                .build();
        if(getActivity() != null && getContext() != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

            GoogleSignInAccount account = null;//GoogleSignIn.getLastSignedInAccount(getContext());

            SignInButton signInButton = v.findViewById(R.id.googleSignIn);
            signInButton.setColorScheme(SignInButton.COLOR_LIGHT);
            signInButton.setSize(SignInButton.SIZE_WIDE);
            signInButton.setVisibility(View.GONE);

            if (account == null) {
                signInButton.setVisibility(View.VISIBLE);
                signInButton.setOnClickListener(this);

               activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            System.out.println(result.getResultCode());
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                Log.i("Google Sign In", "Recieved RESULT_OK");
                                // The Task returned from this call is always completed, no need to attach
                                // a listener.
                                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                                handleSignInResult(task);
                            }
                        });

            }
            else {
                login(account);
            }

        }

        return v;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.googleSignIn) {
            Log.i("Google Sign In", "Opening Google Sign In prompt.");
            signIn();

        }

    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        activityResultLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            login(account);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google Sign In", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void login(final GoogleSignInAccount account) {
        //TODO Use account here somehow. We can get all the information from there.
        Log.i("Google Sign In", "Logged in. Sending user to main.");
        Intent myIntent = new Intent(getActivity(), LoggedInActivity.class);
        myIntent.putExtra("email",account.getEmail());
        myIntent.putExtra("firstName",account.getGivenName());
        myIntent.putExtra("lastName",account.getFamilyName());
        if(account.getPhotoUrl() != null) {
            myIntent.putExtra("photo", account.getPhotoUrl().toString());
        }
        startActivity(myIntent);
    }



}