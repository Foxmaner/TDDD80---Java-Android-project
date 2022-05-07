package com.example.strinder.logged_in;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.TrainingSession;
import com.example.strinder.backend_related.tables.User;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.MyViewHolder>{
    private final Context context;
    private final List<Post> posts;
    private final List<User> users;

    public PostRecyclerViewAdapter(Context context, List<Post> posts, List<User> users){
        this.context=context;
        this.posts = posts;
        this.users = users;
    }


    @NonNull
    @Override
    public PostRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.post_card,parent,false);
        return new PostRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostRecyclerViewAdapter.MyViewHolder holder, int position) {

        //holder.postCaptionView.setText(postModels.get(position).getCaption());
        Post post = posts.get(position);
        User user;

        //In case we know that all posts belong to one individual. The profile is one such case.
        if(users.size() == 1) {
            user = users.get(0);
        }
        else {
            user = users.get(position);
        }

        TrainingSession session = post.getTrainingSession();

        holder.postNameView.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
        //We do not want to reload the image.
        Picasso.with(context).load(user.getPhotoUrl())
                .placeholder(android.R.drawable.sym_def_app_icon)
                .error(android.R.drawable.sym_def_app_icon)
                .into(holder.profileImage);



        holder.postCaptionView.setText(post.getCaption());
        holder.postTitleView.setText(post.getTitle());
        holder.postDate.setText(post.getDate());

        if (session != null) {
            holder.postExercise.setText(session.getExercise());
            holder.postDistanceValueView.setText(String.format("%s %s", session.getDistance(),
                    session.getDistanceUnit()));
            holder.postTimeValueView.setText(session.getElapsedTime());
            holder.postSpeedValueView.setText(String.format("%s %s", session.getSpeed(), session.getSpeedUnit()));

        }

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private final TextView postExercise;
        private final TextView postNameView;
        private final TextView postCaptionView;
        private final TextView postTitleView;
        private final TextView postDistanceValueView;
        private final TextView postTimeValueView;
        private final TextView postSpeedValueView;
        private final TextView postDate;
        private final ImageView profileImage;


        public MyViewHolder(@NonNull View itemView) {
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


        }
    }
}
