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
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.strinder.R;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;
import com.example.strinder.backend_related.tables.Comment;
import com.example.strinder.backend_related.tables.User;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder>{

    private final List<Comment> comments;
    private final Context context;
    private final ServerConnection connection;
    private final User user;

    public CommentAdapter(final Context context, List<Comment> comments, final User user) {
        this.context = context;
        this.comments = comments;
        this.connection = new ServerConnection(context);
        this.user = user;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comment_card, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        if(comments.size() > position) {
            Comment currentComment = comments.get(position);

            connection.sendStringJsonRequest("/user/get_data/" + currentComment.getUserId(),
                    new JSONObject(), Request.Method.GET, user.getAccessToken(),
                    new VolleyResponseListener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Gson gson = new Gson();
                            User commentUser = gson.fromJson(response,User.class);

                            //Display the user's full name.
                            holder.getName().setText(String.format("%s %s",
                                    commentUser.getFirstName(),commentUser.getLastName()));

                            //We do not want to reload the image.
                            Picasso.with(context).load(commentUser.getPhotoUrl())
                                    .placeholder(android.R.drawable.sym_def_app_icon)
                                    .error(android.R.drawable.sym_def_app_icon)
                                    .into(holder.getProfileImage());


                            holder.getCommentText().setText(currentComment.getText());
                        }

                        @Override
                        public void onError(VolleyError error) {

                        }
                    });


        }

        if(this.user.getId() == this.comments.get(position).getUserId()){
            holder.getDeleteButton().setVisibility(View.VISIBLE);

            holder.getDeleteButton().setOnClickListener(view ->
                    connection.sendStringJsonRequest("/del/comment/" + String.valueOf(this.comments.get(position).getId()),
                            new JSONObject(), Request.Method.DELETE, this.user.getAccessToken(),
                            new VolleyResponseListener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(context, "Comment removed",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(VolleyError error) {
                                    Log.e("Like Post Error", "Error occurred when deleting comment.");
                                    Toast.makeText(context, "Failed to delete post.",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }));
        }






    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}


class CommentViewHolder extends RecyclerView.ViewHolder {
    private final ImageView profileImage;
    private final TextView commentText, name;
    private final ImageButton deleteButton;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        this.profileImage = itemView.findViewById(R.id.commentCardProfileImage);
        this.commentText = itemView.findViewById(R.id.commentCardText);
        this.name = itemView.findViewById(R.id.commentCardName);
        this.deleteButton = itemView.findViewById(R.id.buttonDeleteComment);
    }

    public ImageView getProfileImage() {
        return profileImage;
    }

    public TextView getCommentText() {
        return commentText;
    }

    public TextView getName() {
        return name;
    }

    public ImageButton getDeleteButton() {
        return deleteButton;
    }
}