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
        yield client
    os.close(db_fd)
    os.unlink(name)


def test1(client):
    # Register a user to the db.
    user = {"username": "Liam", "first_name": "Liam", "last_name": "Andersson", "password": "Hej123"}
    user1_request = client.post(address + "/add", json=user)
    assert user1_request.status_code == 200


def test2(client):
    # Register a user to the db.
    user = {"username": "EskilCatMan", "first_name": "Eskil", "last_name": "Brännerud", "password": "Hej1234"}
    user1_request = client.post(address + "/add", json=user)
    assert user1_request.status_code == 200


def test3(client):
    user = {"username": "Liam", "password": "Hej123"}
    user_login = client.post(address + "/user/login", json=user)
    assert user_login.status_code == 200


def test4(client):
    # We need the token
    token = login_and_get_token(client)
    # Add a post to the user_id: 1.
    post_data = {"title": "Min post", "caption": "Omg guys!"}
    add_post_req = client.post(address + "/add/1", json=post_data, headers={"Authorization": "Bearer " +
                                                                            token})
    assert add_post_req.status_code == 200


def test5(client):
    # We need the token
    token = login_and_get_token(client)
    post_data = {"title": "Min andra post", "caption": "Omg guys hello there!"}
    add_post_req2 = client.post(address + "/add/1", json=post_data, headers={"Authorization": "Bearer " +
                                                                             token})
    assert add_post_req2.status_code == 200


def test6(client):
    # We need the token
    token = login_and_get_token(client)
    # Befriends two users
    befriend_request = client.post(address + "/befriend/1/2", headers={"Authorization": "Bearer " + token})
    assert befriend_request.status_code == 200


def test7(client):
    # We need the token
    token = login_and_get_token(client)
    # Checks if two users are friends
    are_friends_req = client.get(address + "/friends/1/2", headers={"Authorization": "Bearer " + token})
    assert are_friends_req.status_code == 200


def test8(client):
    # We need the token
    token = login_and_get_token(client)
    # Returns ALL (if any) of the latest posts from user with id: 1.
    get_posts_req = client.get(address + "/posts/1/-1", headers={"Authorization": "Bearer " + token})
    assert get_posts_req.status_code == 200


def test9(client):
    # We need the token
    token = login_and_get_token(client)
    # Returns a specific amount of friends to a user.
    get_posts_req = client.get(address + "/friends/1/1", headers={"Authorization": "Bearer " + token})
    return get_posts_req.status_code == 200


def test10(client):
    # We need the token
    token = login_and_get_token(client)
    #  Add a comment to a post
    post_data = {"text": "Du är så snygg", "user_id": 1}
    add_comment_req = client.post(address + "/comments/1", json=post_data,
                                  headers={"Authorization": "Bearer " + token})
    assert add_comment_req.status_code == 200


def test11(client):
    # We need the token
    token = login_and_get_token(client)
    # Print all comments to a post with the route
    get_comments_req = client.get(address + "/comments/1/-1", headers={"Authorization": "Bearer " + token})
    assert get_comments_req.status_code == 200


# --- Deletes --- #


def test12(client):
    # We need the token
    token = login_and_get_token(client)
    # Remove a user with user_id:1

    remove = client.delete(address + "/del/usr/2", headers={"Authorization": "Bearer " + token})
    assert remove.status_code == 200


def test13(client):
    # We need the token
    token = login_and_get_token(client)
    # Remove a user with user_id:1, which does not exist

    remove2 = client.delete(address + "/del/usr/8", headers={"Authorization": "Bearer " + token})
    assert remove2.status_code == 400


def test14(client):
    # We need the token
    token = login_and_get_token(client)
    # Remove a post with post_id:1.

    remove3 = client.delete(address + "/del/post/1", headers={"Authorization": "Bearer " + token})
    assert remove3.status_code == 200


def test15(client):
    # We need the token
    token = login_and_get_token(client)
    # Remove a post with post_id:8, which does not exist

    remove4 = client.delete(address + "/del/post/8", headers={"Authorization": "Bearer " + token})
    assert remove4.status_code == 400


def test16(client):
    # We need the token
    token = login_and_get_token(client)
    # Remove a post with comment_id:1.

    remove5 = client.delete(address + "/del/comment/1", headers={"Authorization": "Bearer " + token})
    assert remove5.status_code == 200


def test17(client):
    # We need the token
    token = login_and_get_token(client)
    # Remove a post with comment_id:8, which does not exist

    remove6 = client.delete(address + "/del/comment/8", headers={"Authorization": "Bearer " + token})
    assert remove6.status_code == 400


def test18(client):
    # Remove a post with comment_id:8, which does not exist and where there is no token

    remove7 = client.delete(address + "/del/comment/8")
    assert remove7.status_code == 401


# Help function
def login_and_get_token(client):
    user = {"username": "Liam", "password": "Hej123"}
    user_login = client.post(address + "/user/login", json=user)
    return user_login.get_json()["access_token"]
