package com.example.strinder.logged_in;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.User;
import com.example.strinder.logged_in.adapters.FollowAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.List;

/**
 * This class is a subclass of {@link Fragment Fragment}.
 * This class displays all the followers of type {@link User User} for the logged-in
 * {@link User User} object.
 */
public class FollowFragment extends Fragment {

    private SearchView searchFriendText;
    private RecyclerView followList;
    private ServerConnection connection;
    private TextView title;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            user =  getArguments().getParcelable("account");
            connection = new ServerConnection(getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v  = inflater.inflate(R.layout.fragment_follower, container, false);

        searchFriendText = v.findViewById(R.id.searchFriendText);
        title = v.findViewById(R.id.followTitle);
        searchFriendText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                fetchUsers();
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                /*
                    We don't need to call the fetch here, as it reacts to every character in the
                    SearchView.
                */
                return false;
            }

        });

        SwipeRefreshLayout swipeRefreshLayout = v.findViewById(R.id.friendSwipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchUsers();
            Log.i("Refresh Friends","Refreshing friends, fetching all friends.");
            swipeRefreshLayout.setRefreshing(false);
        });


        followList = v.findViewById(R.id.followList);
        followList.setLayoutManager(new LinearLayoutManager(getContext()));

        //Initialize with a fetch
        fetchUsers();

        return v;
    }

    /** Fetches the followers for the logged-in {@link User User} object.
     */
    private void fetchUsers() {
        String fullName = searchFriendText.getQuery().toString();

        if(!fullName.isEmpty()) {
            connection.sendStringJsonRequest("/user/get_users/" + fullName, new JSONObject(),
                    Request.Method.GET, user.getAccessToken(),
                    new VolleyResponseListener<String>() {

                        @Override
                        public void onResponse(String response) {
                            title.setText(getString(R.string.searchResult));
                            handleResponse(response);
                        }

                        @Override
                        public void onError(VolleyError error) {
                            connection.maybeDoRefresh(error,user);
                            Log.e("Retrieve Users Error", "Failed to get users from " +
                                    "database");
                            Toast.makeText(getContext(), "Search Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            // Display what the user follows.
            title.setText(getString(R.string.users_that_you_follow));

            if(getContext() != null) {
                FollowAdapter adapter = new FollowAdapter(getContext(), user.getFriends(), user);
                followList.setAdapter(adapter);
            }
        }
    }

    /** Handles the .JSON response and converts it to a {@link List<User> List<User>} object
     * with the help of {@link Gson Gson}
     * @param response - the json response as a {@link String String} object.
     */
    private void handleResponse(final String response) {
        if(getContext() != null) {
            TypeToken<List<User>> token = new TypeToken<List<User>>() {
            };
            Gson gson = new Gson();
            List<User> users = gson.fromJson(response, token.getType());

            FollowAdapter adapter = new FollowAdapter(getContext(), users, user);
            followList.setAdapter(adapter);
        }

    }

}