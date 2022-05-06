package com.example.strinder.logged_in;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements VolleyResponseListener<String> {

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



        Bundle bundle = getArguments();
        if(bundle != null) {
            User user =  bundle.getParcelable("account");

            ServerConnection connection = new ServerConnection(this.getContext());

            connection.sendStringJsonRequest("/posts/latest/-1",
                    new JSONObject(),
                    Request.Method.GET, user.getAccessToken(), new VolleyResponseListener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Gson gson = new Gson();
                            FetchedPosts fetchedPosts = gson.fromJson(response,
                                    FetchedPosts.class);

                            RecyclerView recyclerView = v.findViewById(R.id.homeFeedRecycleView);

                            PostRecyclerViewAdapter adapter = new PostRecyclerViewAdapter(getContext(),
                                    fetchedPosts.getPosts(), fetchedPosts.getUsers());

                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                            recyclerView.setAdapter(adapter);

                        }

                        @Override
                        public void onError(VolleyError error) {
                            Toast.makeText(getContext(),"Failed to load posts. Try refreshing.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        }

        // Inflate the layout for this fragment
        return v;
    }


    @Override
    public void onResponse(String response) {
        System.out.println(response);
    }

    @Override
    public void onError(VolleyError error) {
        System.out.println("response fail:");
        System.out.println(error);
    }
}