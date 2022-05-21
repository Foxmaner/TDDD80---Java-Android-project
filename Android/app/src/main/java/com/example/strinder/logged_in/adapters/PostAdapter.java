package com.example.strinder.logged_in.adapters;

import android.content.Context;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.TrainingSession;
import com.example.strinder.backend_related.tables.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

/** This class extends the {@link androidx.recyclerview.widget.RecyclerView.Adapter
 * RecyclerView.Adapter} class and is used to customize a RecyclerView for a {@link List<Post>
 * List<Post>} and the users that posted these posts, which are passed along as a
 * {@link List<User> List<User>} object. The class allows us to handle button presses and
 * different kinds of data on each any every post.
 */
public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private final Context context;
    private final List<Post> posts;
    private final List<User> users;
    private final User currentUser;
    private final ServerConnection connection;
    private final Fragment fragment;

    /** Initialize a PostAdapter object.
     *
     * @param context - a {@link Context context} object.
     * @param posts - a {@link List<Post> List<Post>} object. These are the posts that
     *                 will be displayed in the {@link RecyclerView RecyclerView}
     * @param users - a {@link List<User> List<User>} object. These are the users that
     * will be displayed in the {@link RecyclerView RecyclerView}
     *
     * @param currentUser - the logged-in {@link User User} object.
     * @param fragment - the {@link Fragment Fragment} object that this adapter is in. Is necessary
     *                   for navigation.
     */
    public PostAdapter(final Context context, final List<Post> posts, final List<User> users,
                       final User currentUser, final Fragment fragment) {
        this.context = context;
        this.posts = posts;
        this.users = users;
        this.currentUser = currentUser;
        this.fragment = fragment;
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

        Post post;
        User user;

        //In case we know that all posts belong to one individual. The profile is one such case.
        if (users.size() == 1 && users.get(0).equals(currentUser) && posts.size() > position) {
            user = currentUser;
            post = user.getPosts().get(posts.indexOf(posts.get(position)));
        }
        else {
            user = users.get(position);
            post = posts.get(position);
        }

        //Set Map position
        holder.getMapView().getMapAsync(googleMap -> {
            MapsInitializer.initialize(context.getApplicationContext());
            // Add a marker at the specified location.
            LatLng pos = new LatLng(post.getLatitude(), post.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title("Exercise location"));

            // You could set the values below as constants, but these are just
            // displayed here and does not really have to be explained.
            CameraPosition cameraPosition = new CameraPosition.Builder().
                    target(pos).zoom(16).build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            //Make it so that the user can't move around in the map window.
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);

        });

        holder.getMapView().onCreate(null);
        holder.getMapView().onStart();
        holder.getMapView().onResume();


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
                                post.setLikes(likeUsers);
                                setLikeText(holder, likeUsers);
                            }

                            @Override
                            public void onError(VolleyError error) {
                                connection.maybeDoRefresh(error,currentUser);
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
            Bundle bundle = new Bundle();
            bundle.putParcelable("account",currentUser);
            bundle.putParcelable("post",post);
            bundle.putInt("location",position);

            NavHostFragment.findNavController(fragment).navigate(R.id.commentScreen,bundle);
        });

        if(currentUser.equals(user)) {
            holder.getDeleteButton().setVisibility(View.VISIBLE);

            holder.getDeleteButton().setOnClickListener(view ->
                    connection.sendStringJsonRequest("/del/post/" + post.getId(),
                            new JSONObject(), Request.Method.DELETE, currentUser.getAccessToken(),
                            new VolleyResponseListener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(context, "Post removed",
                                            Toast.LENGTH_SHORT).show();

                                    if(user.getPosts() != null)
                                        user.getPosts().remove(post);
                                }

                                @Override
                                public void onError(VolleyError error) {
                                    connection.maybeDoRefresh(error,currentUser);
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

    /** Sets the text that displays how many likes a {@link Post Post} has.
     *
     * @param holder - the {@link FollowViewHolder FollowViewHolder} object.
     * @param users - the {@link List<User> List<User>} object that is passed into the
 *                    {@link PostAdapter PostAdapter}.
     */
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

    /** Fetch the likes for a specific {@link Post Post} object from the database.
     *
     * @param post - the {@link Post Post} object that we want to check the likes on.
     * @param holder - the {@link FollowViewHolder FollowViewHolder} object.
     */
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
                        connection.maybeDoRefresh(error,currentUser);
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

/** This class is used within the {@link com.example.strinder.logged_in.adapters.PostAdapter
 * PostAdapter} class. In this class we find and define all the different {@link View View}
 * objects.
 */
class PostViewHolder extends RecyclerView.ViewHolder {
    private final TextView postExercise,postNameView,postCaptionView,postTitleView,
            postDistanceValueView, postTimeValueView, postSpeedValueView, postDate,likes;
    private final ImageView profileImage;
    private final ImageButton likeButton;
    private final ImageButton commentButton;
    private final MapView mapView;
    private final ImageButton deleteButton;

    /** Initialize a PostViewHolder object.
     *
     * @param itemView - a {@link View View} object that lets us find the children {@link View View}
     *                   objects.
     */
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

    /** Returns the post's exercise.
     *
     * @return a {@link TextView TextView} object.
     * */
    public TextView getPostExercise() {
        return postExercise;
    }

    /** Returns the post's name.
     *
     * @return a {@link TextView TextView} object.
     * */
    public TextView getPostNameView() {
        return postNameView;
    }

    /** Returns the post's caption.
     *
     * @return a {@link TextView TextView} object.
     * */
    public TextView getPostCaptionView() {
        return postCaptionView;
    }

    /** Returns the post's title.
     *
     * @return a {@link TextView TextView} object.
     * */
    public TextView getPostTitleView() {
        return postTitleView;
    }

    /** Returns the post's distance.
     *
     * @return a {@link TextView TextView} object.
     * */
    public TextView getPostDistanceValueView() {
        return postDistanceValueView;
    }

    /** Returns the post's time.
     *
     * @return a {@link TextView TextView} object.
     * */
    public TextView getPostTimeValueView() {
        return postTimeValueView;
    }

    /** Returns the post's speed.
     *
     * @return a {@link TextView TextView} object.
     * */
    public TextView getPostSpeedValueView() {
        return postSpeedValueView;
    }

    /** Returns the post's date.
     *
     * @return a {@link TextView TextView} object.
     * */
    public TextView getPostDate() {
        return postDate;
    }

    /** Returns the post's likes.
     *
     * @return a {@link TextView TextView} object.
     * */
    public TextView getLikes() {
        return likes;
    }

    /** Returns the post's profile image.
     *
     * @return a {@link ImageView ImageView} object.
     * */
    public ImageView getProfileImage() {
        return profileImage;
    }

    /** Returns the post's like button.
     *
     * @return a {@link ImageButton ImageButton} object.
     * */
    public ImageButton getLikeButton() {
        return likeButton;
    }

    /** Returns the post's comment button.
     *
     * @return a {@link ImageButton ImageButton} object.
     * */
    public ImageButton getCommentButton() {
        return commentButton;
    }

    /** Returns the post's map.
     *
     * @return a {@link MapView MapView} object.
     * */
    public MapView getMapView() {
        return mapView;
    }

    /** Returns the post's delete button.
     *
     * @return a {@link ImageButton ImageButton} object.
     * */
    public ImageButton getDeleteButton() {
        return deleteButton;
    }
}


