package com.example.strinder.backend_related;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Gender;

import org.json.JSONException;
import org.json.JSONObject;


public class User implements Parcelable {

    private String firstName;
    private String lastName;
    private String email;
    private final String username;
    private String birthday;
    private String gender;
    //TODO Implement photoUrl in database, not sure how this will work out right now. We need a
    //TODO solution for the uploading of images before this.
    private String photoUrl;
    private String biography;
    private final int id;

    public User(final String firstName, final String lastName, final String email,
                final String username, final String photoUrl, final String gender,
                final String birthday, final int id) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.photoUrl = photoUrl;
        this.birthday = birthday;
        this.gender = gender;
        this.id = id;
        //Biography is always empty from the start.
        this.biography = "";

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

    public int getId() {
        return id;
    }

    public String getBiography() {
        return biography;
    }

    public void setAndUploadData(final Context context, final String token,
                                 final VolleyResponseListener listener, final String firstName,
                                 final String lastName, final String email,
                                 final String birthday, final String gender,
                                 final String biography){

        if(context == null || token == null || listener == null) {
            throw new IllegalArgumentException("The context,token or the listener was null," +
                    " not a valid argument.");
        }
        if(firstName != null) {
            this.firstName = firstName;
        }

        if(lastName != null) {
            this.lastName = lastName;
        }

        if(email != null) {
            this.email = email;
        }

        if(birthday != null) {
            this.birthday = birthday;
        }

        if(gender != null) {
            this.gender = gender;
        }

        if(biography != null) {
            this.biography = biography;
        }

        ServerConnection connection = new ServerConnection(context);
        JSONObject object = new JSONObject();
        try {
            object.put("first_name", this.firstName);
            object.put("last_name", this.lastName);
            object.put("email", this.email);
            object.put("birthday", this.birthday);
            object.put("gender", this.gender);
            object.put("biography", this.biography);
        }
        catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context,"Failed to change data in database.",Toast.LENGTH_SHORT).
                    show();
        }

        connection.sendStringJsonRequest("/user/set_data",object, Request.Method.POST,token,
                listener);

    }

    public void setAndUploadData(final Context context, final String token,
                            final VolleyResponseListener listener) {
        this.setAndUploadData(context,token,listener,null,null,null,
                null,null,null);
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
        parcel.writeInt(id);
    }

    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            User user = new User(in.readString(),in.readString(),in.readString(),in.readString(),
                    in.readString(),in.readString(),in.readString(),in.readInt());

            return user;
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

}
