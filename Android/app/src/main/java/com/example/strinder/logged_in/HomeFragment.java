package com.example.strinder.logged_in;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.ServerConnection;
import com.example.strinder.backend_related.User;
import com.example.strinder.backend_related.VolleyResponseListener;
import com.example.strinder.logged_in.handlers.PostModel;
import com.example.strinder.logged_in.handlers.PostModel_RecyclerViewAdapter;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements VolleyResponseListener<String> {
    private ArrayList<PostModel> postModels = new ArrayList<>();
    private ServerConnection connection;
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


        System.out.println("hejhej!!!!!");
        Bundle bundle = getArguments();
        if(bundle != null) {
            user =  bundle.getParcelable("account");
            System.out.println(user);
        }
        System.out.println(user);

        connection.sendStringJsonRequest("/add/" + user.getId(), new JSONObject(),
                Request.Method.GET, user.getAccessToken(), this);

        RecyclerView recyclerView = v.findViewById(R.id.homeFeedRecycleView);
        setUpPostModels();
        PostModel_RecyclerViewAdapter adapter = new PostModel_RecyclerViewAdapter(this.getContext(),postModels);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        recyclerView.setAdapter(adapter);
        // Inflate the layout for this fragment
        return v;
    }

    private void setUpPostModels(){
        for (int i = 0; i < 10; i++) {
            postModels.add(new PostModel("Eskil Cool" + i*20,"Cooler caption" + i));
        }
    }


    @Override
    public void onResponse(String response) {
        System.out.println("response success:");
        System.out.println(response);
    }

    @Override
    public void onError(VolleyError error) {
        System.out.println("response fail:");
        System.out.println(error);
    }
}