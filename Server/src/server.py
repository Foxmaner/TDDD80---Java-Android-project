import os
import re
import traceback
from datetime import timedelta, datetime, timezone

from flask import jsonify, request
from flask_jwt_extended import (
    JWTManager, jwt_required, create_access_token,
    create_refresh_token,
    get_jwt_identity, get_jwt
)
from google.auth.transport import requests
from google.oauth2 import id_token
from sqlalchemy import desc

from database_com import app, db, User, Post, Comment, TokenBlocklist, TrainingSession

ACCESS_EXPIRES = timedelta(minutes=15)
app.config["JWT_SECRET_KEY"] = os.environ.get("JWT_SECRET_KEY")
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = ACCESS_EXPIRES
jwt = JWTManager(app)


# Callback function to check if a JWT exists in the database blocklist
@jwt.token_in_blocklist_loader
def check_if_token_revoked(_, jwt_payload: dict) -> bool:
    jti = jwt_payload["jti"]
    token = db.session.query(TokenBlocklist.id).filter_by(jti=jti).scalar()

    return token is not None


# ----- POST ----- #

# The jwt_refresh_token_required decorator insures a valid refresh
# token is present in the request before calling this endpoint. We
# can use the get_jwt_identity() function to get the identity of
# the refresh token, and use the create_access_token() function again
# to make a new access token for this identity.

# Motivation for this route: We did not want to create a new access token for every request to the server.
@app.route("/refresh", methods=['POST'])
@jwt_required(refresh=True)
def refresh():
    """Refreshes the access token if the function is sent a valid refresh token."""
    ret = {
        'access_token': create_access_token(identity=get_jwt_identity())
    }

    return jsonify(ret), 200


@app.route("/authenticate", methods=["POST"])
def authenticate():
    """
    Authenticate a user to the backend. Takes an id token from the client and checks if
    the signature is valid. If valid, we get access to some data from Google which we use to check if
    there exists an account with that id, or if we need to register the user. The return value of this
    function is the user values and a refresh- and access-token.
    """
    post_input = request.get_json()
    token = post_input["idToken"]
    web_id = os.environ.get('WEB_KEY')

    try:
        google_request = requests.Request()
        # Specify the CLIENT_ID of the app that accesses the backend:
        info = id_token.verify_oauth2_token(token, google_request, web_id, 10)

        if info["iss"] == "https://accounts.google.com":
            # ID token is valid. Get the user's Google Account ID and other information.
            username = info["sub"]
            email = info["email"]
            first_name = ""
            if "given_name" in info:
                first_name = info["given_name"]
            last_name = ""
            if "family_name" in info:
                last_name = info["family_name"]
            photo_url = None
            if "picture" in info:
                photo_url = info["picture"]
            # From Post Request
            birthday = post_input["birthday"]
            gender = post_input["gender"]

            user = User.query.filter_by(username=username).first()

            # If there are no usernames like this, we continue.
            if user is None:
                # Some accounts do not have these values set. So we need to check
                if birthday is not None:
                    birthday = datetime.strptime(birthday, "%Y/%m/%d")

                user = User(username=username, first_name=first_name, last_name=last_name,
                            email=email, gender=gender, birthday=birthday, biography="I am new to Strinder!",
                            photo_url=photo_url)

                # Add it to the database and save.
                db.session.add(user)
                db.session.commit()

            token = create_access_token(identity=user.id)
            refresh_token = create_refresh_token(identity=user.id)

            data = user.to_dict()
            data["accessToken"] = token
            data["refreshToken"] = refresh_token
            return data, 200

        else:
            return "", 400

    except(ValueError, KeyError):
        # Print the exception.
        print(traceback.format_exc())
        return "", 400


