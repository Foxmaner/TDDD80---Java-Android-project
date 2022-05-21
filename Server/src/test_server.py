import tempfile
import os
from datetime import datetime

import pytest
from flask_jwt_extended import create_access_token

import server
from database_com import db_uri, address, User, db, app

# HOW TO RUN:
# coverage run -m pytest test_server.py
# coverage report -m
# Might have to do python3 -m <rest here>

"""
NOTE: There can be a conflict with the @app.before_first_request annotation
in server.py. If so, comment it out.
"""


# IMPORTANT NOTE: Save your database before this, because this drops the database.
@pytest.fixture(scope="session", autouse=True)
def init():
    with app.app_context():
        db.drop_all()
        db.create_all()
        db.session.commit()

        # This is global because we can't pass it to the tests in a good way.
        global user
        user = authenticate()


@pytest.fixture
def client():
    db_fd, name = tempfile.mkstemp()
    server.app.config['SQLALCHEMY_DATABASE_URL'] = db_uri

    server.app.config['TESTING'] = True
    with server.app.test_client() as client:
        with app.app_context():
            yield client, user

    os.close(db_fd)
    os.unlink(name)


def test_add_post_1(client):
    # We need the token
    c = client[0]
    token = get_token(client)
    print(address)
    # Add a post to the user_id: 1.
    post_data = {"title": "Min post", "caption": "Omg guys!", "longitude": 50, "latitude": 50}

    add_post_req = c.post(address + "/post/add", json=post_data, headers={"Authorization": "Bearer " + token})
    assert add_post_req.status_code == 200


def test_add_post_2(client):
    c = client[0]
    token = get_token(client)
    # We need the token

    post_data = {"title": "Min andra post", "caption": "Omg guys hello there!", "longitude": 50, "latitude": 50}
    add_post_req2 = c.post(address + "/post/add", json=post_data, headers={"Authorization": "Bearer " + token})
    assert add_post_req2.status_code == 200


def test_follow(client):
    c = client[0]
    token = get_token(client)
    # We need the token

    # Follow yourself
    user_id = str(client[1]["id"])
    befriend_request = c.post(address + "/follow/"+user_id, headers={"Authorization": "Bearer " + token})
    assert befriend_request.status_code == 400


def test_like_post(client):
    c = client[0]
    token = get_token(client)
    # We need the token

    like_req = c.post(address + "/post/like/1", headers={"Authorization": "Bearer " + token})
    assert like_req.status_code == 200


def test_see_followers(client):
    c = client[0]
    token = get_token(client)
    # We need the token

    # Checks if two users are friends
    are_friends_req = c.get(address + "/followers/-1", headers={"Authorization": "Bearer " + token})
    assert are_friends_req.status_code == 200


def test_see_posts_1(client):
    c = client[0]
    token = get_token(client)
    # We need the token

    # Returns ALL (if any) of the latest posts from user with id: 1.
    get_posts_req = c.get(address + "/posts/latest/-1", headers={"Authorization": "Bearer " + token})
    assert get_posts_req.status_code == 200


def test_add_comment_1(client):
    c = client[0]
    token = get_token(client)
    # We need the token

    #  Add a comment to a post
    post_data = {"text": "Du är så snygg"}
    add_comment_req = c.post(address + "/comments/add/1", json=post_data,
                             headers={"Authorization": "Bearer " + token})
    assert add_comment_req.status_code == 200


def test_see_all_comments_1(client):
    c = client[0]
    token = get_token(client)
    # We need the token

    # Print all comments to a post with the route
    get_comments_req = c.get(address + "/comments/1", headers={"Authorization": "Bearer " + token})
    assert get_comments_req.status_code == 200


# --- Deletes --- #

def test_del_post_1(client):
    c = client[0]
    token = get_token(client)
    # We need the token

    # Remove a post with post_id:1.

    remove3 = c.delete(address + "/del/post/1", headers={"Authorization": "Bearer " + token})
    assert remove3.status_code == 200


def test_del_post_again(client):
    c = client[0]
    token = get_token(client)
    # We need the token

    # Remove a post with post_id:8, which does not exist

    remove4 = c.delete(address + "/del/post/1", headers={"Authorization": "Bearer " + token})
    assert remove4.status_code == 400


def test_del_comment_1(client):
    c = client[0]
    token = get_token(client)
    # We need the token

    # Remove a post with comment_id:1.

    remove5 = c.delete(address + "/del/comment/1", headers={"Authorization": "Bearer " + token})
    assert remove5.status_code == 200


def test_del_comment_again(client):
    c = client[0]
    token = get_token(client)
    # We need the token

    # Remove a post with comment_id:8, which does not exist

    remove6 = c.delete(address + "/del/comment/8", headers={"Authorization": "Bearer " + token})
    assert remove6.status_code == 400


# ---- HELP METHODS ----#
def get_token(client):
    """Returns the access token. """
    return client[1]["accessToken"]


def authenticate():
    """
    This method authenticates a user. This is necessary in testing, as the
    app used GoogleSignIn - which I can't do through testing. This function basically
    generates an access token for a test user.
    """

    test_user = User.query.filter_by(email="test@test.se").first()

    if test_user is None:
        test_user = User(username="0", first_name="Firstname", last_name="Lastname",
                         email="test@test.se", gender="Male", birthday=datetime.strptime("2002/09/15", "%Y/%m/%d"),
                         biography="I am new to this app!")

        # Add it to the database and save.
        db.session.add(test_user)
        db.session.commit()

    # Create the access token
    token = create_access_token(identity=test_user.id)
    data = test_user.to_dict()
    data["accessToken"] = token

    return data
