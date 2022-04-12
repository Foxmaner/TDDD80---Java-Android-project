import tempfile
import os
import pytest
import server
from database_com import db_uri, address

# Run with pytest. "pytest test_server.py"


@pytest.fixture
def client():
    db_fd, name = tempfile.mkstemp()
    server.app.config['SQLALCHEMY_DATABASE_URL'] = db_uri

    server.app.config['TESTING'] = True
    with server.app.test_client() as client:
        with server.app.app_context():
            server.db.create_all()

            user = {"username": "TestUser1", "first_name": "TestUser1FirstName", "last_name": "TestUser1LastName",
                    "password": "TestUser1Password"}

            client.post(address + "/add", json=user)

            token = login_and_get_token(client)

        yield client, token
    os.close(db_fd)
    os.unlink(name)


def test_register_1(client):
    # Register a user to the db.

    user = {"username": "Liam", "first_name": "Liam", "last_name": "Andersson", "password": "Hej123"}
    c = client[0]
    print(client[1])
    user1_request = c.post(address + "/add", json=user)
    assert user1_request.status_code == 200


def test_register_2(client):
    # Register a user to the db.
    user = {"username": "EskilCatMan", "first_name": "Eskil", "last_name": "Brännerud", "password": "Hej1234"}
    c = client[0]
    user1_request = c.post(address + "/add", json=user)
    assert user1_request.status_code == 200


def test_login_1(client):
    user = {"username": "TestUser1", "password": "TestUser1Password"}
    c = client[0]
    user_login = c.post(address + "/user/login", json=user)
    assert user_login.status_code == 200


def test_add_post_1(client):
    # We need the token
    c = client[0]
    token = client[1]

    # Add a post to the user_id: 1.
    post_data = {"title": "Min post", "caption": "Omg guys!"}
    add_post_req = c.post(address + "/add/1", json=post_data, headers={"Authorization": "Bearer " +
                                                                                        token})
    assert add_post_req.status_code == 200


def test_add_post_2(client):
    c = client[0]
    token = client[1]
    # We need the token

    post_data = {"title": "Min andra post", "caption": "Omg guys hello there!"}
    add_post_req2 = c.post(address + "/add/1", json=post_data, headers={"Authorization": "Bearer " +
                                                                                         token})
    assert add_post_req2.status_code == 200


def test_add_friend_1(client):
    c = client[0]
    token = client[1]
    # We need the token

    # Befriends two users
    befriend_request = c.post(address + "/befriend/1/2", headers={"Authorization": "Bearer " + token})
    assert befriend_request.status_code == 200


def test_see_friends_1(client):
    c = client[0]
    token = client[1]
    # We need the token

    # Checks if two users are friends
    are_friends_req = c.get(address + "/friends/1/2", headers={"Authorization": "Bearer " + token})
    assert are_friends_req.status_code == 200


def test_see_posts_1(client):
    c = client[0]
    token = client[1]
    # We need the token

    # Returns ALL (if any) of the latest posts from user with id: 1.
    get_posts_req = c.get(address + "/posts/1/-1", headers={"Authorization": "Bearer " + token})
    assert get_posts_req.status_code == 200


def test_see_nr_friends_2(client):
    c = client[0]
    token = client[1]
    # We need the token

    # Returns a specific amount of friends to a user.
    get_posts_req = c.get(address + "/friends/1/1", headers={"Authorization": "Bearer " + token})
    return get_posts_req.status_code == 200


def test_add_comment_1(client):
    c = client[0]
    token = client[1]
    # We need the token

    #  Add a comment to a post
    post_data = {"text": "Du är så snygg", "user_id": 1}
    add_comment_req = c.post(address + "/comments/1", json=post_data,
                             headers={"Authorization": "Bearer " + token})
    assert add_comment_req.status_code == 200


def test_see_all_comments_1(client):
    c = client[0]
    token = client[1]
    # We need the token

    # Print all comments to a post with the route
    get_comments_req = c.get(address + "/comments/1/-1", headers={"Authorization": "Bearer " + token})
    assert get_comments_req.status_code == 200


# --- Deletes --- #


def test_del_user_1(client):
    c = client[0]
    token = client[1]
    # We need the token

    # Remove a user with user_id:1

    remove = c.delete(address + "/del/usr/2", headers={"Authorization": "Bearer " + token})
    assert remove.status_code == 200


def test_login_removed_user_1(client):
    c = client[0]
    token = client[1]
    # We need the token

    # Remove a user with user_id:1, which does not exist

    remove2 = c.delete(address + "/del/usr/8", headers={"Authorization": "Bearer " + token})
    assert remove2.status_code == 400


def test_del_post_1(client):
    c = client[0]
    token = client[1]
    # We need the token

    # Remove a post with post_id:1.

    remove3 = c.delete(address + "/del/post/1", headers={"Authorization": "Bearer " + token})
    assert remove3.status_code == 200


def test_see_del_post_1(client):
    c = client[0]
    token = client[1]
    # We need the token

    # Remove a post with post_id:8, which does not exist

    remove4 = c.delete(address + "/del/post/8", headers={"Authorization": "Bearer " + token})
    assert remove4.status_code == 400


def test_del_comment_1(client):
    c = client[0]
    token = client[1]
    # We need the token

    # Remove a post with comment_id:1.

    remove5 = c.delete(address + "/del/comment/1", headers={"Authorization": "Bearer " + token})
    assert remove5.status_code == 200


def test_del_comment_2(client):
    c = client[0]
    token = client[1]
    # We need the token

    # Remove a post with comment_id:8, which does not exist

    remove6 = c.delete(address + "/del/comment/8", headers={"Authorization": "Bearer " + token})
    assert remove6.status_code == 400


def test_del_comment_3(client):
    # Remove a post with comment_id:8, which does not exist and where there is no token
    c = client[0]

    remove7 = c.delete(address + "/del/comment/8")
    assert remove7.status_code == 401


# Help function
def login_and_get_token(client):
    user = {"username": "TestUser1", "password": "TestUser1Password"}
    user_login = client.post(address + "/user/login", json=user)
    print(user_login.status_code)

    return user_login.get_json()["access_token"]
