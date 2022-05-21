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
import java.util.Objects;

/** This class is a representation of the database table User.
 *  When requesting User data, it can be thrown into an instance
 *  of this class by using {@link com.google.gson.Gson}.
 */
public class User implements Parcelable {

    private String firstName;
    private String lastName;
    private final String email;
    private final ArrayList<User> follows;
    private final ArrayList<Post> posts;
    private final String username;
    private String birthday;
    private String gender;
    private String photoUrl;
    private String biography;
    private final int id;
    private String accessToken;
    private final String refreshToken;

    /** Initialize a User object.
     *
     * @param parcel - the Parcel object needed when transferring data between a
     * {@link androidx.fragment.app.Fragment Fragment} / {@link android.app.Activity Activity}
     */
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

        follows = new ArrayList<>();
        parcel.readList(follows,User.class.getClassLoader());
        posts = new ArrayList<>();
        parcel.readList(posts,Post.class.getClassLoader());

        accessToken = parcel.readString();
        refreshToken = parcel.readString();
    }

    /** Returns the firstname of the User
     *
     * @return the firstname as a {@link String String} object.
     */
    public String getFirstName() {
        return firstName;
    }

    /** Returns the lastname of the User.
     *
     * @return the lastname as a {@link String string} object.
     */
    public String getLastName() {
        return lastName;
    }

    /** Returns the url of the User's photo.
     *
     * @return the url as a {@link String String} object.
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    //This method is not removed because we might want to use it if we continue the development.
    /** Returns the email of the User.
     *
     * @return the email as a {@link String String} object.
     */
    public String getEmail() {
        return email;
    }

    /** Returns the username of the User.
     *
     * @return the username as a {@link String String} object.
     */
    public String getUsername() {
        return username;
    }

    /** Returns the birthday date of the User.
     *
     * @return the birthday date as a {@link String String} object.
     */
    public String getBirthday() {
        return birthday;
    }

    /** Returns the gender of the User.
     *
     * @return the gender as a {@link String String} object.
     */
    public String getGender() {
        return gender;
    }

    /** Returns the access token of the User.
     *
     * @return the access token as a {@link String String} object.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /** Returns the refresh token of the User.
     *
     * @return the refresh token as a {@link String String} object.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /** Returns the id of the User.
     *
     * @return the id as an integer.
     */
    public int getId() {
        return id;
    }

    /** Returns a {@link java.util.List<User> List<User>} containing the people that you follow.
     *
     * @return a {@link List<User> List} object.
     */
    public List<User> getFriends() {
        return follows;
    }

    /** Returns a {@link java.util.List<Post> List<User>} containing all your posted
     * {@link Post posts}.
     *
     * @return a {@link List<Post> List} object.
     */
    public List<Post> getPosts() {
        return posts;
    }

    /** Returns the User's biography.
     *
     * @return the biography as a {@link String String object}.
     */
    public String getBiography() {
        return biography;
    }

    /** Uploads the User's 'changeable' data to the database. This includes firstname,lastname,
     * birthday,gender,biography,photo url and biography.
     * @param context - a {@link Context Context} object that is needed to create a server
     *                connection.
     * @param listener - a {@link VolleyResponseListener<String> VolleyResponseListener<String>}
     *                 object that allows the developer to catch the error or response when the
     *                 server has responded.
     */
    public void uploadData(final Context context,
                           final VolleyResponseListener<String> listener){

        if(context == null || accessToken == null || listener == null) {
            throw new IllegalArgumentException("The context,token or the listener was null," +
                    " not a valid argument.");
        }

        ServerConnection connection = new ServerConnection(context);
        JSONObject object = new JSONObject();
        //Upload the data that actually can change.
        try {
            object.put("photo_url",this.photoUrl == null ? JSONObject.NULL : this.photoUrl);
            object.put("first_name", this.firstName == null ? JSONObject.NULL : this.firstName);
            object.put("last_name", this.lastName == null ? JSONObject.NULL : this.lastName);
            object.put("birthday", this.birthday == null ? JSONObject.NULL : this.birthday);
            object.put("gender", this.gender == null ? JSONObject.NULL : this.gender);
            object.put("biography", this.biography == null ? JSONObject.NULL : this.biography);
        }
        catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context,"Failed to change data in database.",Toast.LENGTH_SHORT).
                    show();
        }

        connection.sendStringJsonRequest("/user/set_data",object, Request.Method.POST,
                getAccessToken(), listener);

    }

    /** Sets the User's photo url.
     * @param photoUrl - the new photo url.
     */
    public void setPhotoUrl(final String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /** Sets the User's first name.
     * @param firstName - the new first name.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** Sets the User's last name.
     *
     * @param lastName - the new last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /** Sets the User's birthday date.
     *
     * @param birthday - the new birthday date.
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    /** Sets the User's gender.
     *
     * @param gender - the new gender.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /** Sets the User's biography.
     *
     * @param biography - the new biography.
     */
    public void setBiography(String biography) {
        this.biography = biography;
    }

    /** Sets the access token that is needed to communicate with the backend.
     *
     * @param accessToken - the new access token.
     */
    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    /** Allows the User to follow another {@link User User} object.
     *
     * @param user - a {@link User User} object that the User shall follow.
      */
    public void follow(final User user) {
        follows.add(user);
    }

    /** Allows the User to unfollow another {@link User User} object.
     *
     * @param user - a {@link User User} object that the User follows.
     */
    public void unfollow(final User user) {
        follows.remove(user);
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
        parcel.writeList(follows);
        parcel.writeList(posts);
        parcel.writeString(accessToken);
        parcel.writeString(refreshToken);
    }

    /** This method is used when transferring the object between
     * {@link androidx.fragment.app.Fragment} / {@link android.app.Activity}
     */
    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {

            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return id == user.getId() && Objects.equals(firstName, user.getFirstName()) &&
                Objects.equals(lastName, user.getLastName()) && username.equals(user.getUsername());
    }

}
