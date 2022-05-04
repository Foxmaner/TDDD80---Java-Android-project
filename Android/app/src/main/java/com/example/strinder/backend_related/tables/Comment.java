package com.example.strinder.backend_related.tables;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Comment implements Parcelable {
    private final int id,userId,postId;
    private final String text;
    private final List<User> likes;

    public Comment(final Parcel parcel) {

        id = parcel.readInt();
        userId = parcel.readInt();
        postId = parcel.readInt();
        text = parcel.readString();
        likes = new ArrayList<>();
        parcel.readList(likes,User.class.getClassLoader());
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getPostId() {
        return postId;
    }

    public String getText() {
        return text;
    }

    public List<User> getLikes() {
        return likes;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
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
        parcel.writeInt(postId);
        parcel.writeString(text);
        parcel.writeList(likes);
    }
}
