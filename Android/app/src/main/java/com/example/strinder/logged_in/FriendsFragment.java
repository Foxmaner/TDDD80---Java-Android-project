package com.example.strinder.logged_in;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.storage.DropBoxServices;
import com.example.strinder.backend_related.tables.User;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment implements VolleyResponseListener<String> {

    private EditText searchFriendText;
    private TextView friendName,friendBiography;
    private ImageView friendImage;
    private ImageButton addFriendButton;
    private User user;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment FriendsFragment.
     */
    public static FriendsFragment newInstance(final User user) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("account",user);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            user =  getArguments().getParcelable("account");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v  = inflater.inflate(R.layout.fragment_friends, container, false);

        searchFriendText = v.findViewById(R.id.searchFriendText);

        searchFriendText.setOnEditorActionListener((v12, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // When you have pressed "done/enter" on keyboard"
                ServerConnection connection = new ServerConnection(v12.getContext());
                String userID = searchFriendText.getText().toString();
                fetchUser(connection,userID);

                //Closes keyboard
                getActivity();
                InputMethodManager imm = (InputMethodManager) v12.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v12.getWindowToken(), 0);
                handled = true;
            }
            return handled;
        });

        addFriendButton = v.findViewById(R.id.addFriendButton);
        addFriendButton.setOnClickListener(v1 -> addFriend());

        friendName = v.findViewById(R.id.userCardName);
        friendBiography  = v.findViewById(R.id.userCardBio);
        friendImage = v.findViewById(R.id.friendImage);

        return v;
    }

    public void fetchUser(final ServerConnection connection, final String userID) {
        connection.sendStringJsonRequest("/user/get_data/" + userID,
                new JSONObject(),
                Request.Method.GET, user.getAccessToken(), this);

    }

    @Override
    public void onResponse(String response) {
        Gson gson = new Gson();

        User friend = gson.fromJson(response,User.class);

        friendName.setText(String.format("%s %s", friend.getFirstName(), friend.getLastName()));
        friendBiography.setText(friend.getBiography());

        addFriendButton.setVisibility(View.VISIBLE);

        if(user.getPhotoUrl() != null) {
            //This ensures that the image always is set to the newly uploaded one. Picasso ignores (by default) identical URLs.
            if(getActivity() != null) {
                Picasso.with(getActivity().getApplicationContext()).
                        invalidate(DropBoxServices.getUserImagePath(friend));
            }

            Picasso.with(getActivity()).load(friend.getPhotoUrl())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(friendImage);
        }

    }

    @Override
    public void onError(VolleyError error) {

        friendName.setText(getString(R.string.unknown));
        friendBiography.setText(getString(R.string.unknown));
        friendImage.setImageDrawable(null);
        addFriendButton.setVisibility(View.INVISIBLE);

        Log.e("Find Friend", "Failed to find user.");

        Toast.makeText(getContext(),"Failed to find user.",Toast.LENGTH_SHORT).show();
    }

    public void addFriend(){
        ServerConnection connection = new ServerConnection(this.getContext());

        String friendID = searchFriendText.getText().toString();
        connection.sendStringJsonRequest("/befriend/" + friendID,
                new JSONObject(),
                Request.Method.POST, user.getAccessToken(),  new VolleyResponseListener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getContext(),"Succeeded to add friend",
                                Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(VolleyError error) {
                        Toast.makeText(getContext(),"Something went wrong. Failed to add friend.",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }
}