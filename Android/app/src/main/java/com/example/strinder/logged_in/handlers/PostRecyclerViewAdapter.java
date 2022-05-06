package com.example.strinder.logged_in.handlers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.strinder.R;
import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.TrainingSession;

import java.util.List;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.MyViewHolder>{
    private final Context context;
    private final List<Post> posts;


    public PostRecyclerViewAdapter(Context context, List<Post> posts){
        this.context=context;
        this.posts = posts;
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
        holder.postNameView.setText(String.format("%s",posts.get(position).getId()));
        //holder.postCaptionView.setText(postModels.get(position).getCaption());
        TrainingSession session = posts.get(position).getTrainingSession();

        if(session != null) {
            //FIXME We do not know the distance field atm.
            holder.postDistanceValueView.setText(String.format("%s %s",session.getDistance(),
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
        TextView postNameView;
        //TextView postCaptionView;
        TextView postDistanceValueView;
        TextView postTimeValueView;
        TextView postSpeedValueView;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            postNameView = itemView.findViewById(R.id.postNameView);
            //postCaptionView = itemView.findViewById(R.id.postDistanceTextView);
            postDistanceValueView = itemView.findViewById(R.id.postDistanceValueView);
            postTimeValueView = itemView.findViewById(R.id.postTimeValueView);
            postSpeedValueView = itemView.findViewById(R.id.postSpeedValueView);

        }
    }
}