@app.route("/post/add", methods=["POST"])
@jwt_required()
def add_post():
    """This function adds a post to the logged-in user."""
    # Convert parameter to int
    try:
        user_id = get_jwt_identity()
    # If the user_id is incorrect, return error code.
    except(ValueError, TypeError):
        return "", 400

    # Query the user. (There is only one)
    query = User.query.filter_by(id=user_id).first()
    print(user_id)
    if query is not None:
        post_data = request.get_json()

        try:
            # Try to get post_data, if it fails we throw a 400 error code.
            post = Post(user_id=user_id, title=post_data["title"], caption=post_data["caption"],
                        longitude=post_data["longitude"], latitude=post_data["latitude"])
        except KeyError:
            return "", 400

        # Save the post to the user and commit changes to database.
        query.posts.append(post)

        db.session.commit()

        return jsonify(post.to_dict()), 200
    else:
        # We found no match, so return error code 400
        return "", 400


@app.route("/session/set", methods=["POST"])
@jwt_required()
def set_session():
    """
    Sets the training session for a specific post.
    What post is given through the json file.
    """
    post_input = request.get_json()
    try:
        user_id = get_jwt_identity()

        # Check the time format. It has to match 99:59 etc.
        elapsed_time = post_input["time"]
        pattern = re.compile("^[0-9][0-9]:[0-5][0-9]$")
        if pattern.match(elapsed_time) is None:
            return "", 400

        time = elapsed_time
        post_id = post_input["postId"]
        speed_unit = post_input["speedUnit"]
        speed = post_input["speed"]
        distance = post_input["distance"]
        distance_unit = post_input["distanceUnit"]
        exercise = post_input["exercise"]
    # If the user_id is incorrect, return error code.
    except(ValueError, TypeError):
        return "", 400

    post = Post.query.filter_by(id=post_id).first()
    # Make sure that we are editing our own post.
    if post is not None and post.user_id == user_id:
        training = TrainingSession(time=time, post_id=post_id, speed_unit=speed_unit,
                                   speed=speed, distance=distance, distance_unit=distance_unit, exercise=exercise)
        # Update the training_session attribute.
        post.training_session = training

        db.session.commit()

        return jsonify(training.to_dict()), 200

    return "", 400


@app.route("/user/logout", methods=["POST"])
@jwt_required()
def logout():
    """Logs out the user and adds the access token to a 'block list'."""
    try:
        jti = get_jwt()["jti"]
        now = datetime.now(timezone.utc)
        db.session.add(TokenBlocklist(jti=jti, created_at=now))
        db.session.commit()
        return jsonify(msg="JWT revoked")
    except KeyError:
        return "", 400


@app.route("/user/set_data", methods=["POST"])
@jwt_required()
def set_data():
    """Sets the user data for the logged-in user. Some fields can not be changed, such as the email."""
    json_data = request.get_json()
    if json_data is None:
        return "", 400

    # Get the json data, if it is not available - return error code 400.
    try:
        # Try to convert the dictionary to variables. If it fails,then return error code 400.
        user_id = get_jwt_identity()
        firstname = json_data["first_name"]
        lastname = json_data["last_name"]
        birthday = json_data["birthday"]
        gender = json_data["gender"]
        biography = json_data["biography"]
        photo_url = json_data["photo_url"]
    except KeyError:
        return "", 400

    user = User.query.filter_by(id=user_id).first()

    if user is not None:
        if firstname is not None:
            user.first_name = firstname
        if lastname is not None:
            user.last_name = lastname
        if birthday is not None:
            user.birthday = datetime.strptime(birthday, "%Y/%m/%d")
        if gender is not None:
            user.gender = gender
        if biography is not None:
            user.biography = biography
        if photo_url is not None:
            user.photo_url = photo_url

        db.session.commit()

        return "", 200

    return "", 400


