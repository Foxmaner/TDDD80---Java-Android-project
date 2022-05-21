package com.example.strinder.logged_in;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.strinder.logged_in.adapters.PostAdapter;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * This class is a subclass of {@link Fragment Fragment}.
 * This class displays all the {@link com.example.strinder.backend_related.tables.Post Post}
 * available in the database ordered by time of posting.
 */
public class HomeFragment extends Fragment implements VolleyResponseListener<String> {

    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recyclerView;
    private User user;
    private int postLocation;
    private ServerConnection connection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        if(bundle != null) {
            user =  bundle.getParcelable("account");
            postLocation = bundle.getInt("postLocation");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        if(user != null && getContext() != null) {

            connection = new ServerConnection(this.getContext());
            recyclerView = v.findViewById(R.id.homeFeedRecycleView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            swipeContainer = v.findViewById(R.id.homeSwipeContainer);

            // Setup refresh listener which triggers new data loading.
            swipeContainer.setOnRefreshListener(() -> {
                fetchData(connection);
                Log.i("Refresh Home","Refreshing Home, fetching new data.");
                swipeContainer.setRefreshing(false);
            });

            fetchData(connection);

            recyclerView.scrollToPosition(postLocation);
        }

        // Inflate the layout for this fragment
        return v;
    }

    /** Sets the {@link androidx.recyclerview.widget.RecyclerView RecyclerView} options. This
     * includes setting the {@link PostAdapter PostAdapter}.
     * @param adapter - the {@link PostAdapter PostAdapter} object.
     */
    private void setRecyclerViewOptions(final PostAdapter adapter) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /** Fetches the latest {@link com.example.strinder.backend_related.tables.Post Post} objects
     *  from the database. Ordered by time.
     *
     * @param connection - the {@link ServerConnection ServerConnection} object which is used to
     *                   send a request to the backend.
     */
    public void fetchData(final ServerConnection connection) {
        connection.sendStringJsonRequest("/posts/latest/-1",
            new JSONObject(),
            Request.Method.GET, user.getAccessToken(), this);
    }

    @Override
    public void onResponse(String response) {
        Gson gson = new Gson();
        FetchedPosts fetchedPosts = gson.fromJson(response,
                FetchedPosts.class);

        PostAdapter adapter = new PostAdapter(getContext(),
                fetchedPosts.getPosts(), fetchedPosts.getUsers(),user,this);

        setRecyclerViewOptions(adapter);
    }

    @Override
    public void onError(VolleyError error) {
        connection.maybeDoRefresh(error,user);
        Toast.makeText(getContext(),"Failed to load posts. Try refreshing.",
                Toast.LENGTH_SHORT).show();
    }
}