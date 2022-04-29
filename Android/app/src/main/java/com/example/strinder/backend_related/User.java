package com.example.strinder.backend_related;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Gender;


public class User implements Parcelable {

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String birthday;
    private String gender;
    private String photoUrl;
    private int id;

    public User(final String firstName, final String lastName, final String email,
                final String username, final String photoUrl) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.photoUrl = photoUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBirthday() {
        return birthday;
    }

    private void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = String.format("%s/%s/%s",
                birthday.getYear() == null ? "?" : birthday.getYear(),
                birthday.getMonth() == null ? "?" : birthday.getMonth(),
                birthday.getDay() == null ? "?" : birthday.getDay());
    }

    public String getGender() {
        return gender;
    }

    private void setGender(String gender) {
        this.gender = gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender.getFormattedValue();
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
                    in.readString());
            user.setBirthday(in.readString());
            user.setGender(in.readString());
            user.setId(in.readInt());

            return user;
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
