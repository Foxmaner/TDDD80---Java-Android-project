package com.example.strinder.backend_related.tables;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.android.volley.Request;
import com.example.strinder.backend_related.database.ServerConnection;
import com.example.strinder.backend_related.database.VolleyResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final ArrayList<User> friends;
    private final ArrayList<Post> posts;
    private final String username;
    private final String birthday;
    private final String gender;
    private String photoUrl;
    private final String biography;
    private final int id;
    private final String accessToken;

    private User(final Parcel parcel) {
        this.firstName = parcel.readString();
        this.lastName = parcel.readString();
        this.email = parcel.readString();
        this.username = parcel.readString();
        this.birthday = parcel.readString();
        this.gender = parcel.readString();
        this.biography = parcel.readString();
        this.photoUrl = parcel.readString();
        this.id = parcel.readInt();

        friends = new ArrayList<>();
        parcel.readList(friends,User.class.getClassLoader());
        posts = new ArrayList<>();
        parcel.readList(posts,Post.class.getClassLoader());


        accessToken = parcel.readString();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getGender() {
        return gender;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public int getId() {
        return id;
    }

    public List<User> getFriends() {
        return friends;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public String getBiography() {
        return biography;
    }


    public void uploadData(final Context context,
                           final VolleyResponseListener<String> listener){

        if(context == null || accessToken == null || listener == null) {
            throw new IllegalArgumentException("The context,token or the listener was null," +
                    " not a valid argument.");
        }

        ServerConnection connection = new ServerConnection(context);
        JSONObject object = new JSONObject();
        try {
            object.put("photo_url",this.photoUrl);
            object.put("first_name", this.firstName);
            object.put("last_name", this.lastName);
            object.put("birthday", this.birthday);
            object.put("gender", this.gender);
            object.put("biography", this.biography);
        }
        catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context,"Failed to change data in database.",Toast.LENGTH_SHORT).
                    show();
        }

        connection.sendStringJsonRequest("/user/set_data",object, Request.Method.POST,
                getAccessToken(), listener);

    }

    public void setPhotoUrl(final String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(email);
        parcel.writeString(username);
        parcel.writeString(birthday);
        parcel.writeString(gender);
        parcel.writeString(biography);
        parcel.writeString(photoUrl);
        parcel.writeInt(id);
        parcel.writeList(friends);
        parcel.writeList(posts);
        parcel.writeString(accessToken);
    }

    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {

            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

}
