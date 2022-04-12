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

import com.example.strinder.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/**
 * This fragment contains the layout for logging in through Google's API and all the backend
 * code that is needed to make sure that the client's account is verified.
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
                .requestProfile().requestEmail().requestId()
                .build();
        if(getActivity() != null && getContext() != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

            //Check if the user was recently signed in through Google on this device.
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());

            //Set SignInButton style programatically.
            SignInButton signInButton = v.findViewById(R.id.googleSignIn);
            signInButton.setColorScheme(SignInButton.COLOR_LIGHT);
            signInButton.setSize(SignInButton.SIZE_WIDE);
            signInButton.setVisibility(View.GONE);

            if (account == null) {
                //This device is new. The user will have to login through the Google Sign In API.
                signInButton.setVisibility(View.VISIBLE);
                signInButton.setOnClickListener(this);

                activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                Log.i("Google Sign In", "Recieved RESULT_OK");
                                // The Task returned from this call is always completed.

                                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                                handleSignInResult(task);
                            }
                        });

            }
            else {
                //This user has been logged in earlier on this device.
                login(account);
            }

        }

        return v;
    }

    @Override
    public void onClick(View v) {
        /*
            This is called when the user pressed the GoogleSignInButton and will open the
            Google Sign In page.
         */

        if(v.getId() == R.id.googleSignIn) {
            Log.i("Google Sign In", "Opening Google Sign In prompt.");
            signIn();

        }

    }

    /** Launches the Google Sign In page. */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        activityResultLauncher.launch(signInIntent);
    }

    /** Handles the result from the Google Sign In page through a ActivityResultLauncher
     *
     * @param completedTask - a GoogleSignInAccount task.
     */
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

    /** This is called when the user is verified. Within this method we contact our server to
     * check if the account exists or not. If the account already exists (status code 409),
     * we try to login to that specific account with the given details.
     * @param account - the logged in GoogleSignInAccount that contains all the information we need.
     */
    private void login(final GoogleSignInAccount account) {
        Log.i("Google Sign In", "Logged in. Sending user to main.");
        //Connect to OUR server.
        RegisterHandler registerHandler = new RegisterHandler(account,this.getActivity());
        registerHandler.tryRegister();

    }



}