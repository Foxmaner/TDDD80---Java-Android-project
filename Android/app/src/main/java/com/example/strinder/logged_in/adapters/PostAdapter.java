package com.example.strinder.logged_in.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.TrainingSession;
import com.example.strinder.backend_related.tables.User;
import com.example.strinder.logged_in.CommentFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private final Context context;
    private final List<Post> posts;
    private final List<User> users;
    private final User currentUser;
    private final ServerConnection connection;
    private final FragmentManager manager;

    public PostAdapter(final Context context, final List<Post> posts, final List<User> users,
                       final User currentUser, final FragmentManager manager) {
        this.context = context;
        this.posts = posts;
        this.users = users;
        this.currentUser = currentUser;
        this.manager = manager;
        connection = new ServerConnection(context);

    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                         int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.post_card, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        Post post = posts.get(position);
        User user;

        //In case we know that all posts belong to one individual. The profile is one such case.
        if (users.size() == 1 && users.get(0).equals(currentUser)) {
            user = users.get(0);
        }
        else {
            user = users.get(position);
        }

        //Set Map position

        holder.getMapView().onCreate(null);
        holder.getMapView().onResume();

        holder.getMapView().getMapAsync(googleMap -> {
            // Add a marker at the specified location.
            LatLng pos = new LatLng(post.getLatitude(), post.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title("Exercise location"));

            // You could set the values below as constants, but these are just
            // displayed here and does not really have to be explained.
            CameraPosition cameraPosition = new CameraPosition.Builder().
                    target(pos).
                    tilt(60).
                    zoom(15).
                    bearing(0).
                    build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            //Make it so that the user can't move around in the map window.
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);

        });


        //Set onLike listener
        holder.getLikeButton().setOnClickListener(view ->
                connection.sendStringJsonRequest("/post/like/" + post.getId(),
                        new JSONObject(), Request.Method.POST, currentUser.getAccessToken(),
                        new VolleyResponseListener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Gson gson = new Gson();
                                TypeToken<List<User>> token = new TypeToken<List<User>>() {
                                };
                                //The users that have liked the post.
                                List<User> likeUsers = gson.fromJson(response, token.getType());

                                setLikeText(holder, likeUsers);
                            }

                            @Override
                            public void onError(VolleyError error) {
                                Log.e("Like Post Error", "Error occurred when liking post.");
                                Toast.makeText(context, "Failed to like post.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }));


        //Fetch likes
        fetchLikes(post, holder);

        TrainingSession session = post.getTrainingSession();

        holder.getPostNameView().setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
        //We do not want to reload the image.
        Picasso.with(context).load(user.getPhotoUrl())
                .placeholder(android.R.drawable.sym_def_app_icon)
                .error(android.R.drawable.sym_def_app_icon)
                .into(holder.getProfileImage());

        holder.getPostCaptionView().setText(post.getCaption());
        holder.getPostTitleView().setText(post.getTitle());
        holder.getPostDate().setText(post.getDate());

        //Collapse / Expand view
        holder.getCommentButton().setOnClickListener(view -> {
            CommentFragment commentFragment = CommentFragment.newInstance(currentUser,post,position);
            manager.beginTransaction().replace(R.id.loggedInView,
                    commentFragment).commit();
        });

        if(currentUser.equals(user)){
            holder.getDeleteButton().setVisibility(View.VISIBLE);

            holder.getDeleteButton().setOnClickListener(view ->
                    connection.sendStringJsonRequest("/del/post/" + String.valueOf(post.getId()),
                            new JSONObject(), Request.Method.DELETE, currentUser.getAccessToken(),
                            new VolleyResponseListener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(context, "Post removed",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(VolleyError error) {
                                    Log.e("Like Post Error", "Error occurred when deleting post.");
                                    Toast.makeText(context, "Failed to delete post.",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }));

        }



        if (session != null) {
            holder.getPostExercise().setText(session.getExercise());
            holder.getPostDistanceValueView().setText(String.format("%s %s", session.getDistance(),
                    session.getDistanceUnit()));
            holder.getPostTimeValueView().setText(session.getElapsedTime());
            holder.getPostSpeedValueView().setText(String.format("%s %s", session.getSpeed(),
                    session.getSpeedUnit()));

        }

    }

    private void setLikeText(final PostViewHolder holder,
                             final List<User> users) {
        String text;

        int likes = users.size();
        if (users.contains(currentUser)) {
            likes--;
            text = String.format("You and %s other people have liked " +
                    " this post", likes);
            DrawableCompat.setTint(holder.getLikeButton().getDrawable(),
                    context.getColor(R.color.selected));
        }
        else {
            text = String.format("%s people have liked" +
                    " this post", likes);
            DrawableCompat.setTint(holder.getLikeButton().getDrawable(),
                    context.getColor(R.color.papaya));
        }

        holder.getLikes().setText(text);
    }

    private void fetchLikes(final Post post, final PostViewHolder holder) {

        connection.sendStringJsonRequest("/post/get_likes/" + post.getId(), new JSONObject(),
                Request.Method.GET, currentUser.getAccessToken(),
                new VolleyResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        TypeToken<List<User>> token = new TypeToken<List<User>>() {
                        };
                        //The users that have liked the post.
                        List<User> likeUsers = gson.fromJson(response, token.getType());

                        setLikeText(holder, likeUsers);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.e("Like Post Error", "Error occurred when fetching likes.");
                        Toast.makeText(context, "Failed to get amount of likes on post.",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }


}


class PostViewHolder extends RecyclerView.ViewHolder {
    private final TextView postExercise,postNameView,postCaptionView,postTitleView,
            postDistanceValueView, postTimeValueView, postSpeedValueView, postDate,likes;
    private final ImageView profileImage;
    private final ImageButton likeButton;
    private final ImageButton commentButton;
    private final MapView mapView;
    private final ImageButton deleteButton;
    public PostViewHolder(@NonNull View itemView) {
        super(itemView);
        postNameView = itemView.findViewById(R.id.postCardName);
        postDistanceValueView = itemView.findViewById(R.id.postCardDistance);
        postTimeValueView = itemView.findViewById(R.id.postCardTime);
        postSpeedValueView = itemView.findViewById(R.id.postCardSpeed);
        postCaptionView = itemView.findViewById(R.id.postCardCaption);
        postTitleView = itemView.findViewById(R.id.postCardTitle);
        postDate = itemView.findViewById(R.id.postCardDate);
        profileImage = itemView.findViewById(R.id.postCardProfileImage);
        postExercise = itemView.findViewById(R.id.postCardActivity);
        likeButton = itemView.findViewById(R.id.likeButton);
        commentButton = itemView.findViewById(R.id.commentButton);
        likes = itemView.findViewById(R.id.postCardLikes);
        mapView = itemView.findViewById(R.id.map);
        deleteButton = itemView.findViewById(R.id.buttonDeletePost);
    }


    public TextView getPostExercise() {
        return postExercise;
    }

    public TextView getPostNameView() {
        return postNameView;
    }

    public TextView getPostCaptionView() {
        return postCaptionView;
    }

    public TextView getPostTitleView() {
        return postTitleView;
    }

    public TextView getPostDistanceValueView() {
        return postDistanceValueView;
    }

    public TextView getPostTimeValueView() {
        return postTimeValueView;
    }

    public TextView getPostSpeedValueView() {
        return postSpeedValueView;
    }

    public TextView getPostDate() {
        return postDate;
    }

    public TextView getLikes() {
        return likes;
    }

    public ImageView getProfileImage() {
        return profileImage;
    }

    public ImageButton getLikeButton() {
        return likeButton;
    }

    public ImageButton getCommentButton() {
        return commentButton;
    }

    public MapView getMapView() {
        return mapView;
    }

    public ImageButton getDeleteButton() {
        return deleteButton;
    }
}


