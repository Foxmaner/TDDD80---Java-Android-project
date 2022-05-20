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
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements VolleyResponseListener<String> {

    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recyclerView;
    private User user;
    private int postLocation;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(final User user, final int location) {
        HomeFragment fragment = new HomeFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("account",user);
        bundle.putInt("postLocation",location);
        fragment.setArguments(bundle);
        return fragment;
    }

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

            ServerConnection connection = new ServerConnection(this.getContext());
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


    private void setRecyclerViewOptions(final PostAdapter adapter) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
    public void fetchData(final ServerConnection connection) {
        connection.sendStringJsonRequest("/posts/latest/-1",
            new JSONObject(),
            Request.Method.GET, user.getAccessToken(), this);
    }

    public void onResponse(String response) {
        System.out.println(response);
        Gson gson = new Gson();
        FetchedPosts fetchedPosts = gson.fromJson(response,
                FetchedPosts.class);

        PostAdapter adapter = new PostAdapter(getContext(),
                fetchedPosts.getPosts(), fetchedPosts.getUsers(),user,this);

        setRecyclerViewOptions(adapter);
    }

    @Override
    public void onError(VolleyError error) {
        Toast.makeText(getContext(),"Failed to load posts. Try refreshing.",
                Toast.LENGTH_SHORT).show();
    }
}