package com.example.strinder.logged_in;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.strinder.R;
import com.example.strinder.logged_in.handlers.PostModel;
import com.example.strinder.logged_in.handlers.PostModel_RecyclerViewAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    ArrayList<PostModel> postModels = new ArrayList<>();
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
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


}