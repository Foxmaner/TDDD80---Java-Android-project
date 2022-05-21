package com.example.strinder.logged_out;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.strinder.R;
import com.example.strinder.logged_out.handlers.AuthenticationHandler;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

/**
 * The class is a subclass of {@link Fragment}.
 * This fragment contains the layout for logging in through Google's API and all the backend
 * code that is needed to make sure that the client's account is verified.
 */
public class LoginFragment extends Fragment implements View.OnClickListener{

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> activityResultLauncher;

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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestIdToken(getString(R.string.web_client_id))
                .requestProfile().requestEmail().requestId().requestScopes(
                        new Scope("https://www.googleapis.com/auth/user.addresses.read"),
                        new Scope("https://www.googleapis.com/auth/user.gender.read"),
                        new Scope("https://www.googleapis.com/auth/user.birthday.read"),
                        new Scope(Scopes.PROFILE))
                .build();

        if(getActivity() != null && getContext() != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

            SignInButton signInButton = v.findViewById(R.id.googleSignIn);
            signInButton.setColorScheme(SignInButton.COLOR_LIGHT);
            signInButton.setSize(SignInButton.SIZE_WIDE);
            signInButton.setVisibility(View.VISIBLE);

            mGoogleSignInClient.silentSignIn().addOnCompleteListener(this.getActivity(),
                    this::handleSignInResult);


            //This device is new. The user will have to login through the Google Sign In API.
            signInButton.setOnClickListener(this);
            activityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Log.i("Google Sign In", "Received RESULT_OK");
                            // The Task returned from this call is always completed.

                            Task<GoogleSignInAccount> task = GoogleSignIn.
                                    getSignedInAccountFromIntent(result.getData());

                            handleSignInResult(task);
                        }
                        else {
                            Toast.makeText(getContext(),"Failed to login with Google.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        return v;
    }

    /** This is called when the user pressed the GoogleSignInButton and will execute the {@link
     * LoginFragment#signIn()} method.
     */
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

    /** This method launches the Google Sign In page. */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        activityResultLauncher.launch(signInIntent);
    }

    /** Handles the result from the GoogleSignIn process.
     *
     * @param completedTask - a {@link Task<GoogleSignInAccount> Task<GoogleSignInAccount>} object.
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

    /** This is called when the user is verified. The method sends a request to the backend with
     * a specific token that Google has given us. The backend then either accepts or rejects our
     * request. This request is handled inside the
     * {@link AuthenticationHandler AuthenticationHandler}
     *
     * @param account - the logged in {@link GoogleSignInAccount GoogleSignInAccount} that
     *                  contains all the information we need.
     */
    private void login(final GoogleSignInAccount account) {
        Log.i("Google Sign In", "Logged in. Authentication with backend in process...");

        if(this.getActivity() != null){
            AuthenticationHandler authenticationHandler = new AuthenticationHandler(account,
                    this.getActivity());
            authenticationHandler.tryAuthentication();
        }


    }

}