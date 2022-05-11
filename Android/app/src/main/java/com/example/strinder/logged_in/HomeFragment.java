package com.example.strinder.logged_in;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.strinder.logged_in.adapters.PostRecyclerViewAdapter;
import com.google.gson.Gson;

import org.json.JSONObject;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements VolleyResponseListener<String> {

    private SwipeRefreshLayout swipeContainer;
    private Boolean isAtEndOfScroll = false;
    private RecyclerView recyclerView;
    private User user;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(final User user) {
        HomeFragment fragment = new HomeFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("account",user);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        //fetchData();
        Bundle bundle = getArguments();
        if(bundle != null) {
            user =  bundle.getParcelable("account");

            ServerConnection connection = new ServerConnection(this.getContext());
            recyclerView = v.findViewById(R.id.homeFeedRecycleView);
            swipeContainer = v.findViewById(R.id.swipeContainer);

            // Setup refresh listener which triggers new data loading
            swipeContainer.setOnRefreshListener(() -> {
                fetchData(connection);
                Log.i("Refresh Home","Fetch data");
                swipeContainer.setRefreshing(false);
            });

            fetchData(connection);

        }

        // Inflate the layout for this fragment
        return v;
    }



    public void fetchData(final ServerConnection connection) {
        connection.sendStringJsonRequest("/posts/latest/-1",
            new JSONObject(),
            Request.Method.GET, user.getAccessToken(), this);
    }

    public void onResponse(String response) {
        Gson gson = new Gson();
        FetchedPosts fetchedPosts = gson.fromJson(response,
                FetchedPosts.class);

        PostRecyclerViewAdapter adapter = new PostRecyclerViewAdapter(getContext(),
                fetchedPosts.getPosts(), fetchedPosts.getUsers(),user);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView,
                                             int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isAtEndOfScroll = !recyclerView.canScrollVertically(1) &&
                        !isAtEndOfScroll;

            }
        });


    }

    @Override
    public void onError(VolleyError error) {
        Toast.makeText(getContext(),"Failed to load posts. Try refreshing.",
                Toast.LENGTH_SHORT).show();
    }
}