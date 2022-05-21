package com.example.strinder.backend_related.tables;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/** This class is a representation of the database table Post. When requesting Post data, it
 * can be thrown into an instance of this class by using {@link com.google.gson.Gson}.
 */
public class Post implements Parcelable {

    private final int id,userId;
    private final String title,caption;
    private final String date;
    private List<User> likes;
    private List<Comment> comments;
    private TrainingSession trainingSession;
    private final double latitude,longitude;

    /** Initializes a Post object
     *
     * @param parcel - the Parcel object needed when transferring data between a
     * {@link androidx.fragment.app.Fragment Fragment} / {@link android.app.Activity Activity}
     */
    private Post(final Parcel parcel) {
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

        latitude = parcel.readDouble();
        longitude = parcel.readDouble();

    }

    /** Returns the id of the post
     *
     * @return the id as an integer.
     */
    public int getId() {
        return id;
    }

    /** Returns the title of the Post.
     *
     * @return the title as a {@link String String} object.
     */
    public String getTitle() {
        return title;
    }

    /** Returns the caption of the Post.
     *
     * @return the caption as a {@link String String} object.
     */
    public String getCaption() {
        return caption;
    }

    /** Returns the date of the Post
     *
     * @return the date as a {@link String String} object.
     */
    public String getDate() {
        return date;
    }

    /** Returns a {@link java.util.List<User> List<User>} containing the people that have liked the post.
     *
     * @return a {@link List<User> List} object.
     */
    public List<User> getLikes() {
        return likes;
    }

    /** Returns a {@link java.util.List<Comment> List<Comment>} containing the comments on the post.
     *
     * @return a {@link List<Comment> List<Comment>} object.
     */
    public List<Comment> getComments() {
        return comments;
    }

    /** Returns the latitude of the position in which the post was created on.
     *
     * @return the latitude as a double.
     */
    public double getLatitude() {
        return latitude;
    }

    /** Returns the longitude of the position in which the post was created on.
     *
     * @return the latitude as a double.
     */
    public double getLongitude() {
        return longitude;
    }

    /** Sets the comments on the post to the specified {@link List<Comment> List<Comment} parameter.
     *
     * @param comments - a {@link List<Comment> List<Comment>} object.
     */
    public void setComments(final List<Comment> comments) {
        this.comments = comments;
    }

    /** Sets the likes on the post to the specified {@link List<User> List<User>} parameter.
     *
     * @param likes - a  {@link List<User> List<User>} object.
     */
    public void setLikes(final List<User> likes) {
        this.likes = likes;
    }

    /** Returns the {@link com.example.strinder.backend_related.tables.TrainingSession
     *  TrainingSession} of the post.
     *  @return a {@link TrainingSession TrainingSession} object.
     */
    public TrainingSession getTrainingSession() {
        return trainingSession;
    }

    /** Sets the TrainingSession of the post to the specified
     * {@link com.example.strinder.backend_related.tables.TrainingSession TrainingSession} parameter
     * @param trainingSession - a {@link TrainingSession TrainingSession} object.
     */
    public void setTrainingSession(final TrainingSession trainingSession) {
        this.trainingSession = trainingSession;
    }

    /** This method is used when transferring the object between
     * {@link androidx.fragment.app.Fragment} / {@link android.app.Activity}
     */
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
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return id == post.id && userId == post.userId && title.equals(post.title) && caption.equals(post.caption) && date.equals(post.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, title, caption, date);
    }
}
