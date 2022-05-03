package com.example.strinder.backend_related.tables;

import android.os.Parcel;
import android.os.Parcelable;

public class TrainingSession implements Parcelable {
    private final int id,postId;
    private final String elapsedTime;
    private final String speedUnit,exercise;
    private final float speed;

    protected TrainingSession(Parcel in) {
        id = in.readInt();
        elapsedTime = in.readString();
        postId = in.readInt();
        speedUnit = in.readString();
        speed = in.readFloat();
        exercise = in.readString();
    }



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
    }
}