@app.route("/follow/<follow_id>", methods=["POST"])
@jwt_required()
def follow(follow_id):
    """
    Allows the logged-in user to follow a specific user.
    The id for this user is specified in the url.
    """

    # Try to convert to integer.
    try:
        follow_id = int(follow_id)
        user_id = get_jwt_identity()

    except(ValueError, TypeError):
        return "", 400

    # Query the users.
    follower = User.query.filter_by(id=follow_id).first()
    user = User.query.filter_by(id=user_id).first()

    if user is not None and follower is not None and user != follower and follower not in user.follows:
        user.follows.append(follower)

        db.session.commit()

        # Return the user we followed
        return jsonify(follower.to_dict()), 200

    return "", 400


@app.route("/comments/add/<post_id>", methods=["POST"])
@jwt_required()
def add_comment(post_id):
    """Adds a comment to a post. The post id is specified in the url. """
    # Try to convert to integer.
    try:
        post_id = int(post_id)
    except(ValueError, TypeError):
        return "", 400

    post = Post.query.filter_by(id=post_id).first()

    if post is not None:
        post_data = request.get_json()
        try:
            # Create a comment
            comment = Comment(post_id=post_id, text=post_data["text"], user_id=get_jwt_identity())
            post.comments.append(comment)
            db.session.commit()
            return "", 200

        except KeyError:
            return "", 400

    else:
        return "", 400


@app.route("/post/like/<post_id>", methods=["POST"])
@jwt_required()
def like(post_id):
    """
    Like / remove like from a specific post depending on if you have liked it or not.
    The post id is given in the url."""
    try:
        post_id = int(post_id)
        user_id = int(get_jwt_identity())
    except (ValueError, TypeError):
        return "", 400

    post = Post.query.filter_by(id=post_id).first()
    user = User.query.filter_by(id=user_id).first()
    if post is not None and user is not None:

        if user in post.likes:
            post.likes.remove(user)
        else:
            post.likes.append(user)

        db.session.commit()
        data = [user.to_dict_follows() for user in post.likes]
        return jsonify(data), 200
    else:
        return "", 400


@app.route("/follow/remove/<follow_id>", methods=["POST"])
@jwt_required()
def unfollow(follow_id):
    """
    Allows the user to unfollow a specific user.
    The id for this user is given in the url.
    """
    try:
        follow_id = int(follow_id)
    except (ValueError, TypeError):
        return "", 400

    follower = User.query.filter_by(id=follow_id).first()
    user = User.query.filter_by(id=get_jwt_identity()).first()

    if follower is not None and user is not None and follower in user.follows:
        user.follows.remove(follower)

        db.session.commit()
        return "", 200

    return "", 400


# ------- GET -------- #
@app.route("/post/get_likes/<post_id>", methods=["GET"])
@jwt_required()
def get_likes(post_id):
    """Returns all the likes for a specific post. The post id is given in the url."""
    try:
        post_id = int(post_id)
    except(ValueError, TypeError):
        return "", 400

    post = Post.query.filter_by(id=post_id).first()

    if post is not None:
        data = [user.to_dict_follows() for user in post.likes]
        return jsonify(data), 200
    else:
        return "", 400


@app.route("/user/get_user/<user_id>", methods=["GET"])
@jwt_required()
def get_user(user_id):
    """Returns the user data for a specific user. The id for this user is given in the url."""
    try:
        user_id = int(user_id)
    except (TypeError, ValueError):
        return "", 400

    user = User.query.filter_by(id=user_id).first()

    if user is not None:
        return jsonify(user.to_dict()), 200

    return "", 400


@app.route("/user/get_users/<full_name>")
@jwt_required()
def get_users_by_name(full_name):
    """
    Returns all the users that matches the given name, except
    the logged-in user. The function does not care about lower or upper case letters.
    """
    try:
        full_name = str(full_name)
    except (ValueError, TypeError):
        return "", 400

    users = User.query.filter(User.full_name.like("%" + full_name.lower() + "%"),
                              User.id != get_jwt_identity()).all()

    if users is not None:
        # Convert User objects to dictionary.
        users = [user.to_dict_follows() for user in users]

        return jsonify(users), 200

    return "", 400


