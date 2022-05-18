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
import com.example.strinder.backend_related.storage.DropBoxServices;
import com.example.strinder.backend_related.tables.User;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

public class FollowAdapter extends RecyclerView.Adapter<FollowViewHolder> {

    private final List<User> users;
    private final Context context;
    private final ServerConnection connection;
    private final User user;


    public FollowAdapter(final Context context, List<User> users, final User user) {
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
                holder.getFriendName().setText(String.format("%s %s", currentUser.getFirstName(),
                        currentUser.getLastName()));

                holder.getFriendBiography().setText(currentUser.getBiography());
                System.out.println(currentUser.getPhotoUrl());
                if (currentUser.getPhotoUrl() != null) {
                    //This ensures that the image always is set to the newly uploaded one. Picasso ignores (by default) identical URLs.
                    if (context != null) {
                        Picasso.with(context.getApplicationContext()).
                                invalidate(DropBoxServices.getUserImagePath(currentUser));
                    }

                    Picasso.with(context).load(currentUser.getPhotoUrl())
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .placeholder(android.R.drawable.sym_def_app_icon)
                            .error(android.R.drawable.sym_def_app_icon)
                            .into(holder.getProfileImage());
                }

                updateButtonUI(holder,currentUser);

                holder.getAddFriendButton().setOnClickListener(view -> {

                    if(user.getFriends().contains(currentUser)) {
                        removeFriend(holder,currentUser);
                    }
                    else {
                        addFriend(holder,currentUser);
                    }
                });


        }



    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void updateButtonUI(final FollowViewHolder holder, final User currentUser) {
        if(context != null) {
            if (user.getFriends().contains(currentUser)) {
                DrawableCompat.setTint(holder.getAddFriendButton().getDrawable(),
                        context.getColor(R.color.selected));
            }
            else {
                DrawableCompat.setTint(holder.getAddFriendButton().getDrawable(),
                        context.getColor(R.color.papaya));
            }
        }
    }
    private void addFriend(final FollowViewHolder holder, final User currentUser){
        ServerConnection connection = new ServerConnection(context);

        connection.sendStringJsonRequest("/follow/" + currentUser.getId(),
                new JSONObject(),
                Request.Method.POST, user.getAccessToken(), new VolleyResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        User follow = gson.fromJson(response,User.class);
                        user.addFriend(follow);

                        updateButtonUI(holder,currentUser);
                        Log.i("Add Success",
                                "Successfully added user to your follow list.");
                        Toast.makeText(context,"Successfully added user!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.e("Add Fail", "Failed to add user to follow list");
                        Toast.makeText(context,"Failed to add user. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeFriend(final FollowViewHolder holder, final User currentUser) {
        connection.sendStringJsonRequest("/follow/remove/" + currentUser.getId(),
                new JSONObject(),
                Request.Method.POST, user.getAccessToken(), new VolleyResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        user.removeFriend(currentUser);

                        updateButtonUI(holder,currentUser);
                        Log.i("Remove Success",
                                "Successfully removed user from your follow list.");
                        Toast.makeText(context,"Successfully removed user!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.e("Remove Fail", "Failed to remove user from your follow list");
                        Toast.makeText(context,"Failed to remove user. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

class FollowViewHolder extends RecyclerView.ViewHolder {
    private final ImageView profileImage;
    private final TextView friendName,friendBiography;
    private final ImageButton addFriendButton;

    public FollowViewHolder(@NonNull View itemView) {
        super(itemView);
        this.profileImage = itemView.findViewById(R.id.friendImage);
        this.friendName = itemView.findViewById(R.id.userCardName);
        this.friendBiography = itemView.findViewById(R.id.userCardBio);
        this.addFriendButton = itemView.findViewById(R.id.addFriendButton);
    }

    public ImageView getProfileImage() {
        return profileImage;
    }

    public TextView getFriendName() {
        return friendName;
    }

    public TextView getFriendBiography() {
        return friendBiography;
    }

    public ImageButton getAddFriendButton() {
        return addFriendButton;
    }
}
