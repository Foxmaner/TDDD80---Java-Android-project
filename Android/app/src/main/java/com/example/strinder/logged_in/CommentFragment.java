package com.example.strinder.logged_in;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.Comment;
import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.User;
import com.example.strinder.logged_in.adapters.CommentAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentFragment extends Fragment {

    private User user;
    private Post post;
    private ServerConnection connection;
    private RecyclerView commentList;
    private int location;
    private TextView amountOfComments;
    private SwipeRefreshLayout swipeRefreshLayout;

    public CommentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CommentFragment.
     */
    public static CommentFragment newInstance(final User user, final Post post,
                                              final int location) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putParcelable("account",user);
        args.putParcelable("post",post);
        args.putInt("location",location);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable("account");
            post = getArguments().getParcelable("post");
            location = getArguments().getInt("location");
        }

        connection = new ServerConnection(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_comment, container, false);

        if(post != null && user != null) {
            commentList = v.findViewById(R.id.commentList);

            commentList.setLayoutManager(new LinearLayoutManager(getContext()));

            fetchComments(post);
            //Add comment button listener
            Button addComment = v.findViewById(R.id.addComment);
            addComment.setOnClickListener(view -> {
                EditText commentText = v.findViewById(R.id.commentText);
                String text = commentText.getText().toString();
                commentText.setText("");
                //If empty, don't continue
                if (text.isEmpty()) {
                    Toast.makeText(getContext(), "Please write a valid comment",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("text", text);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                connection.sendStringJsonRequest("/comments/add/" + post.getId(), jsonObject,
                        Request.Method.POST, user.getAccessToken(),
                        new VolleyResponseListener<String>() {

                            @Override
                            public void onResponse(String response) {
                                fetchComments(post);

                                Log.i("Add Comment Success", "Added comment to post " +
                                        post.getId());
                                Toast.makeText(getContext(), "Successfully Added Comment!",
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(VolleyError error) {
                                connection.maybeDoRefresh(error,user);
                                Log.e("Add Comment Error", error.toString());
                                Toast.makeText(getContext(), "Failed to add your comment! " +
                                                "Please try again!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            swipeRefreshLayout = v.findViewById(R.id.commentSwipeContainer);

            // Setup refresh listener which triggers reload of comments.
            swipeRefreshLayout.setOnRefreshListener(() -> {
                fetchComments(post);
                Log.i("Refresh Home","Refreshing Home, fetching new data.");
                swipeRefreshLayout.setRefreshing(false);
            });

            ImageButton backButton = v.findViewById(R.id.backButton);
            backButton.setOnClickListener(view -> {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("account",user);
                    bundle.putInt("location",location);

                    NavHostFragment.findNavController(this).navigate(R.id.homeScreen,bundle);
                }
            );


            amountOfComments = v.findViewById(R.id.amountOfComments);
            int size = post.getComments().size();
            if(size > 0) {
                amountOfComments.setText(String.format("There are currently %s" +
                        " comments on this post.",size));
            }
            else {
                amountOfComments.setText(R.string.noComments);
            }
        }

        return v;
    }


    private void fetchComments(final Post post) {
        //Send connection
        connection.sendStringJsonRequest("/comments/" + post.getId(), new JSONObject(),
                Request.Method.GET, user.getAccessToken(),
                new VolleyResponseListener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        TypeToken<List<Comment>> token = new TypeToken<List<Comment>>() {
                        };
                        post.setComments(gson.fromJson(response,token.getType()));

                        CommentAdapter adapter = new CommentAdapter(getContext(),
                                post.getComments(),user);

                        commentList.setAdapter(adapter);

                        //Set text that tells the user how many comments there are.
                        int size = post.getComments().size();

                        amountOfComments.setText(String.format("There are currently %s" +
                                " comments on this post.",size));


                    }


                    @Override
                    public void onError(VolleyError error) {
                        connection.maybeDoRefresh(error,user);
                        Log.e("Error Fetching Comments", "Failed to fetch" +
                                " comments for post " + post.getId());

                        Toast.makeText(getContext(), "Failed to fetch comments.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}