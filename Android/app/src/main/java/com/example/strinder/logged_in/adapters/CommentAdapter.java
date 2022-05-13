package com.example.strinder.logged_in.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.strinder.R;
import com.example.strinder.backend_related.tables.Comment;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder>{

    private final List<Comment> comments;
    private final Context context;

    public CommentAdapter(final Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
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

            /*
            //We do not want to reload the image.
            Picasso.with(context).load(comment.getPhotoUrl())
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(holder.profileImage);
            */

            holder.getCommentText().setText(currentComment.getText());

        }

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}


class CommentViewHolder extends RecyclerView.ViewHolder {
    private final ImageView profileImage;
    private final TextView commentText;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        this.profileImage = itemView.findViewById(R.id.commentCardProfileImage);
        this.commentText = itemView.findViewById(R.id.commentCardText);
    }

    public ImageView getProfileImage() {
        return profileImage;
    }

    public TextView getCommentText() {
        return commentText;
    }
}