@app.route("/posts/latest/<nr_of_posts>", methods=["GET"])
@jwt_required()
def get_posts(nr_of_posts):
    """Fetch selected nr of posts ordered by latest. -1 = ALL. The amount is given in the url."""

    try:
        nr_of_posts = int(nr_of_posts)

    except(ValueError, TypeError):
        return "", 400

    if nr_of_posts == -1:
        posts = [post.to_dict() for post in Post.query.
                 order_by(desc(Post.date_time)).all() if post.user_id]
    elif nr_of_posts >= 0:
        posts = [post.to_dict() for post in Post.query.
                 order_by(desc(Post.date_time)).limit(nr_of_posts).all()]
    else:
        return "", 400

    users = []
    new_posts = []

    current_user = User.query.filter_by(id=get_jwt_identity()).first()
    # This code can probably be achieved more efficiently through SQL code.
    # Only get the posts that are from users that you follow.
    for post in posts:

        post_user = User.query.filter_by(id=post["userId"]).first()

        if post_user.id != get_jwt_identity():

            for user in current_user.follows:
                if user.id == post["userId"]:
                    new_posts.append(post)
                    users.append(user)
        else:
            new_posts.append(post)
            users.append(post_user)

    data = {"posts": new_posts, "users": [user.to_dict_follows() for user in users]}
    return data, 200


@app.route("/comments/<post_id>", methods=["GET"])
@jwt_required()
def get_comments(post_id):
    """Fetch all comments for a specific post. """
    try:
        post_id = int(post_id)

    except(ValueError, TypeError):
        return "", 400

    post = Post.query.filter_by(id=post_id).first()

    if post is not None:
        comments = [comment.to_dict() for comment in post.comments]

        return jsonify(comments), 200
    else:
        return "", 400


# This is not used in our code - therefore untested! It is included in the unit tests.
@app.route("/followers/<nr_of_follows>", methods=["GET"])
@jwt_required()
def get_followers(nr_of_follows):
    """Fetch selected nr of follows. -1 = ALL. The amount is given in the url."""
    try:
        user_id = int(get_jwt_identity())
        nr_of_follows = int(nr_of_follows)

    except(ValueError, TypeError):
        return "", 400

    user = User.query.filter_by(id=user_id).first()

    if user is not None:

        if nr_of_follows == -1:
            follows = [follower.to_dict() for follower in user.follows]

        elif nr_of_follows >= 0:
            follows = [follower.to_dict() for follower in user.follows[:nr_of_follows]]
        else:
            return "", 400
    else:
        return "", 400

    return jsonify(follows), 200


@app.route('/del/post/<post_id>', methods=["DELETE"])
@jwt_required()
def remove_post(post_id):
    """Remove a post that is created by the currently logged-in user. The post id is given in the url. """
    post = Post.query.filter_by(id=post_id)

    if post.first() is not None and post.first().user_id == get_jwt_identity():
        post.likes = []
        post.delete()
        db.session.commit()
        return "", 200
    else:
        return "", 400


@app.route('/del/comment/<comment_id>', methods=["DELETE"])
@jwt_required()
def remove_comment(comment_id):
    """Remove a comment that is created by the currently logged-in user. The comment id is given in the url."""
    comment = Comment.query.filter_by(id=comment_id)

    if comment.first() is not None and comment.first().user_id == get_jwt_identity():
        comment.delete()
        db.session.commit()
        return "", 200
    else:
        return "", 400


if __name__ == "__main__":
    app.debug = True
    app.port = int(os.environ.get("PORT", 8080))
    # Initialize database (Not run on heroku)
    db.create_all()
    db.session.commit()
    app.run()


# This runs before the first ever request and is NECESSARY in order for the heroku server to run correctly!
@app.before_first_request
def init():
    db.create_all()
    db.session.commit()
