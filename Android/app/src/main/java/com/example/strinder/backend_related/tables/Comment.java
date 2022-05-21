package com.example.strinder.backend_related.tables;

import android.os.Parcel;
import android.os.Parcelable;

/** This class is a representation of the database table Comment. When requesting Comment data, it
 * can be thrown into an instance of this class by using {@link com.google.gson.Gson}.
 */
public class Comment implements Parcelable {
    private final int id,userId,postId;
    private final String text;

    /** Initialize a Comment object.
     *
     * @param parcel - the Parcel object needed when transferring data between a
     * {@link androidx.fragment.app.Fragment Fragment} / {@link android.app.Activity Activity}
     */
    private Comment(final Parcel parcel) {

        id = parcel.readInt();
        userId = parcel.readInt();
        postId = parcel.readInt();
        text = parcel.readString();
    }
    /** Returns the id of this comment.
     * @return return the id as an integer.
     */
    public int getId() {
        return id;
    }

    /** Returns the id of the {@link com.example.strinder.backend_related.tables.User} that created
     * the comment.
     * @return the id as an integer.
     */
    public int getUserId() {
        return userId;
    }

    //This method is not removed because we might want to use it if we continue the development.
    /** Returns the id of the {@link com.example.strinder.backend_related.tables.Post} that the
     * comment belongs to.
     * @return the id as an integer.
     */
    @SuppressWarnings("unused")
    public int getPostId() {
        return postId;
    }

    /** Returns the text of the comment.
     *
     * @return the text as a {@link String} object.
     */
    public String getText() {
        return text;
    }

    /** This method is used when transferring the object between
     * {@link androidx.fragment.app.Fragment} / {@link android.app.Activity}
     */
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
    }
}
