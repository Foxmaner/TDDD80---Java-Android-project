package com.example.strinder.logged_in;

import com.example.strinder.backend_related.tables.Post;
import com.example.strinder.backend_related.tables.User;

import java.util.List;

/** This class is used when retrieving all the {@link Post Post} objects and the corresponding
 * {@link User User} objects. This class simply wraps two {@link List List} objects into a class
 * in order to use {@link com.google.gson.Gson Gson} in a clever and short way.
 */
public class FetchedPosts {
    // These will never be assigned to (GSON magic).
    @SuppressWarnings("unused")
    private List<Post> posts;
    @SuppressWarnings("unused")
    private List<User> users;

    /** Returns the posts.
     *
     * @return a {@link List List<Post>} object
     */
    public List<Post> getPosts() {
        return posts;
    }

    /** Returns the users.
     *
     * @return a {@link List List<User>} object.
     */
    public List<User> getUsers() {
        return users;
    }

}
