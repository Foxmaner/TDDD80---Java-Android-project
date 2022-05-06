import requests
# Tests that do not use Pytest
from src.database_com import address

print("Running Client...")

# Register a user to the db.
user = {"username": "Liam", "first_name": "Liam", "last_name": "Andersson", "password": "Hej123"}
user1_request = requests.post(address + "/add", json=user)
print(user1_request.text)

# Register a user to the db.
user = {"username": "EskilCatMan", "first_name": "Eskil", "last_name": "Brännerud", "password": "Hej1234"}
user1_request_2 = requests.post(address+"/add", json=user)
print(user1_request_2.text)

# Login
user = {"username": "Liam", "password": "Hej123"}
user_login = requests.post(address+"/user/login", json=user)
token = user_login.json()["access_token"]

# Add a post to the user_id: 1.
post_data = {"title": "Min post", "caption": "Omg guys!"}
add_post_req = requests.post(address+"/add/1", json=post_data, headers={"Authorization": "Bearer " +
                                                                                                      token})
print(add_post_req.text)

# Add a post to the user_id: 1.
post_data = {"title": "Min andra post", "caption": "Omg guys hello there!"}
add_post_req2 = requests.post(address+"/add/1", json=post_data, headers={"Authorization": "Bearer " +
                                                                                                       token})
print(add_post_req2.text)

# Befriends two users
befriend_request = requests.post(address+"/befriend/1/2", headers={"Authorization": "Bearer " + token})
print(befriend_request.text)

# Checks if two users are friends
are_friends_req = requests.get(address+"/friends/1/2", headers={"Authorization": "Bearer " + token})
print(are_friends_req.text)

# Returns ALL (if any) of the latest posts from user with id: 1.
get_posts_req = requests.get(address+"/posts/1/-1", headers={"Authorization": "Bearer " + token})
print(get_posts_req.text)

# Returns a specific amount of friends to a user.
get_posts_req = requests.get(address+"/friends/1/1", headers={"Authorization": "Bearer " + token})
print(get_posts_req.text)

#  Add a comment to a post
post_data = {"text": "Du är så snygg", "user_id": 1}
add_comment_req = requests.post(address+"/comments/1", json=post_data, headers={"Authorization": "Bearer "
                                                                                                              + token})
print(add_comment_req.text)


# Print all comments to a post with the route
get_comments_req = requests.get(address+"/comments/1/-1", headers={"Authorization": "Bearer " + token})
print(get_comments_req.text)

# --- Deletes --- #
# Removes first user "Liam"
remove_usr_req = requests.delete(address+"/del/usr/1", headers={"Authorization": "Bearer " + token})
print(remove_usr_req.text)

# Try to remove not valid user
remove_usr_req = requests.delete(address+"/del/usr/8", headers={"Authorization": "Bearer " + token})
print(remove_usr_req.text)

# Removes first post
remove_post_req = requests.delete(address+"/del/post/1", headers={"Authorization": "Bearer " + token})
print(remove_post_req.text)

# Try to remove not valid post
remove_post_req = requests.delete(address+"/del/post/8", headers={"Authorization": "Bearer " + token})
print(remove_post_req.text)

# Removes first comment
remove_comment_req = requests.delete(address+"/del/comment/1", headers={"Authorization": "Bearer " +
                                                                                     token})
print(remove_post_req.text)

# Try to remove not valid comment
remove_comment_req_2 = requests.delete(address+"/del/comment/8", headers={"Authorization": "Bearer " +
                                                                                       token})
print(remove_comment_req_2.text)
