package com.example.strinder.logged_in.handlers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.strinder.R;

import java.util.ArrayList;

public class PostModel_RecyclerViewAdapter extends RecyclerView.Adapter<PostModel_RecyclerViewAdapter.MyViewHolder>{
    Context context;
    ArrayList<PostModel> postModels;


    public PostModel_RecyclerViewAdapter(Context context, ArrayList<PostModel> postModels){
        this.context=context;
        this.postModels = postModels;
    }

    @NonNull
    @Override
    public PostModel_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.post_card,parent,false);
        return new PostModel_RecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostModel_RecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.postNameView.setText(postModels.get(position).getName());
        //holder.postCaptionView.setText(postModels.get(position).getCaption());
        holder.postDistanceValueView.setText(postModels.get(position).getDistance());
        holder.postTimeValueView.setText(postModels.get(position).getTime());
        holder.postSpeedValueView.setText(postModels.get(position).getSpeed());
    }

    @Override
    public int getItemCount() {
        return postModels.size();
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
