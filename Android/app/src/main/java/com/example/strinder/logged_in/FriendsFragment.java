package com.example.strinder.logged_in;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.LoggedInActivity;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.TrainingSession;
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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText searchFriendText;

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
    // TODO: Rename and change types and number of parameters
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v  = inflater.inflate(R.layout.fragment_friends, container, false);

        Bundle bundle = getArguments();
        if(bundle != null) {
            user =  bundle.getParcelable("account");

            EditText searchFriendText = (EditText)v.findViewById(R.id.searchFriendText);
            searchFriendText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // When you have pressed "done/enter" on keyboard"
                        ServerConnection connection = new ServerConnection(v.getContext());
                        String userID = searchFriendText.getText().toString();
                        fetchUser(connection,userID);


                        //Closes keyboard
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        handled = true;
                    }
                    return handled;
                }
            });

            ImageButton addFriendButton = (ImageButton) v.findViewById(R.id.buttonFriendAdd);
            ImageButton messageFriendButton = (ImageButton) v.findViewById(R.id.buttonFriendMessage);
            addFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFriend();
                }
            });

            messageFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("MessageFriend");
                }
            });

        }



        return v;
    }

    public void fetchUser(final ServerConnection connection, final String userID) {
        connection.sendStringJsonRequest("/user/get_data/" + userID,
                new JSONObject(),
                Request.Method.GET, user.getAccessToken(), this);

    }

    @Override
    public void onResponse(String response) {
        System.out.println("Svar!!");
        System.out.println(response.toString());
        View v = this.getView();
        TextView textFriendName = (TextView) v.findViewById(R.id.userCardName);
        TextView textFriendBio = (TextView) v.findViewById(R.id.userCardBio);
        ImageView imageFriend = (ImageView) v.findViewById(R.id.friendImage);
        Gson gson = new Gson();

        User friend = gson.fromJson(response,User.class);

        textFriendName.setText(friend.getFirstName().toString() + " " + friend.getLastName());
        textFriendBio.setText(friend.getBiography().toString());

        ImageButton addFriendButton = (ImageButton) v.findViewById(R.id.buttonFriendAdd);
        ImageButton messageFriendButton = (ImageButton) v.findViewById(R.id.buttonFriendMessage);
        addFriendButton.setVisibility(View.VISIBLE);
        messageFriendButton.setVisibility(View.VISIBLE);
        if(user.getPhotoUrl() != null) {
            //This ensures that the image always is set to the newly uploaded one. Picasso ignores (by default) identical URLs.
            if(getActivity() != null) {
                Picasso.with(getActivity().getApplicationContext()).
                        invalidate("https://www.dropbox.com/s/g3ybnjebb26s51t/"+
                                user.getUsername()+".png?raw=1");
            }
            Picasso.with(getActivity()).load(friend.getPhotoUrl())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(imageFriend);
        }

    }

    @Override
    public void onError(VolleyError error) {
        System.out.println("Error!!");
        System.out.println(error.toString());

        View v = this.getView();
        TextView textFriendName = (TextView) v.findViewById(R.id.userCardName);
        TextView textFriendBio = (TextView) v.findViewById(R.id.userCardBio);
        ImageView imageFriend = (ImageView) v.findViewById(R.id.friendImage);
        ImageButton addFriendButton = (ImageButton) v.findViewById(R.id.buttonFriendAdd);
        ImageButton messageFriendButton = (ImageButton) v.findViewById(R.id.buttonFriendMessage);


        textFriendName.setText("Cant find user");
        textFriendBio.setText("");
        imageFriend.setImageDrawable(null);
        addFriendButton.setVisibility(View.INVISIBLE);
        messageFriendButton.setVisibility(View.INVISIBLE);


    }

    public void addFriend(){
        ServerConnection connection = new ServerConnection(this.getContext());
        EditText searchFriendText = (EditText)this.getView().findViewById(R.id.searchFriendText);
        String friendID = searchFriendText.getText().toString();
        connection.sendStringJsonRequest("/befriend/" + friendID,
                new JSONObject(),
                Request.Method.GET, user.getAccessToken(),  new VolleyResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("lyckad att befrienda");
                        System.out.println(response);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        System.out.println("misslyckad befriend");
                        System.out.println(error);
                    }
                    });
        return;
    }
}