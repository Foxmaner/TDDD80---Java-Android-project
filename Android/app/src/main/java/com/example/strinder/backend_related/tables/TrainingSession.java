package com.example.strinder.backend_related.tables;

import android.os.Parcel;
import android.os.Parcelable;
/** This class is a representation of the database table TrainingSession.
 *  When requesting TrainingSession data, it can be thrown into an instance
 *  of this class by using {@link com.google.gson.Gson}.
 */
public class TrainingSession implements Parcelable {
    private final int id,postId;
    private final String elapsedTime,speedUnit,distanceUnit, exercise;
    private final float speed,distance;

    /** Initialize a TrainingSession object.
     *
     * @param parcel - the Parcel object needed when transferring data between a
     * {@link androidx.fragment.app.Fragment Fragment} / {@link android.app.Activity Activity}
     */
    private TrainingSession(Parcel parcel) {
        id = parcel.readInt();
        elapsedTime = parcel.readString();
        postId = parcel.readInt();
        speedUnit = parcel.readString();
        speed = parcel.readFloat();
        exercise = parcel.readString();
        distance = parcel.readFloat();
        distanceUnit = parcel.readString();
    }

    /** Returns the id of the {@link com.example.strinder.backend_related.tables.TrainingSession
     * TrainingSession}.
     *
     * @return the id as an integer.
     */
    public int getId() {
        return id;
    }

    //This method is not removed because we might want to use it if we continue the development.
    /** Returns the id of the {@link com.example.strinder.backend_related.tables.Post Post} that the
     * TrainingSession belongs to.
     * @return the id as an integer.
     */
    public int getPostId() {
        return postId;
    }

    /** Returns the elapsed time of the exercise.
     *
     * @return the elapsed time as a {@link String String} object.
     */
    public String getElapsedTime() {
        return elapsedTime;
    }

    /** Returns the speed unit of the exercise.
     *
     * @return the speed unit as a {@link String String} object.
     */
    public String getSpeedUnit() {
        return speedUnit;
    }

    /** Returns the exercise of the TrainingSession.
     *
     * @return the exercise as a {@link String String} object.
     */
    public String getExercise() {
        return exercise;
    }

    /** Returns the speed of the exercise.
     *
     * @return the speed as a float value.
     */
    public float getSpeed() {
        return speed;
    }

    /** Returns the distance unit of the exercise.
     *
     * @return the distance unit as a {@link String String} object.
     */
    public String getDistanceUnit() {
        return distanceUnit;
    }

    /** Returns the distance of the exercise.
     *
     * @return the distance as a float value.
     */
    public float getDistance() {
        return distance;
    }

    /** This method is used when transferring the object between
     * {@link androidx.fragment.app.Fragment} / {@link android.app.Activity}
     */
    public static final Creator<TrainingSession> CREATOR = new Creator<TrainingSession>() {
        @Override
        public TrainingSession createFromParcel(Parcel in) {
            return new TrainingSession(in);
        }

        @Override
        public TrainingSession[] newArray(int size) {
            return new TrainingSession[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(elapsedTime);
        parcel.writeInt(postId);
        parcel.writeString(speedUnit);
        parcel.writeFloat(speed);
        parcel.writeString(exercise);
        parcel.writeFloat(distance);
        parcel.writeString(distanceUnit);
    }
}
