import requests
print("Running Client...")


# Add a user to the db.
user = {"username": "Liam", "first_name": "Liam", "last_name": "Andersson", "password": "Hej123"}
user1_request = requests.post("http://localhost:5000/add", json=user)
print(user1_request.text)

# Add a user to the db.
user = {"username": "Eskil", "first_name": "Eskil", "last_name": "Brännerud", "password": "Hej123"}
user2_request = requests.post("http://localhost:5000/add", json=user)
print(user2_request.text)


# Add a post to the user_id: 1.
post_data = {"title": "Min post", "caption": "Omg guys!"}
add_post_req = requests.post("http://localhost:5000/add/1", json=post_data)
print(add_post_req.text)

# Add a post to the user_id: 1.
post_data = {"title": "Min andra post", "caption": "Omg guys hello there!"}
add_post_req2 = requests.post("http://localhost:5000/add/1", json=post_data)
print(add_post_req2.text)

# Befriends two users
befriend_request = requests.post("http://localhost:5000/befriend/1/2")
print(befriend_request.text)

# Checks if two users are friends
are_friends_req = requests.get("http://localhost:5000/friends/1/2")
print(are_friends_req.text)

# Returns ALL (if any) of the latest posts from user with id: 1.
get_posts_req = requests.get("http://localhost:5000/posts/1/-1")
print(get_posts_req.text)

# Returns a specific amount of friends to a user.
get_posts_req = requests.get("http://localhost:5000/friends/1/1")
print(get_posts_req.text)

# Returns a specific amount of friends to a user.
get_posts_req = requests.get("http://localhost:5000/friends/1/1")
print(get_posts_req.text)

# Removes first user "Liam"
remove_usr_req = requests.delete("http://localhost:5000/del/usr/1")
print(remove_usr_req.text)

# Try to remove not valid user
remove_usr_req = requests.delete("http://localhost:5000/del/usr/8")
print(remove_usr_req.text)

# Removes first post
remove_post_req = requests.delete("http://localhost:5000/del/post/1")
print(remove_post_req.text)

# Try to remove not valid post
remove_post_req = requests.delete("http://localhost:5000/del/post/8")
print(remove_post_req.text)

# Removes first comment
remove_comment_req = requests.delete("http://localhost:5000/del/comment/1")
print(remove_post_req.text)

# Try to remove not valid comment
remove_comment_req = requests.delete("http://localhost:5000/del/comment/8")
print(remove_post_req.text)





# TODO Add a comment to a post

# TODO Print all comments to a post with the route
