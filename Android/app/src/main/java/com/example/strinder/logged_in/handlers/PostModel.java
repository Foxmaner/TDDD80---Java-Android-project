package com.example.strinder.logged_in.handlers;

public class PostModel {
    String name;
    String caption;

    public PostModel(String name, String caption) {
        this.name = name;
        this.caption = caption;
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

    @Override
    public String toString() {
        return "PostModel{" +
                "name='" + name + '\'' +
                ", caption='" + caption + '\'' +
                '}';
    }
}
