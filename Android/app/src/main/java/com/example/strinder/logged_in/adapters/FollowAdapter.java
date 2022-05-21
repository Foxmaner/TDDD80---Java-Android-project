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
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.User;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

/** This class extends the {@link androidx.recyclerview.widget.RecyclerView.Adapter
 * RecyclerView.Adapter} class and is used to customize a RecyclerView for a {@link List<User>
 * List<User>}. The class allows us to handle button presses and different kinds of
 * data on each any every user.
 */
public class FollowAdapter extends RecyclerView.Adapter<FollowViewHolder> {

    private final List<User> users;
    private final Context context;
    private final ServerConnection connection;
    private final User user;

    /** Initialize a FollowAdapter object.
     *
     * @param context - a {@link Context context} object.
     * @param users - a {@link List<User> List<User>} object. These are the users that
     *                 will be displayed in the {@link RecyclerView RecyclerView}
     * @param user - the logged-in {@link User User} object.
     *
     */
    public FollowAdapter(@NonNull final Context context, List<User> users, final User user) {
        this.context = context;
        this.users = users;
        this.connection = new ServerConnection(context);
        this.user = user;
    }


    @NonNull
    @Override
    public FollowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.user_card, parent, false);
        return new FollowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowViewHolder holder, int position) {

        if(users.size() > position) {
            User currentUser = users.get(position);
                holder.getFollowName().setText(String.format("%s %s", currentUser.getFirstName(),
                        currentUser.getLastName()));

                holder.getFollowBiography().setText(currentUser.getBiography());

                if (currentUser.getPhotoUrl() != null) {
                    //This ensures that the image always is set to the newly uploaded one. Picasso ignores (by default) identical URLs.
                    Picasso.with(context.getApplicationContext()).
                            invalidate(currentUser.getPhotoUrl());

                    Picasso.with(context).load(currentUser.getPhotoUrl())
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .placeholder(android.R.drawable.sym_def_app_icon)
                            .error(android.R.drawable.sym_def_app_icon)
                            .into(holder.getProfileImage());
                }

                updateButtonUI(holder,currentUser);

                holder.getFollowButton().setOnClickListener(view -> {

                    if(user.getFriends().contains(currentUser)) {
                        unFollowUser(holder,currentUser);
                    }
                    else {
                        followUser(holder,currentUser);
                    }
                });


        }



    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /** Updates the {@link android.widget.ImageButton ImageButton} depending on if you already
     * follow the selected {@link User User} or not.
     * @param holder - the {@link FollowViewHolder FollowViewHolder} object.
     * @param currentUser - the selected {@link User User} object.
     */
    private void updateButtonUI(final FollowViewHolder holder, final User currentUser) {
        if (user.getFriends().contains(currentUser)) {
            DrawableCompat.setTint(holder.getFollowButton().getDrawable(),
                    context.getColor(R.color.selected));
        }
        else {
            DrawableCompat.setTint(holder.getFollowButton().getDrawable(),
                    context.getColor(R.color.papaya));
        }
    }

    /** Follows a selected User.
     * @param holder - the {@link FollowViewHolder FollowViewHolder} object.
     * @param currentUser - the selected {@link User User} object that is to be followed.
     */
    private void followUser(final FollowViewHolder holder, final User currentUser){
        ServerConnection connection = new ServerConnection(context);

        connection.sendStringJsonRequest("/follow/" + currentUser.getId(),
                new JSONObject(),
                Request.Method.POST, user.getAccessToken(), new VolleyResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        User follow = gson.fromJson(response,User.class);
                        user.follow(follow);

                        updateButtonUI(holder,currentUser);
                        Log.i("Add Success",
                                "Successfully added user to your follow list.");
                        Toast.makeText(context,"Successfully added user!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        connection.maybeDoRefresh(error,user);
                        Log.e("Add Fail", "Failed to add user to follow list");
                        Toast.makeText(context,"Failed to add user. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Unfollows a selected User.
     * @param holder - the {@link FollowViewHolder FollowViewHolder} object.
     * @param currentUser - the selected {@link User User} object that is to be unfollowed.
     */
    private void unFollowUser(final FollowViewHolder holder, final User currentUser) {
        connection.sendStringJsonRequest("/follow/remove/" + currentUser.getId(),
                new JSONObject(),
                Request.Method.POST, user.getAccessToken(), new VolleyResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        user.unfollow(currentUser);

                        updateButtonUI(holder,currentUser);
                        Log.i("Remove Success",
                                "Successfully removed user from your follow list.");
                        Toast.makeText(context,"Successfully removed user!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        connection.maybeDoRefresh(error,user);
                        Log.e("Remove Fail", "Failed to remove user from your follow list");
                        Toast.makeText(context,"Failed to remove user. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

/** This class is used within the {@link com.example.strinder.logged_in.adapters.FollowAdapter
 * FollowAdapter} class. In this class we find and define all the different {@link View View}
 * objects.
 */
class FollowViewHolder extends RecyclerView.ViewHolder {
    private final ImageView profileImage;
    private final TextView followName, followBiography;
    private final ImageButton followButton;

    /** Initialize a FollowViewHolder object.
     *
     * @param itemView - a {@link View View} object that lets us find the children {@link View View}
     *                   objects.
     */
    public FollowViewHolder(@NonNull View itemView) {
        super(itemView);
        this.profileImage = itemView.findViewById(R.id.friendImage);
        this.followName = itemView.findViewById(R.id.userCardName);
        this.followBiography = itemView.findViewById(R.id.userCardBio);
        this.followButton = itemView.findViewById(R.id.addFriendButton);
    }

    /** Returns the profile image.
     *
     * @return a {@link ImageView ImageView} object.
     */
    public ImageView getProfileImage() {
        return profileImage;
    }

    /** Returns the User's name.
     *
     * @return a {@link TextView TextView} object.
     */
    public TextView getFollowName() {
        return followName;
    }

    /** Returns the User's biography.
     *
     * @return a {@link TextView TextView} object.
     */
    public TextView getFollowBiography() {
        return followBiography;
    }

    /** Returns the {@link ImageButton ImageButton} object that is used to follow / unfollow .
     *
     * @return a {@link ImageButton ImageView} object.
     */
    public ImageButton getFollowButton() {
        return followButton;
    }
}
