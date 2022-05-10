package com.example.strinder.logged_in;

import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.User;

import java.util.List;

public class FetchedPosts {
    private List<Post> posts;
    private List<User> users;

    public List<Post> getPosts() {
        return posts;
    }

    public List<User> getUsers() {
        return users;
    }

}
