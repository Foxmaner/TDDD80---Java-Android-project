package com.example.strinder.logged_in;

import android.os.Bundle;
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
import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.User;
import com.example.strinder.logged_in.handlers.PostRecyclerViewAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements VolleyResponseListener<String> {
    private User user;
    private SwipeRefreshLayout swipeContainer;
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



        fetchData();
        //Loads recyclerview with empty list
        RecyclerView recyclerView = v.findViewById(R.id.homeFeedRecycleView);
        List<Post> emptyPostList = new ArrayList<Post>();
        PostRecyclerViewAdapter adapter = new PostRecyclerViewAdapter(this.getContext(),
                emptyPostList);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        recyclerView.setAdapter(adapter);

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();
                swipeContainer.setRefreshing(false);
            }

        });

        // Inflate the layout for this fragment
        return v;
    }


    @Override
    public void onResponse(String response) {

        //Loads recyclerview with list of all posts
        Gson gson=new Gson();
        TypeToken<List<Post>> token = new TypeToken<List<Post>>(){};
        List<Post> postList = gson.fromJson(response, token.getType());

        RecyclerView recyclerView = this.getView().findViewById(R.id.homeFeedRecycleView);

        PostRecyclerViewAdapter adapter = new PostRecyclerViewAdapter(this.getContext(),
                postList);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        recyclerView.setAdapter(adapter);
        if(postList.size()==0){
            Toast.makeText(this.getContext(), "There are no posts",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onError(VolleyError error) {
        System.out.println("response fail:");
        System.out.println(error);
    }

    public void fetchData(){
        Bundle bundle = getArguments();
        if(bundle != null) {
            user =  bundle.getParcelable("account");

            ServerConnection connection = new ServerConnection(this.getContext());

            connection.sendStringJsonRequest("/posts/" + user.getId() + "/-1" ,
                    new JSONObject(),
                    Request.Method.GET, user.getAccessToken(), this);

        }
    }
}