package com.example.strinder.backend_related.tables;

import android.os.Parcel;
import android.os.Parcelable;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.List;

public class Post implements Parcelable {

    private final int id,userId;
    private final String title,caption;
    private final String date;
    private final List<User> likes;
    private final List<Comment> comments;
    private TrainingSession trainingSession;

    public Post(final Parcel parcel) {
        id = parcel.readInt();
        userId = parcel.readInt();
        title = parcel.readString();
        caption = parcel.readString();
        date = parcel.readString();
        likes = new ArrayList<>();
        parcel.readList(likes,User.class.getClassLoader());
        comments = new ArrayList<>();
        parcel.readList(comments,Comment.class.getClassLoader());

        trainingSession = parcel.readParcelable(TrainingSession.class.getClassLoader());

    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getCaption() {
        return caption;
    }

    public String getDate() {
        return date;
    }

    public List<User> getLikes() {
        return likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public TrainingSession getTrainingSession() {
        return trainingSession;
    }

    public void setTrainingSession(final TrainingSession trainingSession) {
        this.trainingSession = trainingSession;
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(userId);
        parcel.writeString(title);
        parcel.writeString(caption);
        parcel.writeString(date);
        parcel.writeList(likes);
        parcel.writeList(comments);
        parcel.writeParcelable(trainingSession,i);
    }


}
