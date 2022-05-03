package com.example.strinder.backend_related;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private final String firstName;
    private final String lastName;
    private final String email;
    //private final ArrayList<String> friends;
    //private final ArrayList<String> posts;
    private final String username;
    private final String birthday;
    private final String gender;
    //TODO Implement photoUrl in database, not sure how this will work out right now. We need a
    //TODO solution for the uploading of images before this.
    private final String photoUrl;
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

        /*friends = new ArrayList<>();
        parcel.readList(friends,String.class.getClassLoader());
        posts = new ArrayList<>();
        parcel.readList(posts,String.class.getClassLoader());
        */

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

    /*public List<String> getFriends() {
        return friends;
    }

    public List<String> getPosts() {
        return posts;
    }

     */

    public String getBiography() {
        return biography;
    }

    /*
    public void setAndUploadData(final Context context, final String token,
                                 final VolleyResponseListener<String> listener, final String firstName,
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
    */

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

        /*
        parcel.writeList(friends);
        parcel.writeList(posts);
        */

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
