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

/** This class extends the {@link androidx.recyclerview.widget.RecyclerView.Adapter
 * RecyclerView.Adapter} class and is used to customize a RecyclerView for a {@link List<Comment>
 * List<Comment>}. The class allows us to handle button presses and different kinds of
 * data on each any every comment.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder>{

    private final List<Comment> comments;
    private final Context context;
    private final ServerConnection connection;
    private final User user;

    /** Initialize a CommentAdapter object.
     *
     * @param context - a {@link Context context} object.
     * @param comments - a {@link List<Comment> List<Comment>} object. These are the comments that
     *                 will be displayed in the {@link RecyclerView RecyclerView}.
     * @param user - the logged-in {@link User User} object.
     *
     */
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
            //Get the Comment object that we are at right now in the list.
            Comment currentComment = comments.get(position);

            connection.sendStringJsonRequest("/user/get_user/" + currentComment.getUserId(),
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
                            connection.maybeDoRefresh(error,user);
                            Log.e("Fetch User Error", "Failed to fetch user data");
                            Toast.makeText(context,"Failed to fetch user data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });


        }

        if(this.user.getId() == this.comments.get(position).getUserId()){
            holder.getDeleteButton().setVisibility(View.VISIBLE);

            holder.getDeleteButton().setOnClickListener(view ->
                    connection.sendStringJsonRequest("/del/comment/" +
                                    this.comments.get(position).getId(),
                            new JSONObject(), Request.Method.DELETE, this.user.getAccessToken(),
                            new VolleyResponseListener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(context, "Comment removed",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(VolleyError error) {
                                    connection.maybeDoRefresh(error,user);
                                    Log.e("Like Post Error", "Error occurred when " +
                                            "deleting comment.");
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

/** This class is used within the {@link com.example.strinder.logged_in.adapters.CommentAdapter
 * CommentAdapter} class. In this class we find and define all the different {@link View View}
 * objects.
 */
class CommentViewHolder extends RecyclerView.ViewHolder {
    private final ImageView profileImage;
    private final TextView commentText, name;
    private final ImageButton deleteButton;

    /** Initialize a CommentViewHolder object
     * @param itemView - a {@link View View} object that lets us find the children {@link View View}
     *                 objects.
     */
    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        this.profileImage = itemView.findViewById(R.id.commentCardProfileImage);
        this.commentText = itemView.findViewById(R.id.commentCardText);
        this.name = itemView.findViewById(R.id.commentCardName);
        this.deleteButton = itemView.findViewById(R.id.buttonDeleteComment);
    }

    /** Returns the profile image.
     *
     * @return a {@link ImageView ImageView} object.
     */
    public ImageView getProfileImage() {
        return profileImage;
    }

    /** Returns the comment text.
     *
     * @return a {@link TextView TextView} object.
     */
    public TextView getCommentText() {
        return commentText;
    }

    /** Returns the name of the User.
     *
     * @return a {@link TextView TextView} object.
     */
    public TextView getName() {
        return name;
    }

    /** Returns the delete button.
     *
     * @return a {@link ImageButton ImageButton} object.
     */
    public ImageButton getDeleteButton() {
        return deleteButton;
    }
}