package com.example.strinder.logged_in.handlers;

public class PostModel {
    String name;
    String caption;
    String distance;
    String time;
    String speed;

    public PostModel(String name, String caption, String distance, String time, String speed) {
        this.name = name;
        this.caption = caption;
        this.distance = distance;
        this.time = time;
        this.speed = speed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "PostModel{" +
                "name='" + name + '\'' +
                ", caption='" + caption + '\'' +
                ", distance='" + distance + '\'' +
                ", time='" + time + '\'' +
                ", speed='" + speed + '\'' +
                '}';
    }
}
